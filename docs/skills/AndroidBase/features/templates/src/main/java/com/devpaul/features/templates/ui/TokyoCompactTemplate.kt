package com.devpaul.features.templates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.features.templates.R
import com.devpaul.features.templates.ResumeTemplateCatalog

private val TokyoInk = Color(0xFF20242B)
private val TokyoMuted = Color(0xFF5E636D)
private val TokyoSection = Color(0xFFEAF2FF)
private val TokyoLinkInk = Color(0xFF425A7A)

@Composable
internal fun TokyoCompactTemplate(resume: ResumeDocument) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 42.dp, top = 36.dp, end = 42.dp, bottom = 36.dp),
    ) {
        TokyoHeader(resume)

        val showLinks = ResumeOptionalSection.Links in resume.optionalSections && resume.links.isNotEmpty()
        if (showLinks) {
            Spacer(Modifier.height(14.dp))
            TokyoLinks(resume)
        }

        if (resume.summary.isNotBlank()) {
            TokyoSectionGap()
            TokyoSectionTitle(stringResource(R.string.ui_about_me_d15068))
            TokyoFormattedBody(resume.summary, resume.summaryFormatting)
        }

        if (resume.experiences.isNotEmpty()) {
            TokyoSectionGap()
            TokyoSectionTitle(stringResource(R.string.ui_work_experience_1706e7))
            resume.experiences.forEachIndexed { index, experience ->
                TokyoEntry(
                    title = experience.position,
                    organization = experience.company,
                    location = experience.city,
                    date = tokyoDate(experience.startYear, experience.localizedEndYear()),
                    description = experience.achievements,
                    descriptionFormatting = experience.achievementsFormatting,
                )
                if (index != resume.experiences.lastIndex) Spacer(Modifier.height(11.dp))
            }
        }

        if (resume.education.isNotEmpty()) {
            TokyoSectionGap()
            TokyoSectionTitle(stringResource(R.string.ui_education_4e9a5f))
            resume.education.forEachIndexed { index, education ->
                TokyoEntry(
                    title = education.degree,
                    organization = education.institution,
                    location = education.city,
                    date = tokyoDate(education.startYear, education.endYear),
                )
                if (index != resume.education.lastIndex) Spacer(Modifier.height(9.dp))
            }
        }

        if (resume.skills.isNotEmpty()) {
            TokyoSectionGap()
            TokyoSectionTitle(stringResource(R.string.ui_skills_0794df))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                resume.skills.forEach { skill -> TokyoBulletLabel(skill) }
            }
        }

        val showLanguages = ResumeOptionalSection.Languages in resume.optionalSections && resume.languages.isNotEmpty()
        if (showLanguages) {
            TokyoSectionGap()
            TokyoSectionTitle(stringResource(R.string.ui_languages_c765ac))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                resume.languages.forEach { language ->
                    TokyoBulletLabel(
                        buildString {
                            append(language.language)
                            if (language.level.isNotBlank()) append(" (").append(language.level).append(")")
                        },
                    )
                }
            }
        }

        val showCourses = ResumeOptionalSection.Courses in resume.optionalSections && resume.courses.isNotEmpty()
        if (showCourses) {
            TokyoSectionGap()
            TokyoSectionTitle(stringResource(R.string.ui_courses_177a73))
            resume.courses.forEachIndexed { index, course ->
                TokyoEntry(
                    title = course.name,
                    organization = course.institution,
                    location = "",
                    date = tokyoDate(course.startDate, course.endDate),
                )
                if (index != resume.courses.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }

        val showReferences = ResumeOptionalSection.References in resume.optionalSections && resume.references.isNotEmpty()
        if (showReferences) {
            TokyoSectionGap()
            TokyoSectionTitle(stringResource(R.string.ui_references_9e4676))
            resume.references.forEachIndexed { index, reference ->
                Text(
                    text = listOf(reference.name, reference.company, reference.phone, reference.email)
                        .filter(String::isNotBlank)
                        .joinToString("  |  "),
                    color = TokyoInk,
                    fontSize = 6.8.sp,
                    lineHeight = 9.sp,
                )
                if (index != resume.references.lastIndex) Spacer(Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun TokyoHeader(resume: ResumeDocument) {
    val contact = resume.contact
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = listOf(contact.name, contact.lastName)
                .filter(String::isNotBlank)
                .joinToString(" ")
                .ifBlank { stringResource(R.string.ui_your_name_f308e7) }
                .uppercase(),
            color = TokyoInk,
            fontSize = 23.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
        if (contact.professionalTitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = contact.professionalTitle,
                color = TokyoInk,
                fontSize = 10.5.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
        val address = listOf(contact.address, contact.city)
            .filter(String::isNotBlank)
            .distinct()
            .joinToString(", ")
        val contactLine = listOf(contact.phone, contact.email, address)
            .filter(String::isNotBlank)
            .joinToString("   |   ")
        if (contactLine.isNotBlank()) {
            Spacer(Modifier.height(9.dp))
            Text(
                text = contactLine,
                color = TokyoMuted,
                fontSize = 6.7.sp,
                lineHeight = 8.5.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TokyoLinks(resume: ResumeDocument) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        resume.links.chunked(2).forEach { links ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                links.forEach { link ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(3.dp),
                        color = TokyoSection,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                modifier = Modifier.size(9.dp),
                                tint = TokyoLinkInk,
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = link.url.ifBlank { link.name },
                                color = TokyoLinkInk,
                                fontSize = 6.2.sp,
                                lineHeight = 7.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (links.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TokyoSectionTitle(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(23.dp)
            .background(TokyoSection, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TokyoInk,
            fontSize = 9.5.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun TokyoEntry(
    title: String,
    organization: String,
    location: String,
    date: String,
    description: String = "",
    descriptionFormatting: ResumeTextFormatting = ResumeTextFormatting(),
) {
    Text(
        text = listOf(title, organization, location, date)
            .filter(String::isNotBlank)
            .joinToString("  |  "),
        color = TokyoInk,
        fontSize = 7.sp,
        lineHeight = 9.sp,
        fontWeight = FontWeight.Bold,
    )
    if (description.isNotBlank()) {
        Spacer(Modifier.height(4.dp))
        ExperienceDescription(
            text = description,
            formatting = descriptionFormatting,
            color = TokyoInk,
            fontSize = 6.6.sp,
            lineHeight = 9.2.sp,
        )
    }
}

@Composable
private fun TokyoFormattedBody(text: String, formatting: ResumeTextFormatting) {
    Text(
        text = formattedText(text, formatting),
        modifier = Modifier.fillMaxWidth(),
        color = TokyoInk,
        fontSize = 6.6.sp,
        lineHeight = 9.2.sp,
    )
}

@Composable
private fun TokyoBulletLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(3.5.dp)
                .background(TokyoInk, CircleShape),
        )
        Spacer(Modifier.width(4.dp))
        Text(text, color = TokyoInk, fontSize = 6.5.sp, lineHeight = 8.sp)
    }
}

@Composable
private fun TokyoSectionGap() {
    Spacer(Modifier.height(13.dp))
}

private fun tokyoDate(start: String, end: String): String =
    listOf(start, end).filter(String::isNotBlank).joinToString(" - ")

@Preview(name = "Tokio", showBackground = true)
@Composable
private fun TokyoCompactTemplatePreview() {
    ResumeTemplateDocument(
        resume = previewDocument().copy(templateName = ResumeTemplateCatalog.TOKYO_COMPACT_ID),
        modifier = Modifier.fillMaxWidth(),
    )
}
