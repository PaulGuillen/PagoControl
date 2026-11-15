package plugin

import Config
import dependence.coroutinesImplementation
import dependence.unitTestingImplementation

plugins { id("org.jetbrains.kotlin.jvm") }

kotlin { jvmToolchain(Config.javaVersion.majorVersion.toInt()) }

coroutinesImplementation()
unitTestingImplementation()
