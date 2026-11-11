// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    id("com.android.application") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
}
