package com.devpaul.features.templates.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeTextAlignment
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.reflowSoftLineBreaks
import com.devpaul.features.templates.R
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.ResumeTemplateRenderer

private val DocumentInk = Color(0xFF414143)
private val DocumentMuted = Color(0xFF73757A)
private val DocumentLine = Color(0xFF515154)
private val SidebarBackground = Color(0xFF414143)
private val SidebarText = Color(0xFFF8F8F8)
private val BudapestContentFontSize = 7.8.sp
private val BudapestContentLineHeight = 10.5.sp
private val BudapestSectionFontSize = 10.sp

internal const val RESUME_DESIGN_WIDTH_DP = 490
private val BudapestSidebarWidth = 180.dp
private val BudapestMainWidth = 310.dp

@Composable
fun ResumeTemplateDocument(
    resume: ResumeDocument,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD5D8DE)),
    ) {
        when (ResumeTemplateCatalog.find(resume.templateName).renderer) {
            ResumeTemplateRenderer.Budapest -> BudapestTemplate(resume)
            ResumeTemplateRenderer.Riga -> RigaTemplate(resume)
            ResumeTemplateRenderer.PragueEditorial -> PragueEditorialTemplate(resume)
            ResumeTemplateRenderer.TokyoCompact -> TokyoCompactTemplate(resume)
        }
    }
}

@Composable
private fun BudapestTemplate(resume: ResumeDocument) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = SidebarBackground,
                    size = size.copy(width = BudapestSidebarWidth.toPx()),
                )
            },
    ) {
        BudapestSidebar(
            resume = resume,
            modifier = Modifier.width(BudapestSidebarWidth),
        )
        ClassicMainContent(
            resume = resume,
            modifier = Modifier.width(BudapestMainWidth),
        )
    }
}

