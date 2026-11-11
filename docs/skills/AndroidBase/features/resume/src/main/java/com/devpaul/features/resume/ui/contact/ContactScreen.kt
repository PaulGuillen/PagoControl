package com.devpaul.features.resume.ui.contact

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.ui.components.BuilderFormCard
import com.devpaul.features.resume.ui.components.BuilderPage
import com.devpaul.features.resume.ui.components.BuilderTextField
import com.devpaul.features.resume.ui.components.TrackResumeDraft
import com.devpaul.features.resume.ui.components.isValidEmailOrBlank

@Composable
fun ContactScreen(
    onBack: () -> Unit,
    onContinue: (ResumeContact) -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 5,
    initialData: ResumeContact = ResumeContact(
        name = "Elena",
        lastName = "Vargas",
        professionalTitle = "Android Developer",
        email = "elena.vargas@example.com",
        phone = "+51 987 654 321",
        city = "Lima",
        address = "Lima, Peru",
    ),
    onDraftChange: (ResumeContact) -> Unit = {},
) {
    var name by rememberSaveable(initialData.name) { mutableStateOf(initialData.name) }
    var lastName by rememberSaveable(initialData.lastName) { mutableStateOf(initialData.lastName) }
    var professionalTitle by rememberSaveable(initialData.professionalTitle) { mutableStateOf(initialData.professionalTitle) }
    var email by rememberSaveable(initialData.email) { mutableStateOf(initialData.email) }
    var phone by rememberSaveable(initialData.phone) { mutableStateOf(initialData.phone) }
    var city by rememberSaveable(initialData.city) { mutableStateOf(initialData.city) }
    var address by rememberSaveable(initialData.address) { mutableStateOf(initialData.address) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    TrackResumeDraft(ResumeContact(name, lastName, professionalTitle, email, phone, city, address), onDraftChange)

    fun submit() {
        showErrors = true
        val valid = name.isNotBlank() && professionalTitle.isNotBlank() &&
            (email.isNotBlank() || phone.isNotBlank()) && email.isValidEmailOrBlank()
        if (!valid) AppAnalytics.error("validate_contact", code = "invalid_fields")
        if (valid) {
            onContinue(
                ResumeContact(
                    name = name.trim(),
                    lastName = lastName.trim(),
                    professionalTitle = professionalTitle.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    city = city.trim(),
                    address = address.trim(),
                ),
            )
        }
    }

    BuilderPage(
        onBack = onBack,
        step = 1,
        totalSteps = totalSteps,
        stepName = stringResource(R.string.ui_contact_d8a53e),
        title = stringResource(R.string.ui_contact_information_d4ed96),
        description = stringResource(R.string.ui_enter_your_basic_details_so_recruiters_can_contact_you_0bb08c),
        actionText = stringResource(R.string.ui_continue_to_work_experience_b166d4),
        onContinue = ::submit,
        modifier = modifier,
    ) {
        BuilderFormCard {
            BuilderTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.ui_name_e68491),
                placeholder = stringResource(R.string.ui_your_name_f07e1b),
                isError = showErrors && name.isBlank(),
                supportingText = if (showErrors && name.isBlank()) stringResource(R.string.ui_name_is_required_6043d7) else null,
            )
            Spacer(modifier = Modifier.height(16.dp))
            BuilderTextField(lastName, { lastName = it }, stringResource(R.string.ui_last_name_45b89a), stringResource(R.string.ui_your_last_name_a476e4))
            Spacer(modifier = Modifier.height(16.dp))
            BuilderTextField(
                value = professionalTitle,
                onValueChange = { professionalTitle = it },
                label = stringResource(R.string.ui_professional_title_67693e),
                placeholder = stringResource(R.string.ui_e_g_android_developer_f3a6a5),
                isError = showErrors && professionalTitle.isBlank(),
                supportingText = if (showErrors && professionalTitle.isBlank()) stringResource(R.string.ui_title_is_required_ec3e7d) else null,
            )
            Spacer(modifier = Modifier.height(16.dp))
            BuilderTextField(
                email,
                { email = it },
                stringResource(R.string.ui_email_59a167),
                "email@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = showErrors && (email.isBlank() && phone.isBlank() || !email.isValidEmailOrBlank()),
                supportingText = when {
                    showErrors && !email.isValidEmailOrBlank() -> stringResource(R.string.ui_enter_a_valid_email_d6ab4c)
                    showErrors && email.isBlank() && phone.isBlank() -> stringResource(R.string.ui_add_an_email_or_phone_number_30ce01)
                    else -> null
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            BuilderTextField(
                phone,
                { phone = it },
                stringResource(R.string.ui_phone_99af37),
                "+51 999 999 999",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = showErrors && email.isBlank() && phone.isBlank(),
                supportingText = if (showErrors && email.isBlank() && phone.isBlank()) {
                    stringResource(R.string.ui_add_an_email_or_phone_number_30ce01)
                } else null,
            )
            Spacer(modifier = Modifier.height(16.dp))
            BuilderTextField(city, { city = it }, stringResource(R.string.ui_city_c6fbe2), stringResource(R.string.ui_city_c6fbe2))
            Spacer(modifier = Modifier.height(16.dp))
            BuilderTextField(address, { address = it }, stringResource(R.string.ui_address_04f478), stringResource(R.string.ui_address_04f478))
        }
    }
}

@Preview(name = "Contact Light", showBackground = true, heightDp = 900)
@Composable
private fun ContactLightPreview() {
    CvFreeTheme(darkTheme = false) { ContactScreen({}, {}) }
}

@Preview(name = "Contact Dark", showBackground = true, heightDp = 900)
@Composable
private fun ContactDarkPreview() {
    CvFreeTheme(darkTheme = true) { ContactScreen({}, {}) }
}
