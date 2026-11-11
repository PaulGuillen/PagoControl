package com.devpaul.features.resume.importing

import com.devpaul.core.domain.resume.ResumeDocumentCodec
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.features.templates.ResumeTemplateCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeTextParserTest {

    @Test
    fun `maps every resume section to the creation flow models`() {
        val document = ResumeTextParser.parse(
            """
            PAUL GUILLEN ACUÑA
            Ingeniero de Sistemas
            +51 977 588 534
            paul.guillen@example.com
            Lima, Perú
            https://github.com/paulguillen
            https://linkedin.com/in/paulguillen

            SOBRE MÍ
            Desarrollador Android con experiencia creando aplicaciones accesibles y escalables.

            EXPERIENCIA LABORAL
            Mobile Developer | Zoluxiones Latam | Lima | sep 2025 - Actualidad
            Desarrollo de aplicaciones nativas con Kotlin y Jetpack Compose.
            Software Engineer | Indra | Lima | nov 2024 - may 2025
            Implementación de arquitectura limpia y pruebas unitarias.

            EDUCACIÓN
            Bachiller en Ingeniería de Sistemas | Universidad Autónoma del Perú | Lima | 2017 - 2022

            HABILIDADES
            Kotlin • Jetpack Compose • MVVM • Firebase

            IDIOMAS
            Español (Nativo) • Inglés (Avanzado)

            CURSOS
            Desarrollo Web con React | IDAT | sep 2024 - ene 2025

            REFERENCIAS
            Miguel Pacheco | Indra Company | +51 943 603 363 | mpacheco@example.com
            """.trimIndent(),
        )

        assertEquals("Paul", document.contact.name)
        assertEquals("Guillen Acuña", document.contact.lastName)
        assertEquals("Ingeniero de Sistemas", document.contact.professionalTitle)
        assertEquals("paul.guillen@example.com", document.contact.email)
        assertEquals("Lima", document.contact.city)
        assertEquals(2, document.experiences.size)
        assertEquals("Mobile Developer", document.experiences.first().position)
        assertEquals("Zoluxiones Latam", document.experiences.first().company)
        assertEquals("2025", document.experiences.first().startYear)
        assertEquals("", document.experiences.first().endYear)
        assertTrue(document.experiences.first().isCurrent)
        assertEquals("Universidad Autónoma del Perú", document.education.single().institution)
        assertEquals(listOf("Kotlin", "Jetpack Compose", "MVVM", "Firebase"), document.skills)
        assertTrue(document.summary.startsWith("Desarrollador Android"))
        assertEquals("Desarrollo Web con React", document.courses.single().name)
        assertEquals(2, document.links.size)
        assertEquals("Español", document.languages.first().language)
        assertEquals("Nativo", document.languages.first().level)
        assertEquals("Miguel Pacheco", document.references.single().name)
        assertEquals(
            setOf(
                ResumeOptionalSection.Courses,
                ResumeOptionalSection.Links,
                ResumeOptionalSection.Languages,
                ResumeOptionalSection.References,
            ),
            document.optionalSections,
        )
    }

    @Test(expected = ResumeImportException::class)
    fun `rejects a document without readable text`() {
        ResumeTextParser.parse("   \n\t")
    }

    @Test
    fun `keeps separate education entries when dates are missing`() {
        val document = ResumeTextParser.parse(
            """
            SARAH JOHNSON
            Diseñadora gráfica
            sarah@example.com

            EDUCACIÓN
            Maestría en Bellas Artes | Rhode Island School of Design | Providence
            Bachiller en Bellas Artes | California Institute of the Arts | California
            """.trimIndent(),
        )

        assertEquals(2, document.education.size)
        assertEquals("Rhode Island School of Design", document.education.first().institution)
        assertEquals("California", document.education.last().city)
    }

    @Test
    fun `reconstructs multiline references from a two column resume`() {
        val document = ResumeTextParser.parse(
            """
            PAUL GUILLEN ACUÑA
            Ingeniero de Sistemas
            paul@example.com

            REFERENCIAS
            MIGUEL ANGEL PACHECO PostgreSQL utilizando Python
            Indra Company control transaccional
            T:+51 943603363
            E:mapacheco@indracompany.com
            ARTEMIO GUERRERO SANTA Lima
            CRUZ
            Telefónica del Perú • Desarrollo de aplicaciones
            T:+51 995429498
            E:artemio.guerrero@telefonica.co
            m
            RENZO BERECHE Diseño de soluciones escalables
            NTT DATA negocio
            T:935320924
            E:rbereche@emeal.nttdata.com
            JHOSSY JHOSSEP VARGAS
            Scotiabank
            T:+51 932 937 506
            E:jhossvs2299@gmail.com

            HABILIDADES
            Kotlin • Jetpack Compose
            """.trimIndent(),
        )

        assertEquals(4, document.references.size)
        assertEquals("Miguel Angel Pacheco", document.references[0].name)
        assertEquals("Indra Company", document.references[0].company)
        assertEquals("Artemio Guerrero Santa Cruz", document.references[1].name)
        assertEquals("Telefónica del Perú", document.references[1].company)
        assertEquals("artemio.guerrero@telefonica.com", document.references[1].email)
        assertEquals("Renzo Bereche", document.references[2].name)
        assertEquals("NTT DATA", document.references[2].company)
        assertEquals("Jhossy Jhossep Vargas", document.references[3].name)
        assertTrue(ResumeOptionalSection.References in document.optionalSections)
    }

    @Test
    fun `fills contact and experience in form order using detected template regions`() {
        val document = ResumeTextParser.parse(
            ResumePdfTextContent(
                fullText = """
                    SOBRE MÍ
                    PAUL
                    Lima, Lima, Perú
                    GUILLEN ACUÑA +51977588534
                    paulguillen190@gmail.com
                    INGENIERO DE SISTEMAS
                    EXPERIENCIA LABORAL
                    ZOLUXIONES LATAM Mobile Developer - APP
                    Lima
                    sep. 2025 - Actualidad Scotiabank
                """.trimIndent(),
                headerText = """
                    PAUL
                    GUILLEN ACUÑA
                    INGENIERO DE SISTEMAS
                    Lima, Lima, Perú
                    +51977588534
                    paulguillen190@gmail.com
                """.trimIndent(),
                sidebarText = """
                    SOBRE MÍ
                    Desarrollador Android Senior con más de cinco años de experiencia.
                    REFERENCIAS
                    MIGUEL ANGEL PACHECO
                    Indra Company
                    T:+51 943603363
                    E:mapacheco@indracompany.com
                """.trimIndent(),
                mainText = """
                    PAUL
                    GUILLEN ACUÑA
                    INGENIERO DE SISTEMAS
                    Lima, Lima, Perú
                    +51977588534
                    paulguillen190@gmail.com
                    EXPERIENCIA LABORAL
                    ZOLUXIONES LATAM Mobile Developer - APP
                    Lima
                    sep. 2025 - Actualidad Scotiabank
                    Desarrollo de aplicaciones nativas con Kotlin.
                    INDRA Software Engineer - APP
                    Lima
                    nov. 2024 - may. 2025 Telefónica del Perú
                    Implementación de arquitectura limpia y pruebas unitarias.
                    EDUCACIÓN
                    UNIVERSIDAD Bachiller Ingeniería de Sistemas
                    AUTÓNOMA DEL
                    PERÚ
                    Lima
                    CIBERTEC Android Studio
                    Lima
                """.trimIndent(),
                templateId = ResumeTemplateCatalog.BUDAPEST_ID,
            ),
        )

        assertEquals("Paul", document.contact.name)
        assertEquals("Guillen Acuña", document.contact.lastName)
        assertEquals("Ingeniero de Sistemas", document.contact.professionalTitle)
        assertEquals("Lima", document.contact.city)
        assertEquals(2, document.experiences.size)
        assertEquals("Mobile Developer - APP", document.experiences[0].position)
        assertEquals("ZOLUXIONES LATAM", document.experiences[0].company)
        assertEquals("Lima", document.experiences[0].city)
        assertEquals("2025", document.experiences[0].startYear)
        assertEquals("", document.experiences[0].endYear)
        assertTrue(document.experiences[0].isCurrent)
        assertEquals("Software Engineer - APP", document.experiences[1].position)
        assertEquals("INDRA", document.experiences[1].company)
        assertEquals("2024", document.experiences[1].startYear)
        assertEquals("2025", document.experiences[1].endYear)
        assertEquals(2, document.education.size)
        assertEquals("Bachiller Ingeniería de Sistemas", document.education[0].degree)
        assertEquals("UNIVERSIDAD AUTÓNOMA DEL PERÚ", document.education[0].institution)
        assertEquals("Lima", document.education[0].city)
        assertEquals("Android Studio", document.education[1].degree)
        assertEquals("CIBERTEC", document.education[1].institution)
        assertEquals("Miguel Angel Pacheco", document.references.single().name)
    }

    @Test
    fun `maps experience columns from the legacy CVFree image PDF`() {
        val document = ResumeTextParser.parse(
            ResumePdfTextContent(
                fullText = """
                    PAUL
                    GUILLEN ACUÑA
                    INGENIERO DE SISTEMAS
                    SOBRE MÍ
                    Desarrollador Android Senior con +5 años de experiencia.
                    EXPERIENCIA LABORAL
                """.trimIndent(),
                headerText = """
                    PAUL
                    9 Lima, Lima, 07051, Peru, Lima
                    GUILLEN ACUÑA
                    R+51977588534
                    M paulguillen190@gmail.com
                    INGENIERO DE SISTEMAS
                """.trimIndent(),
                sidebarText = """
                    SOBRE MÍ
                    Desarrollador Android Senior con +5 años de experiencia.
                """.trimIndent(),
                mainText = """
                    PAUL
                    9 Lima, Lima, 07051, Peru, Lima
                    GUILLEN ACUÑA
                    R+51977588534
                    M paulguillen190@gmail.com
                    INGENIERO DE SISTEMAS
                    EXPERIENCIA LABORAL
                    SYNOPSIS
                    Android Engineer
                    Lima
                    2025 - Actualidad
                    BIM
                    Desarrollo de la billetera digital BIM en Android con Kotlin.
                    Mobile Developer - APP
                    zOLUXIONES
                    LATAM
                    Scotiabank
                    Lima
                    2025 - 2026
                    Desarrollo de aplicaciones nativas en Android Studio con Kotlin.
                    INDRA
                    Software Engineer - APP
                    Telefónica del Perú
                    2024 - 9025
                    Jerarquía de ventas para aplicaciones retail.
                    Engineer - APP
                    NTT DATA
                    Lima
                    Scotiabank
                    2022 - 2024
                    Desarrollo de aplicaciones nativas en Android Studio con Kotlin y Java.
                    Telefónica del Perú
                    Lideré el desarrollo de aplicaciones nativas con arquitecturas escalables.
                    EDUCACIÓN
                """.trimIndent(),
                templateId = ResumeTemplateCatalog.BUDAPEST_ID,
            ),
        )

        assertEquals(ResumeTemplateCatalog.BUDAPEST_ID, document.templateName)
        assertEquals("Paul", document.contact.name)
        assertEquals("Guillen Acuña", document.contact.lastName)
        assertEquals("Ingeniero de Sistemas", document.contact.professionalTitle)
        assertTrue(document.summary.startsWith("Desarrollador Android Senior"))
        assertEquals(4, document.experiences.size)
        assertEquals("Android Engineer", document.experiences[0].position)
        assertEquals("SYNOPSIS", document.experiences[0].company)
        assertEquals("2025", document.experiences[0].startYear)
        assertTrue(document.experiences[0].isCurrent)
        assertTrue(document.experiences[0].achievements.startsWith("BIM"))
        assertEquals("Mobile Developer - APP", document.experiences[1].position)
        assertEquals("ZOLUXIONES LATAM", document.experiences[1].company)
        assertEquals("2025", document.experiences[1].startYear)
        assertEquals("2026", document.experiences[1].endYear)
        assertTrue(document.experiences[1].achievements.startsWith("Scotiabank"))
        assertEquals("Software Engineer - APP", document.experiences[2].position)
        assertEquals("INDRA", document.experiences[2].company)
        assertEquals("2024", document.experiences[2].startYear)
        assertEquals("2025", document.experiences[2].endYear)
        assertEquals("Engineer - APP", document.experiences[3].position)
        assertEquals("NTT DATA", document.experiences[3].company)
        assertEquals("2022", document.experiences[3].startYear)
        assertEquals("2024", document.experiences[3].endYear)
    }

    @Test
    fun `maps the remaining sections from the legacy CVFree image PDF`() {
        val sidebar = """
            SOBRE MÍ
            Desarrollador Android Senior con +5 años de experiencia en el desarrollo de aplicaciones nativas con Kotlin y Java en Android Studio. .ESpeote ecializado en arquitecturas modernas como MWM y Clean Architecture, integración de Jetpack Compose, XML, Firebase, Retrofit y gestión de estados con MVI. He liderado proyectos en sectores de banca y telecomunicaciones, asequrando se calidad, escalabilidad y excelente experiencia de usuario. Experiencia con Cl/ CD (Jenkins, Azure DevOps) y metodologías ágiles (Scrum). Además, Cuento con conocimie onocimientos en desarrollo web (Angular) y backend (Node.js, Java 17), así como experiencia en IN el desarrollo de aplicaciones Li no híbridas utilizando lonic, React Native y Flutter.
            ENLACES
            GitHub:
            https://github.com/paulguillen?t=a
            LinkedIn:
            https://www.linkedin.com/in/paul-
            IDIOMAS
            ESPAÑOL
            INGLES
            REFERENCIAS
            JHOSSY JHOSSEP VARGAS
            Scotiabank
            T: 932937506
            C: jhossvs2299 @gmail.com
            MIGUEL ANGEL PACHECO
            Indra Company
            T: +51 943603363
            C: mapacheco @indracompany.com
            ARTEMIO GUERRERO SANTA CRUZ
            Telefónica del Perí
            T: +51 995429498
            C: artemio.guerrero@telefonica.com
            RENZO BERECHE
            NTT DATA
            T: 935320924
            C: rbereche@emeal.nttdata.com
        """.trimIndent()
        val main = """
            HABILIDADES
            aVDI (JENKINSAZURE-DOCKER-KUBERNETES)
            CLEAN ARCHITECTURE GOoGLE ANALYTIcs
            JETPACK cOMPOSE ROOM/SQLITE REACT NATIVE UNIT TESTING SONARQUBE
            REACT EXPO TYPESCRIPT JAVASCRIPT MONGODB NEW RELIC KOIN/HILT POWER BI
            ANGULAR JAVA GRAPHQLRETROFIT FIREBASE JASMINE PYTHON REACTJS
            JACOCO DEVOPS NODEJS KARMA
            KOTLIN NEXTUS KMP sCRUM MVM
            IONIC DART XMIL
            MVISQL GIT
            CURSOS
            • Desarrollo Web - React JS
            IDAT
            2024 - 2025
            IDAT
            Python for Data Analyst
            2023 - 2023
            IDAT
            Desarrollo App - React Expo
            2023 - 2023
            SCRUM STUDY
            Scrum Fundamentals Certfied
            2022 - 2022
            Google Activate
            GOOGLE
            2021 - 2021
            sCRUM STUDY
            Scrum Fundamentals Certfied
            2022 - 2022
            cONECTA
            Desarrollo de Apps Móviles
            EMPLEO
            2021 - 2021
            AUTÓNOMA DEL
            Business Analytics con Power BI
            ONOM
            PERÚ
            2021 - 2021
            EDUCACIÓN
            UNIVERSIDAD
            Bachiller Ingeniería de Sistemas
            AUTÓNOMA DEL
            PERÚ
            Lima
            Android Studio
            CIBERTEC
            Lima
        """.trimIndent()

        val document = ResumeTextParser.parse(
            ResumePdfTextContent(
                fullText = "$sidebar\n$main",
                sidebarText = sidebar,
                mainText = main,
                templateId = ResumeTemplateCatalog.BUDAPEST_ID,
            ),
        )

        assertEquals(2, document.education.size)
        assertEquals("UNIVERSIDAD AUTÓNOMA DEL PERÚ", document.education[0].institution)
        assertEquals("Bachiller Ingeniería de Sistemas", document.education[0].degree)
        assertEquals("Lima", document.education[0].city)
        assertEquals("CIBERTEC", document.education[1].institution)
        assertEquals("Android Studio", document.education[1].degree)
        assertEquals(
            listOf(
                "CI/DI (JENKINS-AZURE-DOCKER-KUBERNETES)", "CLEAN ARCHITECTURE", "GOOGLE ANALYTICS",
                "JETPACK COMPOSE", "ROOM/SQLITE", "REACT NATIVE", "UNIT TESTING", "SONARQUBE",
                "REACT EXPO", "TYPESCRIPT", "JAVASCRIPT", "MONGODB", "NEW RELIC", "KOIN/HILT", "POWER BI",
                "ANGULAR", "JAVA", "GRAPHQL", "RETROFIT", "FIREBASE", "JASMINE", "PYTHON", "REACTJS",
                "JACOCO", "DEVOPS", "NODEJS", "KARMA", "KOTLIN", "NEXTJS", "KMP", "SCRUM", "MVVM",
                "IONIC", "DART", "XML", "MVI", "SQL", "GIT",
            ),
            document.skills,
        )
        assertEquals(8, document.courses.size)
        assertEquals("Desarrollo Web - React JS", document.courses[0].name)
        assertEquals("IDAT", document.courses[0].institution)
        assertEquals("CONECTA EMPLEO", document.courses[6].institution)
        assertEquals("AUTÓNOMA DEL PERÚ", document.courses[7].institution)
        assertEquals("Business Analytics con Power BI", document.courses[7].name)
        assertEquals(2, document.links.size)
        assertEquals(listOf("ESPAÑOL", "INGLES"), document.languages.map { it.language })
        assertEquals(4, document.references.size)
        assertEquals("jhossvs2299@gmail.com", document.references[0].email)
        assertEquals("mapacheco@indracompany.com", document.references[1].email)
        assertEquals("Telefónica del Perú", document.references[2].company)
        assertTrue(document.summary.contains("Especializado en arquitecturas modernas como MVVM"))
        assertTrue(document.summary.contains("asegurando calidad"))
        assertTrue(document.summary.contains("experiencia en el desarrollo de aplicaciones no híbridas"))
    }

    @Test
    fun `preserves every field when decoding an embedded CVFree resume`() {
        val original = ResumeTextParser.parse(
            """
            PAUL GUILLEN ACUÑA
            Ingeniero de Sistemas
            paul@example.com

            EXPERIENCIA LABORAL
            Mobile Developer | Zoluxiones | Lima | 2024 - 2025
            Desarrollo con Kotlin.
            """.trimIndent(),
        ).copy(templateName = ResumeTemplateCatalog.RIGA_ID)

        val decoded = ResumeDocumentCodec.decodeOrNull(ResumeDocumentCodec.encode(original))

        assertEquals(original, decoded)
    }

    @Test
    fun `separates compact PDF skill rows into individual skills`() {
        val document = ResumeTextParser.parse(
            """
            PAUL GUILLEN
            Ingeniero de Sistemas

            HABILIDADES
            KARMA KOTLIN NEXTJS KMP SCRUM MVVM IONIC
            DART XML MVI SQL GIT
            """.trimIndent(),
        )

        assertEquals(
            listOf(
                "KARMA", "KOTLIN", "NEXTJS", "KMP", "SCRUM", "MVVM", "IONIC",
                "DART", "XML", "MVI", "SQL", "GIT",
            ),
            document.skills,
        )
    }

    @Test
    fun `keeps compound skills together when separating a compact row`() {
        val document = ResumeTextParser.parse(
            """
            PAUL GUILLEN
            Ingeniero de Sistemas

            HABILIDADES
            ANDROID STUDIO KOTLIN CLEAN ARCHITECTURE
            """.trimIndent(),
        )

        assertEquals(listOf("ANDROID STUDIO", "KOTLIN", "CLEAN ARCHITECTURE"), document.skills)
    }

    @Test
    fun `normalizes compact skills recovered from embedded PDF data`() {
        val skills = ResumeTextParser.normalizeImportedSkills(
            listOf(
                "KARMA KOTLIN NEXTJS KMP SCRUM MVVM IONIC",
                "DART XML MVI SQL GIT",
            ),
        )

        assertEquals(
            listOf(
                "KARMA", "KOTLIN", "NEXTJS", "KMP", "SCRUM", "MVVM", "IONIC",
                "DART", "XML", "MVI", "SQL", "GIT",
            ),
            skills,
        )
    }

    @Test
    fun `keeps all skills from a dense multi-row PDF grid`() {
        val skills = ResumeTextParser.normalizeImportedSkills(
            listOf(
                "KOTLIN JAVA TYPESCRIPT JAVASCRIPT DART MVVM",
                "CLEAN ARCHITECTURE JETPACK COMPOSE XML MVI KMP",
                "ROOM/SQLITE KOIN/HILT JACOCO RETROFIT IONIC",
                "FIREBASE SQL REACTJS NODEJS MONGODB PYTHON",
                "REACT NATIVE REACT EXPO GO GIT POWER BI NEXTJS",
                "ANGULAR DEVOPS CI/DI(JENKINS - AZURE - DOCKER - KUBERNETES)",
                "UNIT TESTING GRAPHQL NEW RELIC GOOGLE ANALYTICS",
                "SONARQUBE SCRUM KARMA JASMINE",
            ),
        )

        assertEquals(38, skills.size)
        assertTrue("REACT EXPO" in skills)
        assertTrue("NEW RELIC" in skills)
        assertTrue("GOOGLE ANALYTICS" in skills)
        assertTrue("CI/DI(JENKINS - AZURE - DOCKER - KUBERNETES)" in skills)
        assertEquals(listOf("SONARQUBE", "SCRUM", "KARMA", "JASMINE"), skills.takeLast(4))
    }

    @Test
    fun `reflows visual PDF lines into full width experience paragraphs`() {
        val document = ResumeTextParser.parse(
            """
            PAUL GUILLEN
            Ingeniero de Sistemas
            paul@example.com

            EXPERIENCIA LABORAL
            Mobile Developer | Zoluxiones | Lima | 2024 - 2025
            Desarrollo de aplicaciones nativas en Android Studio con
            Kotlin y Jetpack Compose.
            Disene estrategias para asegurar la escalabilidad
            y alineacion con los objetivos del negocio.
            """.trimIndent(),
        )

        assertEquals(
            "Desarrollo de aplicaciones nativas en Android Studio con Kotlin y Jetpack Compose.\n" +
                "Disene estrategias para asegurar la escalabilidad y alineacion con los objetivos del negocio.",
            document.experiences.single().achievements,
        )
    }

    @Test
    fun `keeps separate imported achievement list items while reflowing wrapped lines`() {
        val document = ResumeTextParser.parse(
            """
            PAUL GUILLEN
            Ingeniero de Sistemas
            paul@example.com

            EXPERIENCIA LABORAL
            Mobile Developer | Zoluxiones | Lima | 2024 - 2025
            - Implemente una arquitectura escalable con varios
            modulos y pruebas automatizadas
            - Reduje los tiempos de entrega del equipo
            """.trimIndent(),
        )

        assertEquals(
            "Implemente una arquitectura escalable con varios modulos y pruebas automatizadas\n" +
                "Reduje los tiempos de entrega del equipo",
            document.experiences.single().achievements,
        )
    }
}
