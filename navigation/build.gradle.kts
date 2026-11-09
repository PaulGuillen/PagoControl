plugins {
    id("plugin.kotlin.lib")
    id("org.jetbrains.kotlin.plugin.serialization")
}
dependencies { implementation(libs.serialization.json) }
