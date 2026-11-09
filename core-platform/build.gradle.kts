plugins { id("plugin.android.lib") }
android { namespace = "com.devpaul.pagocontrol.platform" }
dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.coroutines.core)
}
