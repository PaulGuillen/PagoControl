plugins { id("plugin.android.lib") }
android {
    namespace = "com.devpaul.pagocontrol.platform"
    buildFeatures { buildConfig = true }
}
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    testImplementation(libs.junit)
    implementation(libs.timber)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.coroutines.core)
}
