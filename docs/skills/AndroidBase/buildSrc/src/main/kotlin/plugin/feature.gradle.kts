package plugin

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("plugin.koin")
}

android {
    buildFeatures { compose = true }
}

val featureLibs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":session"))
    implementation(project(":core-platform"))
    implementation(project(":core-designsystem"))
    implementation(project(":navigation"))
    implementation(platform(featureLibs.findLibrary("compose-bom").get()))
    implementation(featureLibs.findLibrary("compose-ui").get())
    implementation(featureLibs.findLibrary("compose-preview").get())
    debugImplementation(featureLibs.findLibrary("compose-tooling").get())
    implementation(featureLibs.findLibrary("compose-icons").get())
    implementation(featureLibs.findLibrary("compose-material3").get())
    implementation(featureLibs.findLibrary("androidx-lifecycle-runtime-compose").get())
    implementation(featureLibs.findLibrary("androidx-navigation-compose").get())
    implementation(featureLibs.findLibrary("koin-compose").get())
    implementation(featureLibs.findLibrary("serialization-json").get())
    testImplementation(featureLibs.findLibrary("junit").get())
    testImplementation(featureLibs.findLibrary("coroutines-test").get())
}
