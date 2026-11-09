plugins {
    id("plugin.kotlin.lib")
    id("plugin.koin")
}
dependencies {
    implementation(libs.koin.core)
 implementation(libs.coroutines.core) }
