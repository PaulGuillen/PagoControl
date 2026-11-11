import Config
import dependence.*

plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.compose")
}


android {
    namespace = "com.devpaul.core.designsystem"
    buildFeatures {
        compose = true
    }
}


dependencies {
    implementation(project(":core-platform"))
    composeImplementation()
}
