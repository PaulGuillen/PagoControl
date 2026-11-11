package com.devpaul.cvfree.analytics

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.devpaul.navigation.route.AuthRoute
import com.devpaul.navigation.route.HomeRoute
import com.devpaul.navigation.route.OnboardingRoute
import com.devpaul.navigation.route.ResumeRoute
import com.devpaul.navigation.route.SettingsRoute
import com.devpaul.navigation.route.SplashRoute

internal fun NavDestination.analyticsScreenName(): String? = when {
    hasRoute<SplashRoute.Splash>() -> "splash"
    hasRoute<OnboardingRoute.Welcome>() -> "onboarding"
    hasRoute<AuthRoute.Login>() -> "login"
    hasRoute<AuthRoute.Register>() -> "register"
    hasRoute<AuthRoute.ForgotPassword>() -> "forgot_password"
    hasRoute<AuthRoute.PasswordResetSent>() -> "password_reset_sent"
    hasRoute<HomeRoute.Home>() -> "home"
    hasRoute<HomeRoute.Templates>() -> "all_templates"
    hasRoute<HomeRoute.TemplatePreview>() -> "template_preview"
    hasRoute<HomeRoute.Notifications>() -> "notifications"
    hasRoute<ResumeRoute.ResumeList>() -> "my_cvs"
    hasRoute<ResumeRoute.Importing>() -> "resume_import"
    hasRoute<ResumeRoute.SectionSelection>() -> "resume_sections"
    hasRoute<ResumeRoute.Contact>() -> "resume_contact"
    hasRoute<ResumeRoute.Experience>() -> "resume_experience"
    hasRoute<ResumeRoute.Education>() -> "resume_education"
    hasRoute<ResumeRoute.Skills>() -> "resume_skills"
    hasRoute<ResumeRoute.Courses>() -> "resume_courses"
    hasRoute<ResumeRoute.Links>() -> "resume_links"
    hasRoute<ResumeRoute.Languages>() -> "resume_languages"
    hasRoute<ResumeRoute.References>() -> "resume_references"
    hasRoute<ResumeRoute.Summary>() -> "resume_summary"
    hasRoute<ResumeRoute.Template>() -> "resume_template"
    hasRoute<ResumeRoute.Preview>() -> "resume_preview"
    hasRoute<SettingsRoute.Settings>() -> "settings"
    hasRoute<SettingsRoute.MyPlan>() -> "my_plan"
    hasRoute<SettingsRoute.HelpCenter>() -> "help_center"
    hasRoute<SettingsRoute.Privacy>() -> "privacy"
    else -> null
}
