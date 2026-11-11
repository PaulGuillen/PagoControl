import dependence.*

plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.devpaul.core.platform"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.timber)
    composeImplementation()
}
