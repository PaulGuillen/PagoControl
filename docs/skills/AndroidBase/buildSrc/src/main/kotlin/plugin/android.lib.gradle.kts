package plugin

import Config
import dependence.*

plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    compileSdk = Config.COMPILE_SDK

    defaultConfig {
        minSdk = Config.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = Config.javaVersion
        targetCompatibility = Config.javaVersion
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }

    configureEnvironments()
}

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
}

dependencies {
    androidCoreImplementation()
    androidTestingImplementation()
}
