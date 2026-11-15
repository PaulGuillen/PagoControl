plugins {
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("plugin.application")
    id("plugin.koin")
}

android {
    namespace = "com.devpaul.pagocontrol"

    defaultConfig {
        applicationId = "com.devpaul.pagocontrol"
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "ENVIRONMENT", "\"production\"")
        buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"\"")

    }

    buildTypes {
        debug {
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
            val emulatorHost = providers.gradleProperty("firebaseEmulatorHost").orElse("").get()
            require(emulatorHost.matches(Regex("[a-zA-Z0-9.:-]*")))
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"$emulatorHost\"")
        }
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core-platform"))
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
}