@Composable
private fun BudapestSidebar(
    resume: ResumeDocument,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(SidebarBackground)
            .padding(start = 18.dp, top = 26.dp, end = 18.dp, bottom = 26.dp),
    ) {
        val showLinks = ResumeOptionalSection.Links in resume.optionalSections && resume.links.isNotEmpty()
        val showLanguages = ResumeOptionalSection.Languages in resume.optionalSections && resume.languages.isNotEmpty()
        val showReferences = ResumeOptionalSection.References in resume.optionalSections && resume.references.isNotEmpty()

        if (resume.summary.isNotBlank()) {
            SidebarTitle(stringResource(R.string.ui_about_me_d15068))
            Text(
                text = formattedText(resume.summary, resume.summaryFormatting),
                color = SidebarText,
                fontSize = BudapestContentFontSize,
                lineHeight = BudapestContentLineHeight,
            )
            if (showLinks || showLanguages || showReferences) SidebarDivider()
        }
        if (showLinks) {
            SidebarTitle(stringResource(R.string.ui_links_d38518))
            resume.links.forEachIndexed { index, link ->
                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(fontSize = BudapestContentFontSize, fontWeight = FontWeight.Bold))
                        append(link.name.replaceFirstChar { it.uppercase() })
                        append(':')
                        pop()
                        append('\n')
                        pushStyle(SpanStyle(fontSize = BudapestContentFontSize, textDecoration = TextDecoration.Underline))
                        append(link.url)
                        pop()
                    },
                    color = SidebarText,
                    fontSize = BudapestContentFontSize,
                    lineHeight = BudapestContentLineHeight,
                )
                if (index != resume.links.lastIndex) Spacer(Modifier.height(7.dp))
            }
            if (showLanguages || showReferences) SidebarDivider()
        }
        if (showLanguages) {
            SidebarTitle(stringResource(R.string.ui_languages_c765ac))
            resume.languages.forEachIndexed { index, language ->
                Text(
                    text = language.language.uppercase(),
                    color = SidebarText,
                    fontSize = BudapestContentFontSize,
                    lineHeight = BudapestContentLineHeight,
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(Color.White.copy(alpha = 0.3f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(languageProficiency(language.level))
                            .fillMaxHeight()
                            .background(Color.White),
                    )
                }
                if (index != resume.languages.lastIndex) Spacer(Modifier.height(9.dp))
            }
            if (showReferences) SidebarDivider()
        }
        if (showReferences) {
            SidebarTitle(stringResource(R.string.ui_references_9e4676))
            resume.references.forEachIndexed { index, reference ->
                Text(
                    text = reference.name.uppercase(),
                    color = SidebarText,
                    fontSize = BudapestContentFontSize,
                    lineHeight = BudapestContentLineHeight,
                    fontWeight = FontWeight.Bold,
                )
                CompactSidebarText(reference.company)
                CompactSidebarText(reference.phone, prefix = "T:")
                CompactSidebarText(reference.email, prefix = stringResource(R.string.resume_email_prefix))
                if (index != resume.references.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ClassicMainContent(
    resume: ResumeDocument,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 24.dp),
    ) {
        ResumeHeader(resume)
        Spacer(Modifier.height(16.dp))

        if (resume.experiences.isNotEmpty()) {
            MainSectionTitle(stringResource(R.string.ui_work_experience_1706e7))
            resume.experiences.forEach { experience ->
                TimelineEntry(
                    organization = experience.company.uppercase(),
                    location = experience.city,
                    date = compactDate(experience.startYear, experience.localizedEndYear()),
                    title = experience.position,
                    description = experience.achievements,
                    descriptionFormatting = experience.achievementsFormatting,
                )
            }
        }
        if (resume.skills.isNotEmpty()) {
            SectionGap()
            MainSectionTitle(stringResource(R.string.ui_skills_0794df))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                resume.skills.forEach { skill -> SkillLabel(skill.uppercase()) }
            }
        }
        if (ResumeOptionalSection.Courses in resume.optionalSections && resume.courses.isNotEmpty()) {
            SectionGap()
            MainSectionTitle(stringResource(R.string.ui_courses_177a73))
            resume.courses.forEach { course ->
                TimelineEntry(
                    organization = course.institution.uppercase(),
                    location = "",
                    date = compactDate(course.startDate, course.endDate),
                    title = course.name,
                )
            }
        }
        if (resume.education.isNotEmpty()) {
            SectionGap()
            MainSectionTitle(stringResource(R.string.ui_education_4e9a5f))
            resume.education.forEach { education ->
                TimelineEntry(
                    organization = education.institution.uppercase(),
                    location = education.city,
                    date = compactDate(education.startYear, education.endYear),
                    title = education.degree,
                )
            }
        }
    }
}

@Composable
private fun ResumeHeader(resume: ResumeDocument) {
    val contact = resume.contact
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1.05f)) {
            Text(
                text = listOf(contact.name, contact.lastName)
                    .filter(String::isNotBlank)
                    .joinToString("\n")
                    .uppercase(),
                color = DocumentInk,
                fontSize = 16.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Black,
            )
            if (contact.professionalTitle.isNotBlank()) {
                Spacer(Modifier.height(1.5.dp))
                Text(
                    text = contact.professionalTitle.uppercase(),
                    color = DocumentInk,
                    fontSize = BudapestContentFontSize,
                    lineHeight = BudapestContentLineHeight,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(modifier = Modifier.weight(0.95f)) {
            val contactLines = listOf(
                Icons.Outlined.LocationOn to listOf(contact.address, contact.city)
                    .filter(String::isNotBlank)
                    .joinToString(", "),
                Icons.Outlined.Phone to contact.phone,
                Icons.Outlined.Email to contact.email,
            ).filter { it.second.isNotBlank() }
            contactLines.forEachIndexed { index, (icon, text) ->
                ContactLine(icon, text, showDivider = index < contactLines.lastIndex)
            }
        }
    }
}

@Composable
private fun ContactLine(icon: ImageVector, text: String, showDivider: Boolean) {
    if (text.isBlank()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(vertical = 1.5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .background(DocumentInk, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(9.dp), tint = Color.White)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                color = DocumentMuted,
                fontSize = BudapestContentFontSize,
                lineHeight = BudapestContentLineHeight,
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 19.dp),
                color = Color(0xFFE0E1E4),
                thickness = 0.6.dp,
            )
        }
    }
}

