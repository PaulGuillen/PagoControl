plugins { id("plugin.android.lib") }

android { namespace = "com.example.androidbase.platform" }

dependencies {
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.coroutines.android)
    implementation(libs.timber)
}
