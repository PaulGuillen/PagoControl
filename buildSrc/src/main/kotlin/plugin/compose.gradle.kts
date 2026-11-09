package plugin

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.compose")
}

android { buildFeatures { compose = true } }

val composeLibs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(platform(composeLibs.findLibrary("androidx-compose-bom").get()))
    implementation(composeLibs.findLibrary("androidx-compose-ui").get())
    implementation(composeLibs.findLibrary("androidx-compose-material3").get())
    implementation(composeLibs.findLibrary("androidx-compose-ui-tooling-preview").get())
    debugImplementation(composeLibs.findLibrary("androidx-compose-ui-tooling").get())
}
