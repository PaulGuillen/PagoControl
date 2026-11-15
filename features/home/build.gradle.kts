plugins { id("plugin.feature") }
android { namespace = "com.devpaul.pagocontrol.features.home" }

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.coroutines.play.services)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.firebase.auth)
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }
