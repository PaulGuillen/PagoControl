package com.devpaul.features.resume.ui.summary

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.ui.components.BuilderFormCard
import com.devpaul.features.resume.ui.components.BuilderPage
import com.devpaul.features.resume.ui.components.TrackResumeDraft

private const val SummaryMaxLength = 1_000

@Composable
fun SummaryScreen(
    onBack: () -> Unit,
    onContinue: (String, ResumeTextFormatting) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 5,
    totalSteps: Int = 5,
    initialSummary: String = "",
    initialFormatting: ResumeTextFormatting = ResumeTextFormatting(),
    onDraftChange: (String, ResumeTextFormatting) -> Unit = { _, _ -> },
) {
    val editor = rememberRichTextEditorState(
        initialText = initialSummary,
        initialFormatting = initialFormatting,
        maxLength = SummaryMaxLength,
    )

    TrackResumeDraft(editor.value.text to editor.formatting) { (text, formatting) ->
        onDraftChange(text, formatting)
    }

    BuilderPage(
        onBack = onBack,
        step = step,
        totalSteps = totalSteps,
        stepName = stringResource(R.string.ui_summary_35414e),
        title = stringResource(R.string.ui_professional_summary_b65e50),
        description = stringResource(R.string.ui_summarize_your_profile_experience_and_strengths_you_can_use_ca393b),
        actionText = stringResource(R.string.ui_choose_template_4b2e85),
        onContinue = { onContinue(editor.value.text, editor.formatting) },
        modifier = modifier,
    ) {
        BuilderFormCard {
            RichTextEditor(
                state = editor,
                label = stringResource(R.string.ui_about_me_17764c),
                placeholder = stringResource(R.string.ui_e_g_senior_android_developer_experienced_in_kotlin_compose_a_7d2724),
                minLines = 5,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.summary_character_count, editor.value.text.length, SummaryMaxLength),
                modifier = Modifier.fillMaxWidth().align(Alignment.End),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Preview(name = "Summary Light", showBackground = true, heightDp = 820)
@Composable
private fun SummaryLightPreview() {
    CvFreeTheme(darkTheme = false) { SummaryScreen({}, { _, _ -> }) }
}

@Preview(name = "Summary Dark", showBackground = true, heightDp = 820)
@Composable
private fun SummaryDarkPreview() {
    CvFreeTheme(darkTheme = true) { SummaryScreen({}, { _, _ -> }) }
}
