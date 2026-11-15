package plugin

import Config
import dependence.applicationImplementation
import dependence.composeImplementation
import dependence.unitTestingImplementation

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = Config.COMPILE_SDK
    defaultConfig {
        minSdk = Config.MIN_SDK
        targetSdk = Config.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = Config.javaVersion
        targetCompatibility = Config.javaVersion
    }
    buildFeatures { compose = true; buildConfig = true }
}

composeImplementation()
applicationImplementation()
unitTestingImplementation()
