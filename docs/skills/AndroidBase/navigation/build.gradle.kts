import dependence.*

plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.devpaul.navigation"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    koinImplementation()
    composeImplementation()
}
