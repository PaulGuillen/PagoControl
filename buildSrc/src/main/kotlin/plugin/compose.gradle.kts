package plugin

import dependence.composeImplementation

plugins {
    id("plugin.android.lib")
    id("org.jetbrains.kotlin.plugin.compose")
}

android { buildFeatures { compose = true } }

composeImplementation()
