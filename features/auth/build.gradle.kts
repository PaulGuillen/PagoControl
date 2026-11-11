plugins { id("plugin.feature") }
android {
    namespace = "com.devpaul.pagocontrol.features.auth"
    buildFeatures { buildConfig = true }
    buildTypes {
        debug {
            val emulatorHost = providers.gradleProperty("firebaseEmulatorHost").orElse("").get()
            require(emulatorHost.matches(Regex("[a-zA-Z0-9.:-]*")))
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"$emulatorHost\"")
        }
        release { buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"\"") }
    }
}
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.coroutines.play.services)
}
