plugins {
    id("plugin.kotlin.lib")
    id("plugin.koin")
}

dependencies {
    implementation(libs.koin.core)

    api(libs.coroutines.core)
    api(project(":core-domain"))
}
