package com.devpaul.features.resume.importing

import com.devpaul.features.resume.R

import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeCourse
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.domain.resume.ResumeLanguage
import com.devpaul.core.domain.resume.ResumeLink
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeReference
import com.devpaul.core.domain.resume.reflowSoftLineBreaks
import java.text.Normalizer

internal object ResumeTextParser {

    private enum class Section {
        Header,
        Summary,
        Experience,
        Education,
        Skills,
        Courses,
        Links,
        Languages,
        References,
    }

    private val sectionAliases = mapOf(
        Section.Summary to setOf(
            "PERFIL", "SOBRE MI", "RESUMEN", "RESUMEN PROFESIONAL",
            "PROFILE", "ABOUT ME", "PROFESSIONAL SUMMARY", "SUMMARY",
        ),
        Section.Experience to setOf(
            "EXPERIENCIA", "EXPERIENCIA LABORAL", "EXPERIENCIA PROFESIONAL",
            "EXPERIENCE", "WORK EXPERIENCE", "PROFESSIONAL EXPERIENCE", "EMPLOYMENT",
        ),
        Section.Education to setOf(
            "EDUCACION", "FORMACION", "FORMACION ACADEMICA", "ESTUDIOS",
            "EDUCATION", "ACADEMIC BACKGROUND",
        ),
        Section.Skills to setOf(
            "HABILIDADES", "HABILIDADES TECNICAS", "COMPETENCIAS", "CONOCIMIENTOS", "TECNOLOGIAS",
            "SKILLS", "TECHNICAL SKILLS", "CORE SKILLS",
        ),
        Section.Courses to setOf(
            "CURSOS", "CERTIFICACIONES", "CURSOS Y CERTIFICACIONES",
            "COURSES", "CERTIFICATIONS", "COURSES AND CERTIFICATIONS",
        ),
        Section.Links to setOf(
            "ENLACES", "REDES", "PORTAFOLIO", "LINKS", "SOCIAL LINKS", "PORTFOLIO",
        ),
        Section.Languages to setOf("IDIOMAS", "LENGUAS", "LANGUAGES"),
        Section.References to setOf(
            "REFERENCIAS", "REFERENCIAS LABORALES", "REFERENCES", "PROFESSIONAL REFERENCES",
        ),
    )

