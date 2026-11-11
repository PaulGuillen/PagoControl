package com.devpaul.features.templates.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.features.templates.R
import com.devpaul.features.templates.ResumeTemplateCatalog

private val PragueInk = Color(0xFF38383A)
private val PragueMuted = Color(0xFF66666A)
private val PragueLine = Color(0xFF505054)
private val PragueTrack = Color(0xFFBCBCC0)

@Composable
internal fun PragueEditorialTemplate(resume: ResumeDocument) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 40.dp, top = 36.dp, end = 40.dp, bottom = 36.dp),
    ) {
        PragueHeader(resume)
        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = PragueLine, thickness = 0.8.dp)
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            PragueSidebar(
                resume = resume,
                modifier = Modifier
                    .weight(0.33f)
                    .padding(end = 20.dp),
            )
            Box(
                modifier = Modifier
                    .width(0.8.dp)
                    .fillMaxHeight()
                    .background(PragueLine),
            )
            PragueMainContent(
                resume = resume,
                modifier = Modifier
                    .weight(0.67f)
                    .padding(start = 24.dp),
            )
        }
    }
}

@Composable
private fun PragueHeader(resume: ResumeDocument) {
    val contact = resume.contact
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Column(
            modifier = Modifier
                .weight(0.43f)
                .padding(end = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = listOf(contact.name, contact.lastName)
                    .filter(String::isNotBlank)
                    .joinToString(" ")
                    .ifBlank { stringResource(R.string.ui_your_name_f308e7) }
                    .uppercase(),
                color = PragueInk,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            PragueDiamondDivider()
            if (contact.professionalTitle.isNotBlank()) {
                Spacer(Modifier.height(9.dp))
                Text(
                    text = contact.professionalTitle.uppercase(),
                    color = PragueMuted,
                    fontSize = 7.6.sp,
                    lineHeight = 9.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(15.dp))
            PragueContactRow(Icons.Outlined.Email, contact.email)
            PragueContactRow(Icons.Outlined.Phone, contact.phone)
            PragueContactRow(
                Icons.Outlined.LocationOn,
                listOf(contact.address, contact.city)
                    .filter(String::isNotBlank)
                    .distinct()
                    .joinToString(", "),
            )
        }
        Box(
            modifier = Modifier
                .width(0.8.dp)
                .fillMaxHeight()
                .background(PragueLine),
        )
        Column(
            modifier = Modifier
                .weight(0.57f)
                .padding(start = 24.dp),
        ) {
            PragueHeaderTitle(stringResource(R.string.ui_about_me_d15068))
            if (resume.summary.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = formattedText(resume.summary, resume.summaryFormatting),
                    color = PragueMuted,
                    fontSize = 7.2.sp,
                    lineHeight = 10.5.sp,
                )
            }
        }
    }
}

