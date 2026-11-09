plugins {
    id("plugin.android.lib")
    id("plugin.koin")
}
android { namespace = "com.devpaul.pagocontrol.data" }
dependencies {
    implementation(project(":core-domain"))
    implementation(project(":session"))
    implementation(libs.coroutines.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.koin.android)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
