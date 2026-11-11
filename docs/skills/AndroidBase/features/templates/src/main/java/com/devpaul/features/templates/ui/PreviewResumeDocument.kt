package com.devpaul.features.templates.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeCourse
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.domain.resume.ResumeLanguage
import com.devpaul.core.domain.resume.ResumeLink
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeReference
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.core.platform.locale.AppLanguageManager

@Composable
internal fun rememberPreviewDocument(templateId: String): ResumeDocument {
    val language = AppLanguageManager.current(LocalContext.current)
    return remember(templateId, language) {
        previewDocument(language).copy(templateName = templateId)
    }
}

internal fun previewDocument(language: AppLanguage = AppLanguage.Spanish): ResumeDocument {
    val english = language == AppLanguage.English
    return ResumeDocument(
        optionalSections = setOf(
            ResumeOptionalSection.Courses,
            ResumeOptionalSection.Links,
            ResumeOptionalSection.Languages,
            ResumeOptionalSection.References,
        ),
        contact = ResumeContact(
            name = "Elena",
            lastName = "Vargas",
            professionalTitle = if (english) "Android Developer" else "Desarrolladora Android",
            email = "elena.vargas@example.com",
            phone = "+51 900 000 000",
            city = "Arequipa",
            address = if (english) "Arequipa, Peru" else "Arequipa, Perú",
        ),
        summary = if (english) {
            "Senior Android developer experienced in building native, scalable, and maintainable applications with Kotlin and Jetpack Compose."
        } else {
            "Desarrollador Android Senior con experiencia creando aplicaciones nativas, escalables y mantenibles con Kotlin y Jetpack Compose."
        },
        links = listOf(
            ResumeLink("LinkedIn", "https://linkedin.com/in/elena-vargas-demo"),
            ResumeLink("GitHub", "https://github.com/elena-vargas-demo"),
            ResumeLink(if (english) "Portfolio" else "Portafolio", "https://portfolio.example.com"),
        ),
        languages = listOf(
            ResumeLanguage(if (english) "Spanish" else "Español", if (english) "Native" else "Nativo"),
            ResumeLanguage(if (english) "English" else "Inglés", if (english) "Intermediate" else "Intermedio"),
        ),
        references = listOf(
            ResumeReference(
                "Camila Rojas",
                if (english) "Technical Lead" else "Líder Técnica",
                "+51 900 000 001",
                "camila.rojas@example.com",
            ),
            ResumeReference("Diego Fernandez", "Nexo Digital", "+51 900 000 002", "diego.fernandez@example.com"),
        ),
        experiences = listOf(
            ResumeExperience(
                position = "Mobile Developer - APP",
                company = "Nexo Digital",
                city = "Arequipa",
                startYear = "2022",
                endYear = if (english) "Present" else "Actualidad",
                achievements = if (english) {
                    "Native application development with Kotlin and clean architecture. Implementation of new features and automated tests."
                } else {
                    "Desarrollo de aplicaciones nativas con Kotlin y arquitectura limpia. Implementación de nuevas funcionalidades y pruebas automatizadas."
                },
            ),
        ),
        skills = listOf("Kotlin", "Android", "Jetpack Compose", "Clean Architecture", "Git"),
        courses = listOf(
            ResumeCourse(if (english) "Advanced Android" else "Android Avanzado", "IDAT", "2024", "2025"),
        ),
        education = listOf(
            ResumeEducation(
                institution = if (english) "National University" else "Universidad Nacional",
                degree = if (english) "Systems Engineering" else "Ingeniería de Sistemas",
                city = "Arequipa",
                startYear = "2016",
                endYear = "2021",
            ),
        ),
    )
}
