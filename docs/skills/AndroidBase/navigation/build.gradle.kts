plugins {
    id("plugin.kotlin.lib")
    id("plugin.koin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(libs.koin.core)
 implementation(libs.serialization.json) }
