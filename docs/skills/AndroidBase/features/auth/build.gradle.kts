import dependence.*

plugins {
    id("plugin.feature")
}

android {
    namespace = "com.devpaul.features.auth"
}

dependencies {
    firebaseImplementation()
    coroutinesPlayServicesImplementation()
    implementation(libs.koin.compose)
    testImplementation(libs.kotlinx.coroutines.test)
}
