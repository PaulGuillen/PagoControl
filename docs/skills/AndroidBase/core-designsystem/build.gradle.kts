plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.androidbase.designsystem"
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.compose.icons)
    implementation(platform(libs.compose.bom))
    api(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
}