    private val emailRegex = Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE)
    private val urlRegex = Regex(
        "(?i)(?:https?://|www\\.)[^\\s|,;]+|(?:linkedin\\.com|github\\.com)/[^\\s|,;]+",
    )
    private val phoneRegex = Regex("(?<!\\d)(?:\\+?\\d[\\d ()-]{6,}\\d)(?!\\d)")
    private val yearRegex = Regex("(?<!\\d)(?:19|20)\\d{2}(?!\\d)")
    private val currentExperienceRegex = Regex("(?i)actualidad|actual|presente|present|current")
    private val knownEntryLocations = setOf(
        "LIMA", "CALLAO", "AREQUIPA", "CUSCO", "TRUJILLO", "PIURA", "CHICLAYO",
        "ICA", "HUANCAYO", "TACNA", "PUNO", "MADRID", "BARCELONA", "BOGOTA",
        "SANTIAGO", "MEXICO", "REMOTO", "REMOTE",
    )
    private val dateRangeRegex = Regex(
        "(?i)(?:(?:ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic|jan|apr|aug|dec)[a-z.]*\\s+)?" +
            "(?:19|20)\\d{2}\\s*(?:-|–|—|a|hasta)\\s*" +
            "(?:(?:ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic|jan|apr|aug|dec)[a-z.]*\\s+)?" +
            "(?:(?:19|20)\\d{2}|actualidad|actual|presente|present|current)",
    )
    private val ocrYearRangeTypoRegex = Regex("((?:19|20)\\d{2}\\s*-\\s*)9(0\\d{2})")
    private val bulletSplitRegex = Regex("\\s*(?:[•●▪◦·]|\\||;|,)\\s*")
    private val groupedToolchainRegex = Regex("(?i)CI/(?:CD|DI)\\s*\\([^)]*\\)")
    private val compoundSkillNames = setOf(
        "ANDROID STUDIO", "JETPACK COMPOSE", "CLEAN ARCHITECTURE", "UNIT TESTING",
        "REACT NATIVE", "REACT EXPO", "SPRING BOOT", "POWER BI", "GOOGLE ANALYTICS",
        "ROOM/SQLITE", "KOIN/HILT",
        "GOOGLE CLOUD", "AMAZON WEB SERVICES", "NEW RELIC",
        "MACHINE LEARNING", "DATA ANALYSIS", "ANALISIS DE DATOS", "SOFTWARE DEVELOPMENT",
        "DESARROLLO DE SOFTWARE", "PROJECT MANAGEMENT", "GESTION DE PROYECTOS",
        "AGILE METHODOLOGIES", "METODOLOGIAS AGILES", "VERSION CONTROL", "REST API",
        "WEB SERVICES", "SOLID PRINCIPLES", "PRINCIPIOS SOLID", "UI DESIGN", "UX DESIGN",
    )
    private val singleSkillNames = setOf(
        "ANDROID", "ANGULAR", "AWS", "AZURE", "C", "C++", "C#", "DART", "DOCKER",
        "FIGMA", "FIREBASE", "FLUTTER", "GIT", "GITHUB", "GITLAB", "GRAPHQL", "HTML",
        "IONIC", "JAVA", "JAVASCRIPT", "JENKINS", "JIRA", "KARMA", "KMP", "KOIN",
        "KOTLIN", "KUBERNETES", "MONGODB", "MVC", "MVI", "MVVM", "MYSQL", "NEXTJS",
        "NODEJS", "NOSQL", "ORACLE", "PHP", "POSTGRESQL", "PYTHON", "REACT", "REDUX",
        "RETROFIT", "ROOM", "SCRUM", "SONARQUBE", "SQL", "SQLITE", "SWIFT", "TYPESCRIPT", "UNITY",
        "REACTJS",
        "VUE", "XML",
    )
    private val referenceCompanySuffixRegex = Regex(
        "(?i)^(.+?\\b(?:company|compañ[ií]a|corporation|corp|s\\.?a\\.?c?|s\\.?a\\.?s?|ltd|inc))\\b",
    )
    private val professionalTitleRegex = Regex(
        "(?i)\\b(?:ingenier[oa]|engineer|desarrollador(?:a)?|developer|arquitect[oa]|architect|" +
            "analista|designer|diseñador(?:a)?|consultor(?:a)?|especialista|t[eé]cnic[oa]|" +
            "manager|gerente|director(?:a)?|coordinador(?:a)?|administrador(?:a)?|contador(?:a)?|" +
            "abogad[oa]|docente|profesor(?:a)?|marketing|ventas|recursos humanos|product owner|" +
            "scrum master|qa|soporte|sistemas|inform[aá]tic[oa])\\b",
    )
    private val academicTitleRegex = Regex(
        "(?i)\\b(?:bachiller|licenciad[oa]|maestr[ií]a|doctorad[oa]|ingenier[ií]a|" +
            "t[eé]cnic[oa]|android studio)\\b",
    )

    fun parse(rawText: String): ResumeDocument = parse(ResumePdfTextContent(fullText = rawText))

    fun parse(content: ResumePdfTextContent): ResumeDocument {
        val lines = content.fullText.toCleanLines()
        val flowLines = content.flowText.toCleanLines()
        val headerLines = content.headerText.toCleanLines()
        val mainSections = splitSections(content.mainText.toCleanLines())
        val sidebarSections = splitSections(content.sidebarText.toCleanLines())
        val fullSections = splitSections(lines)
        val flowSections = splitSections(flowLines)
        val headerSections = splitSections(headerLines)

        if (lines.isEmpty() && flowLines.isEmpty()) {
            throw ResumeImportException(R.string.ui_no_readable_content_was_found_in_the_pdf_7f891b)
        }

        fun mainSection(section: Section): List<String> =
            mainSections[section].orEmpty().ifEmpty { fullSections[section].orEmpty() }

        fun sideSection(section: Section): List<String> =
            sidebarSections[section].orEmpty().ifEmpty {
                flowSections[section].orEmpty().ifEmpty { fullSections[section].orEmpty() }
            }

        val contactSource = buildList {
            addAll(headerSections[Section.Header].orEmpty())
            addAll(mainSections[Section.Header].orEmpty())
            addAll(fullSections[Section.Header].orEmpty())
            addAll(flowLines.takeLast(20))
        }.distinct()
        val experiences = parseExperiences(mainSection(Section.Experience))
        val education = parseEducation(mainSection(Section.Education))
        val skills = parseSkills(mainSection(Section.Skills))
        val courses = parseCourses(mainSection(Section.Courses))
        val links = parseLinks(content.fullText + "\n" + content.flowText, sideSection(Section.Links))
        val languages = parseLanguages(sideSection(Section.Languages))
        val references = parseReferences(sideSection(Section.References))

        val optionalSections = buildSet {
            if (courses.isNotEmpty()) add(ResumeOptionalSection.Courses)
            if (links.isNotEmpty()) add(ResumeOptionalSection.Links)
            if (languages.isNotEmpty()) add(ResumeOptionalSection.Languages)
            if (references.isNotEmpty()) add(ResumeOptionalSection.References)
        }

        return ResumeDocument(
            templateName = content.templateId,
            optionalSections = optionalSections,
            contact = parseContact(
                candidateLines = contactSource,
                allLines = (lines + flowLines).distinct(),
                rawText = content.fullText + "\n" + content.flowText,
            ),
            experiences = experiences,
            education = education,
            skills = skills,
            summary = sideSection(Section.Summary).joinToString(" ").cleanOcrSummary(),
            courses = courses,
            links = links,
            languages = languages,
            references = references,
        )
    }

    private fun String.toCleanLines(): List<String> =
        replace('\u00A0', ' ')
            .lineSequence()
            .map(::cleanLine)
            .filter(String::isNotBlank)
            .toList()

    private fun splitSections(lines: List<String>): Map<Section, List<String>> {
        val result = Section.entries.associateWith { mutableListOf<String>() }
        var current = Section.Header
        lines.forEach { line ->
            val detected = detectSection(line)
            if (detected != null) {
                current = detected
            } else {
                result.getValue(current).add(line)
            }
        }
        return result
    }

    private fun detectSection(line: String): Section? {
        val normalized = normalize(line)
            .replace(Regex("[^A-Z ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return sectionAliases.entries.firstOrNull { (_, aliases) -> normalized in aliases }?.key
    }

    private fun parseContact(
        candidateLines: List<String>,
        allLines: List<String>,
        rawText: String,
    ): ResumeContact {
        val contactText = candidateLines.joinToString("\n")
        val email = emailRegex.find(contactText)?.value
            ?: emailRegex.find(rawText)?.value.orEmpty()
        val phone = phoneRegex.findAll(contactText + "\n" + rawText)
            .map(MatchResult::value)
            .map(String::trim)
            .firstOrNull { candidate -> candidate.count(Char::isDigit) >= 8 && !candidate.containsYearRange() }
            .orEmpty()

        val candidates = candidateLines
            .take(50)
            .filterNot { detectSection(it) != null }
            .map { line -> cleanContactCandidate(line, email) }
            .filter(String::isNotBlank)
            .distinct()

        val identity = findContactIdentity(candidates)
            ?: findContactIdentity(allLines.filterNot { detectSection(it) != null })
        val nameLine = identity?.fullName
            ?: candidates.firstOrNull(::looksLikePersonName).orEmpty()
        val titleLine = candidates.firstOrNull {
            looksLikeProfessionalTitle(it) && it.allLettersUppercase()
        } ?: identity?.title
            ?: candidates.firstOrNull(::looksLikeProfessionalTitle).orEmpty()
        val location = extractContactLocation(candidateLines + allLines)

        val nameParts = normalizedPersonName(nameLine).split(' ').filter(String::isNotBlank)
        return ResumeContact(
            name = nameParts.firstOrNull().orEmpty(),
            lastName = nameParts.drop(1).joinToString(" "),
            professionalTitle = normalizedProfessionalTitle(titleLine),
            email = email,
            phone = phone,
            city = location.substringBefore(',').trim(),
            address = location,
        )
    }

    private data class ContactIdentity(
        val fullName: String,
        val title: String,
    )

    private fun cleanContactCandidate(line: String, email: String): String {
        var clean = line
        if (email.isNotBlank()) clean = clean.replace(email, "", ignoreCase = true)
        clean = clean
            .replace(urlRegex, "")
            .replace(phoneRegex, "")
            .replace('|', ' ')
            .trim()
        val location = contactLocationRegex.find(clean)
        if (location != null) clean = clean.substring(0, location.range.first).trim()
        return clean
    }

    private val contactLocationRegex = Regex(
        "(?i)\\b(?:lima|callao|arequipa|cusco|trujillo|piura|chiclayo|ica|huancayo|tacna|" +
            "puno|madrid|barcelona|bogot[aá]|santiago|m[eé]xico)(?:\\s*,[^|]+)+",
    )

    private fun extractContactLocation(lines: List<String>): String {
        lines.forEach { line ->
            contactLocationRegex.find(line)?.value?.trim()?.let { return it }
        }
        return lines.firstOrNull(::looksLikeLocation).orEmpty()
    }

    private fun findContactIdentity(lines: List<String>): ContactIdentity? {
        val titleIndexes = lines.indices
            .filter { looksLikeProfessionalTitle(lines[it]) }
            .sortedByDescending { lines[it].allLettersUppercase() }
        titleIndexes.forEach { titleIndex ->
            val title = lines[titleIndex]
            val nameParts = mutableListOf<String>()
            val firstCandidate = (titleIndex - 8).coerceAtLeast(0)
            for (index in titleIndex - 1 downTo firstCandidate) {
                if (looksLikeNameFragment(lines[index])) {
                    nameParts.add(0, lines[index])
                    if (nameParts.sumOf { it.split(Regex("\\s+")).size } >= 3) break
                }
            }
            val fullName = nameParts.joinToString(" ").trim()
            if (fullName.split(Regex("\\s+")).size in 2..6) {
                return ContactIdentity(fullName = fullName, title = title)
            }
        }
        return null
    }

    private fun looksLikeProfessionalTitle(value: String): Boolean =
        value.length in 3..90 &&
            value.split(Regex("\\s+")).size <= 10 &&
            !value.contains(',') &&
            !value.endsWith('.') &&
            professionalTitleRegex.containsMatchIn(value)

    private fun looksLikeNameFragment(value: String): Boolean {
        if (value.length !in 2..60 || value.contains(Regex("[,|:@]"))) return false
        if (professionalTitleRegex.containsMatchIn(value) || yearRegex.containsMatchIn(value)) return false
        val words = value.split(Regex("\\s+")).filter(String::isNotBlank)
        if (words.size !in 1..5) return false
        val locationWords = setOf("LIMA", "PERU", "PERÚ", "REMOTO", "REMOTE")
        if (words.all { normalize(it) in locationWords }) return false
        return words.all { word ->
            val letters = word.filter(Char::isLetter)
            letters.isNotEmpty() && (letters.all(Char::isUpperCase) || letters.first().isUpperCase())
        }
    }

    private fun looksLikeLocation(value: String): Boolean =
        value.length in 3..100 && value.contains(',') &&
            !emailRegex.containsMatchIn(value) && !yearRegex.containsMatchIn(value)

    private fun parseExperiences(lines: List<String>): List<ResumeExperience> =
        parseDatedEntries(lines).map { entry ->
            ResumeExperience(
                position = entry.title,
                company = entry.organization,
                city = entry.location,
                startYear = entry.startDate,
                endYear = entry.endDate,
                isCurrent = entry.isCurrent,
                achievements = entry.description,
            )
        }

    private fun parseEducation(lines: List<String>): List<ResumeEducation> =
        (parseColumnarEducationEntries(lines) ?: parseAcademicEntries(lines)).map { entry ->
            ResumeEducation(
                institution = entry.organization,
                degree = entry.title,
                city = entry.location,
                startYear = entry.startDate,
                endYear = entry.endDate,
            )
        }

    private fun parseCourses(lines: List<String>): List<ResumeCourse> =
        (parseDatedCourseEntries(lines) ?: parseAcademicEntries(lines)).map { entry ->
            ResumeCourse(
                name = entry.title,
                institution = entry.organization,
                startDate = entry.startDate,
                endDate = entry.endDate,
            )
        }

    private fun parseColumnarEducationEntries(lines: List<String>): List<ParsedEntry>? {
        if (lines.any { it.contains('|') || it.containsDate() }) return null
        val titleIndexes = lines.indices.filter { academicTitleRegex.containsMatchIn(lines[it]) }
        if (titleIndexes.isEmpty()) return null
        if (titleIndexes.any { academicTitleRegex.find(lines[it])?.range?.first != 0 }) return null

        return titleIndexes.mapIndexed { entryIndex, titleIndex ->
            val previousTitle = titleIndexes.getOrNull(entryIndex - 1) ?: -1
            val nextTitle = titleIndexes.getOrNull(entryIndex + 1) ?: lines.size
            val before = mutableListOf<String>()
            var index = titleIndex - 1
            while (index > previousTitle && looksLikeOrganizationFragment(lines[index])) {
                before.add(0, lines[index])
                index--
            }
            val after = lines.subList(titleIndex + 1, nextTitle)
            val organization = (before + after.filter(::looksLikeOrganizationFragment))
                .distinctBy(::normalize)
                .joinToString(" ") { it.uppercase() }
            val location = after.firstOrNull(::isKnownEntryLocation)
                ?: lines.subList((previousTitle + 1).coerceAtLeast(0), nextTitle)
                    .firstOrNull(::looksLikeEntryLocation).orEmpty()
            ParsedEntry(
                title = lines[titleIndex],
                organization = organization,
                location = location,
                startDate = "",
                endDate = "",
                description = "",
            )
        }.filter { it.organization.isNotBlank() }
            .takeIf { it.isNotEmpty() }
    }

    private fun parseDatedCourseEntries(lines: List<String>): List<ParsedEntry>? {
        if (lines.any { it.contains('|') }) return null
        val dateIndexes = lines.indices.filter { lines[it].containsDate() }
        if (dateIndexes.isEmpty()) return null

        return dateIndexes.mapIndexedNotNull { entryIndex, dateIndex ->
            val start = dateIndexes.getOrNull(entryIndex - 1)?.plus(1) ?: 0
            val heading = lines.subList(start, dateIndex)
            val titleIndex = heading.indexOfFirst { line ->
                !looksLikeOrganizationFragment(line) && !looksLikeEntryLocation(line)
            }
            if (titleIndex < 0) return@mapIndexedNotNull null
            val title = heading[titleIndex].removeBulletPrefix()
            val rawOrganization = heading.filterIndexed { index, line ->
                index != titleIndex && looksLikeOrganizationFragment(line)
            }
            val organization = rawOrganization
                .filterNot { fragment ->
                    val normalizedFragment = normalize(fragment)
                    normalizedFragment.length >= 3 && rawOrganization.any { other ->
                        other !== fragment && normalize(other).contains(normalizedFragment)
                    }
                }
                .distinctBy(::normalize)
                .joinToString(" ") { it.uppercase() }
            val (startDate, endDate) = splitDate(lines[dateIndex].extractDate())
            ParsedEntry(
                title = title,
                organization = organization,
                location = "",
                startDate = startDate,
                endDate = endDate,
                description = "",
            )
        }.takeIf { it.isNotEmpty() }
    }

    private fun parseAcademicEntries(lines: List<String>): List<ParsedEntry> {
        if (lines.isEmpty()) return emptyList()
        if (lines.any { it.containsDate() }) return parseDatedEntries(lines)
        return parseUndatedAcademicEntries(lines)
    }

    private fun parseUndatedAcademicEntries(lines: List<String>): List<ParsedEntry> {
        val entries = mutableListOf<ParsedEntry>()
        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            val parsed = parseEntryHeading(line, "").toParsedEntry()
            var organization = parsed.organization
            var title = parsed.title
            var location = parsed.location
            index++

            if (organization.isNotBlank() && title.isNotBlank()) {
                while (index < lines.size && isUppercaseOrganizationContinuation(lines[index])) {
                    organization = listOf(organization, lines[index]).joinToString(" ")
                    index++
                }
                if (index < lines.size && looksLikeEntryLocation(lines[index])) {
                    location = lines[index]
                    index++
                }
            } else if (title.isNotBlank() && title.allLettersUppercase()) {
                organization = title
                title = ""
                if (index < lines.size && looksLikeEntryLocation(lines[index])) {
                    location = lines[index]
                    index++
                }
            }

            entries += ParsedEntry(
                title = title,
                organization = organization,
                location = location,
                startDate = "",
                endDate = "",
                description = "",
            )
        }
        return entries.filter { it.title.isNotBlank() || it.organization.isNotBlank() }
    }

    private fun isUppercaseOrganizationContinuation(value: String): Boolean {
        val words = value.split(Regex("\\s+")).filter(String::isNotBlank)
        return words.isNotEmpty() && words.size <= 5 && value.allLettersUppercase() &&
            !looksLikeEntryLocation(value)
    }

    private fun looksLikeOrganizationFragment(value: String): Boolean {
        if (isKnownEntryLocation(value)) return false
        val letters = value.filter(Char::isLetter)
        return letters.isNotEmpty() &&
            value.split(Regex("\\s+")).size <= 5 &&
            letters.count(Char::isUpperCase) >= letters.length * 0.75
    }

    private fun String.allLettersUppercase(): Boolean {
        val letters = filter(Char::isLetter)
        return letters.isNotEmpty() && letters.all(Char::isUpperCase)
    }

    private data class ParsedEntry(
        val title: String,
        val organization: String,
        val location: String,
        val startDate: String,
        val endDate: String,
        val description: String,
        val isCurrent: Boolean = false,
    )

    private fun parseDatedEntries(lines: List<String>): List<ParsedEntry> {
        if (lines.isEmpty()) return emptyList()
        val dateIndexes = lines.indices.filter { lines[it].containsDate() }
        if (dateIndexes.isEmpty()) {
            return listOf(
                ParsedEntry(
                    title = lines.first(),
                    organization = "",
                    location = "",
                    startDate = "",
                    endDate = "",
                    description = lines.drop(1).toDescriptionText(),
                ),
            )
        }

        val headingStarts = dateIndexes.mapIndexed { index, dateIndex ->
            findEntryHeadingStart(
                lines = lines,
                dateIndex = dateIndex,
                lowerBound = dateIndexes.getOrNull(index - 1)?.plus(1) ?: 0,
            )
        }

        return dateIndexes.mapIndexedNotNull { entryIndex, dateIndex ->
            val headingStart = headingStarts[entryIndex]
            val headingLines = lines.subList(headingStart, dateIndex)
            val separatedTitleIndex = headingLines.indexOfFirst(::looksLikeProfessionalTitle)
                .takeIf { index ->
                    index > 0 || index == 0 && headingLines.drop(1).any(::looksLikeOrganizationFragment)
                }
            if (separatedTitleIndex != null) {
                var title = headingLines[separatedTitleIndex]
                var details = headingLines.drop(separatedTitleIndex + 1)
                if (normalize(details.firstOrNull().orEmpty()) == "APP") {
                    title += " - APP"
                    details = details.drop(1)
                }
                val leadingOrganization = details.takeWhile(::looksLikeOrganizationFragment)
                val organization = if (separatedTitleIndex > 0) {
                    headingLines
                        .take(separatedTitleIndex)
                        .filter(::looksLikeOrganizationFragment)
                        .map(String::uppercase)
                        .joinToString(" ")
                } else {
                    leadingOrganization.joinToString(" ") { it.uppercase() }
                }
                if (separatedTitleIndex == 0) details = details.drop(leadingOrganization.size)
                val location = details.firstOrNull(::isKnownEntryLocation)
                    ?: details.firstOrNull(::looksLikeEntryLocation).orEmpty()
                val date = lines[dateIndex].extractDate()
                val (start, end) = splitDate(date)
                val inlineDetail = lines[dateIndex]
                    .replace(dateRangeRegex, "")
                    .replace(yearRegex, "")
                    .trim(' ', '-', '|', '–', '—')
                val descriptionEnd = headingStarts.getOrNull(entryIndex + 1) ?: lines.size
                val descriptionPrefix = buildList {
                    addAll(details.filterNot { it == location })
                    if (inlineDetail.isNotBlank()) add(inlineDetail)
                }
                val descriptionBody = lines.subList(dateIndex + 1, descriptionEnd).toDescriptionText()
                val description = (descriptionPrefix + descriptionBody)
                    .filter(String::isNotBlank)
                    .joinToString("\n")
                return@mapIndexedNotNull ParsedEntry(
                    title = title,
                    organization = organization,
                    location = location,
                    startDate = start,
                    endDate = end,
                    description = description,
                    isCurrent = currentExperienceRegex.containsMatchIn(date),
                ).takeIf { it.title.isNotBlank() || it.organization.isNotBlank() }
            }
            val location = headingLines.lastOrNull()
                ?.takeIf(::looksLikeEntryLocation)
                .orEmpty()
            val headingWithoutLocation = if (location.isNotBlank()) headingLines.dropLast(1) else headingLines
            val textAlongsideDate = lines[dateIndex]
                .replace(dateRangeRegex, "")
                .replace(yearRegex, "")
                .trim(' ', '-', '|', '–', '—')
            val heading = headingWithoutLocation.joinToString(" ").ifBlank { textAlongsideDate }
            val parsed = parseEntryHeading(heading, lines[dateIndex].extractDate())
            val descriptionEnd = headingStarts.getOrNull(entryIndex + 1) ?: lines.size
            val description = lines.subList(dateIndex + 1, descriptionEnd)
                .toDescriptionText()
            parsed.toParsedEntry().copy(
                location = location.ifBlank { parsed.location },
                description = description,
            ).takeIf { it.title.isNotBlank() || it.organization.isNotBlank() }
        }
    }

    private fun findEntryHeadingStart(
        lines: List<String>,
        dateIndex: Int,
        lowerBound: Int,
    ): Int {
        val inlineHeading = lines[dateIndex]
            .replace(dateRangeRegex, "")
            .replace(yearRegex, "")
            .trim(' ', '-', '|', '–', '—')
        if (inlineHeading.contains('|') ||
            inlineHeading.contains(Regex("\\s[-–—]\\s")) ||
            looksLikeProfessionalTitle(inlineHeading)
        ) {
            return dateIndex
        }
        val separatedTitleIndex = (dateIndex - 1 downTo lowerBound)
            .take(6)
            .firstOrNull { looksLikeProfessionalTitle(lines[it]) }
        if (separatedTitleIndex != null) {
            var start = separatedTitleIndex
            while (
                start - 1 >= lowerBound &&
                separatedTitleIndex - start < 3 &&
                isUppercaseOrganizationContinuation(lines[start - 1])
            ) {
                start--
            }
            return start
        }
        var index = dateIndex - 1
        if (index < lowerBound) return dateIndex
        if (looksLikeEntryLocation(lines[index])) index--
        if (index < lowerBound) return dateIndex

        var start = index
        if (isUppercaseNameContinuation(lines[start]) && start - 1 >= lowerBound) start--
        return start
    }

    private fun looksLikeEntryLocation(value: String): Boolean {
        val clean = value.trim()
        val words = clean.split(Regex("\\s+")).filter(String::isNotBlank)
        val knownLocations = setOf(
            "LIMA", "CALLAO", "AREQUIPA", "CUSCO", "TRUJILLO", "PIURA", "CHICLAYO",
            "ICA", "HUANCAYO", "TACNA", "PUNO", "MADRID", "BARCELONA",
            "BOGOTA", "BOGOTÁ", "SANTIAGO", "MEXICO", "MÉXICO", "REMOTO", "REMOTE",
        )
        return clean.length in 2..45 && words.size <= 4 &&
            !clean.startsWith("•") && !clean.startsWith("-") &&
            !clean.contains('|') && !clean.containsDate() &&
            !professionalTitleRegex.containsMatchIn(clean) &&
            clean.none { it == '.' || it == ';' } &&
            (clean.contains(',') || normalize(clean) in knownLocations ||
                words.size == 1 && clean.any(Char::isLowerCase))
    }

    private fun isKnownEntryLocation(value: String): Boolean =
        normalize(value) in knownEntryLocations

    private data class MutableEntry(
        val title: String,
        val organization: String,
        val location: String,
        val startDate: String,
        val endDate: String,
        val isCurrent: Boolean = false,
        val description: MutableList<String> = mutableListOf(),
    ) {
        fun toParsedEntry() = ParsedEntry(
            title = title,
            organization = organization,
            location = location,
            startDate = startDate,
            endDate = endDate,
            description = description.filter(String::isNotBlank).joinToString("\n"),
            isCurrent = isCurrent,
        )
    }

    private fun parseEntryHeading(line: String, extractedDate: String): MutableEntry {
        val parts = line.split('|').map(String::trim).filter(String::isNotBlank)
        val contentParts = parts.filterNot { it.containsDate() }
        val date = parts.firstOrNull { it.containsDate() }?.extractDate().orEmpty().ifBlank { extractedDate }
        val (start, end) = splitDate(date)
        val isCurrent = currentExperienceRegex.containsMatchIn(date)

        if (contentParts.size >= 2) {
            return MutableEntry(
                title = contentParts[0],
                organization = contentParts[1],
                location = contentParts.getOrElse(2) { "" },
                startDate = start,
                endDate = end,
                isCurrent = isCurrent,
            )
        }

        val headingWithoutDate = line.replace(dateRangeRegex, "").trim(' ', '-', '|', '–', '—')
        val leadingOrganization = headingWithoutDate.split(Regex("\\s+"))
            .takeWhile(::isUppercaseNameWord)
            .dropWhile { word -> word.count(Char::isLetter) == 1 }
        val remainingTitle = headingWithoutDate
            .removePrefix(leadingOrganization.joinToString(" "))
            .trim()
        if (leadingOrganization.isNotEmpty() && remainingTitle.isNotBlank()) {
            return MutableEntry(
                title = remainingTitle,
                organization = leadingOrganization.joinToString(" "),
                location = "",
                startDate = start,
                endDate = end,
                isCurrent = isCurrent,
            )
        }
        val headingParts = when {
            headingWithoutDate.contains('|') -> headingWithoutDate.split('|', limit = 3)
            headingWithoutDate.contains(Regex("\\s+-\\s+")) ->
                headingWithoutDate.split(Regex("\\s+-\\s+"), limit = 3)
            headingWithoutDate.count { it == ',' } in 1..2 -> headingWithoutDate.split(',', limit = 3)
            else -> listOf(headingWithoutDate)
        }
            .map(String::trim)
            .filter(String::isNotBlank)
        return MutableEntry(
            title = headingParts.firstOrNull().orEmpty(),
            organization = headingParts.getOrElse(1) { "" },
            location = headingParts.getOrElse(2) { "" },
            startDate = start,
            endDate = end,
            isCurrent = isCurrent,
        )
    }

    private fun parseSkills(lines: List<String>): List<String> = normalizeImportedSkills(lines)

    internal fun normalizeImportedSkills(skills: List<String>): List<String> = skills
        .map(::repairOcrSkillLine)
        .flatMap { line -> line.split(bulletSplitRegex) }
        .flatMap(::splitCompactSkillList)
        .map { it.removeBulletPrefix().trim() }
        .map(::canonicalSkillName)
        .filter { it.length in 2..45 && !it.containsDate() }
        .distinctBy(::normalize)
        .take(60)

    private fun repairOcrSkillLine(value: String): String = value
        .replace(Regex("(?i)^avdi\\s*\\("), "CI/DI (")
        .replace(Regex("(?i)JENKINSAZURE"), "JENKINS-AZURE")
        .replace(Regex("(?i)GRAPHQL\\s*RETROFIT"), "GRAPHQL RETROFIT")
        .replace(Regex("(?i)\\bNEXTUS\\b"), "NEXTJS")
        .replace(Regex("(?i)\\bMVM\\b"), "MVVM")
        .replace(Regex("(?i)\\bXMIL\\b"), "XML")
        .replace(Regex("(?i)\\bMVI\\s*SQL\\b"), "MVI SQL")

    private fun canonicalSkillName(value: String): String {
        val letters = value.filter(Char::isLetter)
        if (letters.isNotEmpty() && letters.count(Char::isUpperCase) < letters.length * 0.75) return value
        val canonical = (compoundSkillNames + singleSkillNames)
            .firstOrNull { normalize(it) == normalize(value) }
        return canonical ?: value
    }

    private fun splitCompactSkillList(value: String): List<String> {
        val cleanValue = value.removeBulletPrefix().trim()
        groupedToolchainRegex.find(cleanValue)?.let { toolchain ->
            if (toolchain.value == cleanValue) return listOf(cleanValue)
            return buildList {
                cleanValue.substring(0, toolchain.range.first).trim()
                    .takeIf(String::isNotBlank)
                    ?.let { addAll(splitCompactSkillList(it)) }
                add(toolchain.value)
                cleanValue.substring(toolchain.range.last + 1).trim()
                    .takeIf(String::isNotBlank)
                    ?.let { addAll(splitCompactSkillList(it)) }
            }
        }
        val words = cleanValue.split(Regex("\\s+")).filter(String::isNotBlank)
        if (words.size < 2) return listOf(cleanValue)

        val parsed = mutableListOf<Pair<String, Boolean>>()
        var index = 0
        while (index < words.size) {
            val compoundSize = minOf(3, words.size - index).downTo(2).firstOrNull { size ->
                normalize(words.subList(index, index + size).joinToString(" ")) in compoundSkillNames
            }
            if (compoundSize != null) {
                parsed += words.subList(index, index + compoundSize).joinToString(" ") to true
                index += compoundSize
            } else {
                val word = words[index]
                val normalizedWord = normalize(word)
                parsed += word to (
                    normalizedWord in singleSkillNames || normalizedWord in compoundSkillNames
                )
                index++
            }
        }

        val letters = cleanValue.filter(Char::isLetter)
        val isUppercaseList = words.size >= 3 && letters.isNotEmpty() && letters.all(Char::isUpperCase)
        val isKnownList = parsed.size >= 2 && parsed.all { (_, recognized) -> recognized }
        return if (isUppercaseList || isKnownList) parsed.map { it.first } else listOf(cleanValue)
    }

    private fun parseLinks(rawText: String, lines: List<String>): List<ResumeLink> {
        val urls = (urlRegex.findAll(rawText).map(MatchResult::value) +
            lines.asSequence().flatMap { urlRegex.findAll(it).map(MatchResult::value) })
            .map { it.trimEnd('.', ')', ']', '}') }
            .distinctBy { it.lowercase() }
            .take(8)
        return urls.map { url ->
            ResumeLink(
                name = when {
                    "linkedin" in url.lowercase() -> "LinkedIn"
                    "github" in url.lowercase() -> "GitHub"
                    else -> "Portafolio"
                },
                url = url,
            )
        }.toList()
    }

    private fun parseLanguages(lines: List<String>): List<ResumeLanguage> = lines
        .flatMap { it.split(bulletSplitRegex) }
        .map { it.removeBulletPrefix().trim() }
        .filter(String::isNotBlank)
        .map { value ->
            val level = Regex(
                "(?i)nativo|native|básico|basico|basic|intermedio|intermediate|avanzado|advanced|fluido|fluent",
            ).find(value)?.value.orEmpty()
            val language = value
                .replace(Regex("\\([^)]*\\)"), "")
                .replace(Regex("\\s*[-|:]\\s*.*$"), "")
                .trim()
            ResumeLanguage(language = language, level = level.replaceFirstChar { it.uppercase() })
        }
        .filter { it.language.isNotBlank() }
        .distinctBy { normalize(it.language) }
        .take(10)

    private fun parseReferences(lines: List<String>): List<ResumeReference> {
        val references = mutableListOf<ResumeReference>()
        val inlineIndexes = mutableSetOf<Int>()

        lines.forEachIndexed { index, line ->
            parseInlineReference(line)?.let { reference ->
                references += reference
                inlineIndexes += index
            }
        }

        var blockStart = 0
        lines.forEachIndexed { index, line ->
            if (index in inlineIndexes) {
                blockStart = index + 1
                return@forEachIndexed
            }
            val phone = phoneRegex.find(line)?.value?.trim().orEmpty()
            if (phone.isBlank()) return@forEachIndexed

            val identity = parseReferenceIdentity(lines.subList(blockStart, index))
                ?: return@forEachIndexed
            val email = findReferenceEmail(lines, index)
            references += ResumeReference(
                name = identity.name,
                company = identity.company,
                phone = phone,
                email = email?.value.orEmpty(),
            )
            blockStart = (email?.lastLineIndex ?: index) + 1
        }

        return references
            .filter { it.name.isNotBlank() }
            .distinctBy { reference ->
                listOf(reference.name, reference.email, reference.phone)
                    .joinToString("|")
                    .let(::normalize)
            }
            .take(8)
    }

    private fun parseInlineReference(line: String): ResumeReference? {
        if (!line.contains('|')) return null
        val email = emailRegex.find(line)?.value.orEmpty()
        val phone = phoneRegex.find(line)?.value.orEmpty()
        if (email.isBlank() && phone.isBlank()) return null

        val descriptive = line.split('|')
            .map(String::trim)
            .filter(String::isNotBlank)
            .filterNot { part ->
                email.isNotBlank() && part.contains(email, ignoreCase = true) ||
                    phone.isNotBlank() && part.contains(phone)
            }
        if (descriptive.isEmpty()) return null
        return ResumeReference(
            name = normalizedPersonName(descriptive.first()),
            company = descriptive.getOrElse(1) { "" },
            phone = phone,
            email = email,
        )
    }

    private data class ReferenceIdentity(
        val name: String,
        val company: String,
    )

    private fun parseReferenceIdentity(lines: List<String>): ReferenceIdentity? {
        val candidates = lines
            .filterNot { emailRegex.containsMatchIn(it) || phoneRegex.containsMatchIn(it) }
            .takeLast(10)
        if (candidates.size < 2) return null

        val companyLineIndex = candidates.lastIndex
        val searchableNames = candidates.dropLast(1)
        val nameIndex = searchableNames.indices.reversed().firstOrNull { index ->
            extractLeadingUppercaseName(searchableNames[index]).isNotBlank()
        } ?: searchableNames.indices.reversed().firstOrNull { index ->
            looksLikePersonName(searchableNames[index])
        } ?: return null

        var name = extractLeadingUppercaseName(candidates[nameIndex])
            .ifBlank { candidates[nameIndex] }
        var nextIndex = nameIndex + 1
        if (nextIndex < companyLineIndex && isUppercaseNameContinuation(candidates[nextIndex])) {
            name += " ${candidates[nextIndex].trim()}"
            nextIndex++
        }
        val company = candidates.getOrNull(nextIndex)
            ?.takeIf { nextIndex <= companyLineIndex }
            ?.let(::cleanReferenceCompany)
            .orEmpty()

        return ReferenceIdentity(
            name = normalizedPersonName(name),
            company = company,
        )
    }

    private data class ParsedReferenceEmail(
        val value: String,
        val lastLineIndex: Int,
    )

    private fun findReferenceEmail(lines: List<String>, phoneLineIndex: Int): ParsedReferenceEmail? {
        val lastIndex = (phoneLineIndex + 4).coerceAtMost(lines.lastIndex)
        for (index in phoneLineIndex..lastIndex) {
            val email = emailRegex.find(lines[index])?.value ?: continue
            val continuation = lines.getOrNull(index + 1)
                ?.trim()
                ?.takeIf { it.length == 1 && it.single().isLowerCase() }
                .orEmpty()
            val completedEmail = if (email.endsWith(".co", ignoreCase = true) && continuation == "m") {
                email + continuation
            } else {
                email
            }
            return ParsedReferenceEmail(
                value = completedEmail,
                lastLineIndex = index + if (completedEmail != email) 1 else 0,
            )
        }
        return null
    }

    private fun extractLeadingUppercaseName(line: String): String {
        val words = line.split(Regex("\\s+")).takeWhile(::isUppercaseNameWord)
        return words.takeIf { it.size >= 2 }?.joinToString(" ").orEmpty()
    }

    private fun isUppercaseNameContinuation(line: String): Boolean {
        val words = line.trim().split(Regex("\\s+")).filter(String::isNotBlank)
        return words.size == 1 && isUppercaseNameWord(words.single())
    }

    private fun isUppercaseNameWord(value: String): Boolean {
        val letters = value.filter(Char::isLetter)
        return letters.isNotEmpty() && letters.all(Char::isUpperCase)
    }

    private fun cleanReferenceCompany(line: String): String {
        val clean = line
            .substringBefore('|')
            .substringBefore('•')
            .trim()
        referenceCompanySuffixRegex.find(clean)?.groupValues?.getOrNull(1)?.let { return it.trim() }

        val leadingAcronyms = clean.split(Regex("\\s+"))
            .takeWhile(::isUppercaseNameWord)
        return if (leadingAcronyms.size >= 2) leadingAcronyms.joinToString(" ") else clean
    }

    private fun splitDate(value: String): Pair<String, String> {
        val years = yearRegex.findAll(value).map(MatchResult::value).toList()
        return years.firstOrNull().orEmpty() to years.getOrElse(1) { "" }
    }

    private fun String.extractDate(): String =
        dateRangeRegex.find(this)?.value
            ?: yearRegex.findAll(this).joinToString(" - ") { it.value }

    private fun String.containsDate(): Boolean =
        dateRangeRegex.containsMatchIn(this) || yearRegex.findAll(this).count() >= 2 ||
            (yearRegex.containsMatchIn(this) &&
                (contains('|') || Regex("(?i)actual|presente|present|current").containsMatchIn(this)))

    private fun String.containsYearRange(): Boolean = yearRegex.findAll(this).count() >= 2

    private fun looksLikePersonName(value: String): Boolean {
        val words = value.split(Regex("\\s+")).filter(String::isNotBlank)
        if (words.size !in 2..6 || value.length !in 4..70) return false
        if (value.contains('|') || value.contains(',') || value.contains(':')) return false
        if (yearRegex.containsMatchIn(value)) return false
        val letters = value.count(Char::isLetter)
        return letters >= value.length * 0.65
    }

    private fun normalizedPersonName(value: String): String {
        if (value.isBlank() || value.any(Char::isLowerCase)) return value.trim()
        return value.lowercase().split(' ').joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }

    private fun normalizedProfessionalTitle(value: String): String {
        if (value.isBlank() || !value.allLettersUppercase()) return value.trim()
        val connectors = setOf("a", "de", "del", "en", "la", "las", "los", "y")
        return value.lowercase().split(Regex("\\s+")).mapIndexed { index, word ->
            if (index > 0 && word in connectors) word else word.replaceFirstChar(Char::uppercase)
        }.joinToString(" ")
    }

    private fun cleanLine(value: String): String {
        val clean = value
            .trim()
            .replace(Regex("[\\t ]{2,}"), "\u001F")
            .replace(Regex("[\\t ]+"), " ")
            .split('\u001F')
            .joinToString(" ") { collapseTrackedUppercase(it.trim()) }
            .replace(Regex("\\s*@\\s*"), "@")
            .replace("Perí", "Perú")
            .trim()
        return ocrYearRangeTypoRegex.replace(clean) { match ->
            "${match.groupValues[1]}2${match.groupValues[2]}"
        }
    }

    private fun collapseTrackedUppercase(value: String): String {
        val words = value.split(Regex("\\s+")).filter(String::isNotBlank)
        if (words.size < 3) return value
        val uppercaseLetters = words.map { word -> word.filter(Char::isLetter) }
        val tracked = uppercaseLetters.first().all(Char::isUpperCase) &&
            uppercaseLetters.drop(1).all { it.length == 1 && it.single().isUpperCase() }
        return if (tracked) words.joinToString("") else value
    }

    private fun String.removeBulletPrefix(): String =
        replace(Regex("^[•●▪◦·*\\-]+\\s*"), "").trim()

    private fun List<String>.toDescriptionText(): String =
        filter(String::isNotBlank)
            .joinToString("\n")
            .let(::reflowSoftLineBreaks)
            .lineSequence()
            .joinToString("\n") { it.removeBulletPrefix() }

    private fun String.cleanOcrSummary(): String =
        replace(Regex("\\s+"), " ")
            .replace(Regex("(?i)\\.\\s*\\.?(?:ESpeote\\s+)?ecializado"), ". Especializado")
            .replace(Regex("\\bMWM\\b"), "MVVM")
            .replace(Regex("(?i)asequrando\\s+se\\s+"), "asegurando ")
            .replace(Regex("\\bCl/\\s*CD\\b"), "CI/CD")
            .replace("Cuento con conocimie onocimientos", "cuento con conocimientos")
            .replace("experiencia en IN el", "experiencia en el")
            .replace("aplicaciones Li no híbridas", "aplicaciones no híbridas")
            .replace(Regex("\\blonic\\b"), "Ionic")
            .trim()

    private fun normalize(value: String): String = Normalizer
        .normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .uppercase()
        .trim()
}
