plugins {
    id("plugin.android.lib")
    id("plugin.koin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.androidbase.data"
    buildFeatures { buildConfig = true }
    defaultConfig {
        val apiUrl = providers.gradleProperty("api.baseUrl").get()
        require(apiUrl.startsWith("https://") && apiUrl.endsWith("/")) {
            "api.baseUrl debe usar HTTPS y terminar en /"
        }
        buildConfigField("String", "API_BASE_URL", "\"${apiUrl.replace("\"", "") }\"")
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":session"))
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
    implementation(libs.datastore)
    implementation(libs.serialization.json)
    implementation(libs.koin.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
