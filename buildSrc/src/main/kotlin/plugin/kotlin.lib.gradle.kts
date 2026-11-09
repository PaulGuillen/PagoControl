package plugin

import Config

plugins { id("org.jetbrains.kotlin.jvm") }

kotlin { jvmToolchain(Config.javaVersion.majorVersion.toInt()) }