@Composable
private fun PragueSidebar(resume: ResumeDocument, modifier: Modifier = Modifier) {
    val showLinks = ResumeOptionalSection.Links in resume.optionalSections && resume.links.isNotEmpty()
    val showLanguages = ResumeOptionalSection.Languages in resume.optionalSections && resume.languages.isNotEmpty()

    Column(modifier = modifier) {
        if (resume.education.isNotEmpty()) {
            PragueSectionTitle(stringResource(R.string.ui_education_4e9a5f))
            resume.education.forEachIndexed { index, education ->
                Text(
                    text = education.degree.uppercase(),
                    color = PragueInk,
                    fontSize = 7.1.sp,
                    lineHeight = 8.8.sp,
                    fontWeight = FontWeight.Bold,
                )
                PragueItalicText(
                    listOf(education.institution, education.city)
                        .filter(String::isNotBlank)
                        .joinToString(" | "),
                )
                PragueSmallText(pragueDate(education.startYear, education.endYear))
                if (index != resume.education.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }

        if (showLinks) {
            if (resume.education.isNotEmpty()) PragueSideSectionGap()
            PragueSectionTitle(stringResource(R.string.ui_links_d38518))
            resume.links.forEachIndexed { index, link ->
                Text(
                    text = link.name.replaceFirstChar { it.uppercase() },
                    color = PragueInk,
                    fontSize = 7.sp,
                    lineHeight = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (link.url.isNotBlank()) {
                    Text(
                        text = link.url,
                        color = PragueMuted,
                        fontSize = 6.3.sp,
                        lineHeight = 8.2.sp,
                        textDecoration = TextDecoration.Underline,
                    )
                }
                if (index != resume.links.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }

        if (showLanguages) {
            if (resume.education.isNotEmpty() || showLinks) {
                PragueSideSectionGap()
            }
            PragueSectionTitle(stringResource(R.string.ui_languages_c765ac))
            resume.languages.forEachIndexed { index, language ->
                PragueProgressBar(
                    label = language.language.uppercase(),
                    progress = languageProficiency(language.level),
                )
                if (language.level.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    PragueSmallText(language.level)
                }
                if (index != resume.languages.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PragueMainContent(resume: ResumeDocument, modifier: Modifier = Modifier) {
    val showCourses = ResumeOptionalSection.Courses in resume.optionalSections && resume.courses.isNotEmpty()
    val showReferences = ResumeOptionalSection.References in resume.optionalSections && resume.references.isNotEmpty()

    Column(modifier = modifier) {
        if (resume.experiences.isNotEmpty()) {
            PragueSectionTitle(stringResource(R.string.ui_experience_0c1122))
            resume.experiences.forEachIndexed { index, experience ->
                PragueMainEntry(
                    title = experience.position,
                    organization = experience.company,
                    location = experience.city,
                    date = pragueDate(experience.startYear, experience.localizedEndYear()),
                    description = experience.achievements,
                    descriptionFormatting = experience.achievementsFormatting,
                )
                if (index != resume.experiences.lastIndex) Spacer(Modifier.height(13.dp))
            }
        }

        if (resume.skills.isNotEmpty()) {
            if (resume.experiences.isNotEmpty()) PragueMainSectionGap()
            PragueSectionTitle(stringResource(R.string.ui_skills_0794df))
            resume.skills.chunked(2).forEachIndexed { rowIndex, skills ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    skills.forEach { skill ->
                        PragueProgressBar(
                            label = skill.uppercase(),
                            progress = 1f,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (skills.size == 1) Spacer(Modifier.weight(1f))
                }
                if (rowIndex != resume.skills.chunked(2).lastIndex) Spacer(Modifier.height(8.dp))
            }
        }

        if (showCourses) {
            if (resume.experiences.isNotEmpty() || resume.skills.isNotEmpty()) PragueMainSectionGap()
            PragueSectionTitle(stringResource(R.string.ui_courses_177a73))
            resume.courses.forEachIndexed { index, course ->
                PragueMainEntry(
                    title = course.name,
                    organization = course.institution,
                    location = "",
                    date = pragueDate(course.startDate, course.endDate),
                )
                if (index != resume.courses.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }

        if (showReferences) {
            if (resume.experiences.isNotEmpty() || resume.skills.isNotEmpty() || showCourses) {
                PragueMainSectionGap()
            }
            PragueSectionTitle(stringResource(R.string.ui_references_9e4676))
            resume.references.chunked(2).forEachIndexed { rowIndex, references ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    references.forEach { reference ->
                        PragueReference(
                            name = reference.name,
                            company = reference.company,
                            phone = reference.phone,
                            email = reference.email,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (references.size == 1) Spacer(Modifier.weight(1f))
                }
                if (rowIndex != resume.references.chunked(2).lastIndex) Spacer(Modifier.height(13.dp))
            }
        }
    }
}

@Composable
private fun PragueMainEntry(
    title: String,
    organization: String,
    location: String,
    date: String,
    description: String = "",
    descriptionFormatting: ResumeTextFormatting = ResumeTextFormatting(),
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(title.uppercase())
            }
            val workplace = listOf(organization, location)
                .filter(String::isNotBlank)
                .joinToString(" | ")
            if (workplace.isNotBlank()) {
                append(" | ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(workplace.uppercase())
                }
            }
            if (date.isNotBlank()) {
                append(" | ")
                append(date)
            }
        },
        color = PragueInk,
        fontSize = 7.2.sp,
        lineHeight = 9.sp,
    )
    if (description.isNotBlank()) {
        Spacer(Modifier.height(5.dp))
        ExperienceDescription(
            text = description,
            formatting = descriptionFormatting,
            modifier = Modifier.fillMaxWidth(),
            color = PragueMuted,
            fontSize = 6.7.sp,
            lineHeight = 9.7.sp,
        )
    }
}

@Composable
private fun PragueReference(
    name: String,
    company: String,
    phone: String,
    email: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(0.7.dp, PragueLine),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Contacts,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = PragueInk,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = name.uppercase(),
            color = PragueInk,
            fontSize = 6.8.sp,
            lineHeight = 8.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        PragueCenteredSmallText(company)
        PragueCenteredSmallText(phone, "T:")
        PragueCenteredSmallText(email, stringResource(R.string.resume_email_prefix))
    }
}

@Composable
private fun PragueDiamondDivider() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(0.8.dp).background(PragueLine))
        Box(
            Modifier
                .size(7.dp)
                .rotate(45f)
                .background(Color.White)
                .border(0.8.dp, PragueLine),
        )
    }
}

@Composable
private fun PragueContactRow(icon: ImageVector, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(9.dp),
            tint = PragueInk,
        )
        Spacer(Modifier.width(6.dp))
        Text(value, color = PragueMuted, fontSize = 6.5.sp, lineHeight = 8.sp)
    }
}

@Composable
private fun PragueHeaderTitle(text: String) {
    Text(
        text = text,
        color = PragueInk,
        fontSize = 10.5.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
private fun PragueSectionTitle(text: String) {
    PragueHeaderTitle(text)
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = PragueLine, thickness = 0.7.dp)
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun PragueProgressBar(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, color = PragueMuted, fontSize = 6.5.sp, lineHeight = 8.sp)
        Spacer(Modifier.height(2.5.dp))
        Box(Modifier.fillMaxWidth().height(2.5.dp).background(PragueTrack)) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(PragueInk),
            )
        }
    }
}

@Composable
private fun PragueItalicText(value: String) {
    if (value.isBlank()) return
    Text(
        text = value,
        color = PragueMuted,
        fontSize = 6.5.sp,
        lineHeight = 8.2.sp,
        fontStyle = FontStyle.Italic,
    )
}

@Composable
private fun PragueSmallText(value: String) {
    if (value.isBlank()) return
    Text(value, color = PragueMuted, fontSize = 6.2.sp, lineHeight = 8.sp)
}

@Composable
private fun PragueCenteredSmallText(value: String, prefix: String = "") {
    if (value.isBlank()) return
    Text(
        text = if (prefix.isBlank()) value else "$prefix $value",
        color = PragueMuted,
        fontSize = 6.1.sp,
        lineHeight = 8.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PragueSideSectionGap() {
    Spacer(Modifier.height(17.dp))
}

@Composable
private fun PragueMainSectionGap() {
    Spacer(Modifier.height(20.dp))
}

private fun pragueDate(start: String, end: String): String =
    listOf(start, end).filter(String::isNotBlank).joinToString(" - ")

@Preview(name = "Praga", showBackground = true)
@Composable
private fun PragueEditorialTemplatePreview() {
    ResumeTemplateDocument(
        resume = previewDocument().copy(templateName = ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID),
        modifier = Modifier.fillMaxWidth(),
    )
}
