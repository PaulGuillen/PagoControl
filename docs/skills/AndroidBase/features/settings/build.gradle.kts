plugins {
    id("plugin.feature")
}

android {
    namespace = "com.devpaul.features.settings"
}

dependencies {
    implementation(libs.koin.compose)
    testImplementation(libs.kotlinx.coroutines.test)
}
