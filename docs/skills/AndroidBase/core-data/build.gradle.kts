import dependence.*

plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.devpaul.core.data"
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(project(":core-domain"))
    implementation(project(":core-platform"))
    implementation(project(":session"))
    implementation(libs.kotlinx.serialization.json)
    retrofitImplementation()
    koinImplementation()
    datastoreImplementation()
    firebaseImplementation()
    coroutinesPlayServicesImplementation()
    chuckerImplementation()
}
