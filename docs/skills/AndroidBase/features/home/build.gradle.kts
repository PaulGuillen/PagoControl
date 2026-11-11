plugins {
    id("plugin.feature")
}

android {
    namespace = "com.devpaul.features.home"
}

dependencies {
    implementation(project(":features:templates"))
}
