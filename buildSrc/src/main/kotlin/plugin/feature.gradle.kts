package plugin

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("plugin.compose")
    id("plugin.koin")
}

val featureLibs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":session"))
    implementation(project(":core-platform"))
    implementation(project(":core-designsystem"))
    implementation(project(":navigation"))
    implementation(featureLibs.findLibrary("androidx-navigation-compose").get())
    implementation(featureLibs.findLibrary("androidx-lifecycle-runtime-compose").get())
    implementation(featureLibs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    implementation(featureLibs.findLibrary("koin-compose").get())
    implementation(featureLibs.findLibrary("androidx-compose-icons").get())
    testImplementation(featureLibs.findLibrary("junit").get())
    testImplementation(featureLibs.findLibrary("coroutines-test").get())
}
