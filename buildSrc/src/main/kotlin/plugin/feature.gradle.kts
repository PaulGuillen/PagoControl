package plugin

import dependence.featureImplementation
import dependence.unitTestingImplementation

plugins {
    id("plugin.compose")
    id("plugin.koin")
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":session"))
    implementation(project(":core-platform"))
    implementation(project(":core-designsystem"))
    implementation(project(":navigation"))
}

featureImplementation()
unitTestingImplementation()
