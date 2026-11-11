package plugin

import Config

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(Config.javaVersion.majorVersion.toInt())
}
