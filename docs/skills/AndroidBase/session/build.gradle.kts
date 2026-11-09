plugins { id("plugin.kotlin.lib") }

dependencies {
    api(libs.coroutines.core)
    api(project(":core-domain"))
}