@Composable
private fun MainSectionTitle(text: String) {
    Text(text, color = DocumentInk, fontSize = BudapestSectionFontSize, fontWeight = FontWeight.Black)
    Spacer(Modifier.height(3.dp))
    HorizontalDivider(color = DocumentLine, thickness = 0.7.dp)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun TimelineEntry(
    organization: String,
    location: String,
    date: String,
    title: String,
    description: String = "",
    descriptionFormatting: ResumeTextFormatting = ResumeTextFormatting(),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(end = 4.dp, bottom = 7.dp),
        ) {
            Text(
                text = organization,
                color = DocumentInk,
                fontSize = BudapestContentFontSize,
                lineHeight = BudapestContentLineHeight,
                fontWeight = FontWeight.Bold,
            )
            if (location.isNotBlank()) {
                Text(
                    text = location,
                    color = DocumentMuted,
                    fontSize = BudapestContentFontSize,
                    lineHeight = BudapestContentLineHeight,
                )
            }
            if (date.isNotBlank()) {
                Text(
                    text = date,
                    color = DocumentMuted,
                    fontSize = BudapestContentFontSize,
                    lineHeight = BudapestContentLineHeight,
                )
            }
        }
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(Modifier.width(1.dp).fillMaxHeight().background(DocumentLine))
            Box(Modifier.padding(top = 1.dp).size(5.dp).background(DocumentLine, CircleShape))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 5.dp, bottom = 8.dp),
        ) {
            Text(
                text = title,
                color = DocumentInk,
                fontSize = BudapestContentFontSize,
                lineHeight = BudapestContentLineHeight,
                fontWeight = FontWeight.Bold,
            )
            if (description.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                ExperienceDescription(
                    text = description,
                    formatting = descriptionFormatting,
                    modifier = Modifier.fillMaxWidth(),
                    color = DocumentMuted,
                    fontSize = BudapestContentFontSize,
                    lineHeight = BudapestContentLineHeight,
                )
            }
        }
    }
}

@Composable
private fun SkillLabel(text: String) {
    Surface(
        shape = RoundedCornerShape(1.dp),
        color = Color.White,
        border = BorderStroke(0.6.dp, Color(0xFFB8BAC0)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
            color = DocumentMuted,
            fontSize = BudapestContentFontSize,
            lineHeight = BudapestContentLineHeight,
        )
    }
}

@Composable
private fun SidebarTitle(text: String) {
    Text(text, color = SidebarText, fontSize = BudapestSectionFontSize, fontWeight = FontWeight.Black)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SidebarDivider() {
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(color = Color.White.copy(alpha = 0.38f), thickness = 0.8.dp)
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun CompactSidebarText(text: String, prefix: String = "") {
    if (text.isNotBlank()) {
        Text(
            text = if (prefix.isBlank()) text else "$prefix $text",
            color = SidebarText,
            fontSize = BudapestContentFontSize,
            lineHeight = BudapestContentLineHeight,
        )
    }
}

@Composable
private fun SectionGap() = Spacer(Modifier.height(14.dp))

private fun compactDate(start: String, end: String): String =
    listOf(start, end).filter(String::isNotBlank).joinToString(" - ")

internal fun formattedText(text: String, formatting: ResumeTextFormatting): AnnotatedString {
    val displayText = if (formatting.spans.isEmpty() && formatting.paragraphs.isEmpty()) {
        reflowSoftLineBreaks(text)
    } else {
        text
    }
    return buildAnnotatedString {
        append(displayText)
        formatting.spans.forEach { span ->
            val start = span.start.coerceIn(0, displayText.length)
            val end = span.end.coerceIn(start, displayText.length)
            if (start == end) return@forEach
            addStyle(
                SpanStyle(
                    fontWeight = if (span.bold) FontWeight.Bold else null,
                    fontStyle = if (span.italic) FontStyle.Italic else null,
                    textDecoration = when {
                        span.underline && span.strikethrough -> TextDecoration.combine(
                            listOf(TextDecoration.Underline, TextDecoration.LineThrough),
                        )
                        span.underline -> TextDecoration.Underline
                        span.strikethrough -> TextDecoration.LineThrough
                        else -> null
                    },
                    color = span.textColorArgb?.let(::Color) ?: Color.Unspecified,
                    background = span.backgroundColorArgb?.let(::Color) ?: Color.Unspecified,
                ),
                start,
                end,
            )
        }
        formatting.paragraphs.forEach { paragraph ->
            val start = paragraph.start.coerceIn(0, displayText.length)
            val end = paragraph.end.coerceIn(start, displayText.length)
            if (start < end) {
                addStyle(
                    ParagraphStyle(
                        textAlign = when (paragraph.alignment) {
                            ResumeTextAlignment.Start -> TextAlign.Start
                            ResumeTextAlignment.Center -> TextAlign.Center
                            ResumeTextAlignment.End -> TextAlign.End
                            ResumeTextAlignment.Justify -> TextAlign.Justify
                        },
                    ),
                    start,
                    end,
                )
            }
        }
    }
}

@Preview(
    name = "Currículum completo",
    showBackground = true,
)
@Composable
private fun ResumeTemplateDocumentPreview() {
    ResumeTemplateDocument(
        resume = previewDocument(),
        modifier = Modifier.fillMaxWidth(),
    )
}
