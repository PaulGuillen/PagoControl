plugins {
    id("plugin.application")
    id("plugin.koin")
}

android {
    namespace = "com.devpaul.pagocontrol"

    defaultConfig {
        applicationId = "com.devpaul.pagocontrol"
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":session"))
    implementation(project(":core-data"))
    implementation(project(":core-designsystem"))
    implementation(project(":navigation"))
    implementation(project(":features:auth"))
    implementation(project(":features:home"))
    implementation(project(":features:movements"))
    implementation(project(":features:profile"))
    implementation(project(":features:reports"))
    implementation(project(":features:splash"))
    implementation(project(":features:onboarding"))
    implementation(libs.koin.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
