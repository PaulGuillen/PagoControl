pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CvFree"
include(":app")
include(":navigation")
include(":session")
include(":core-data")
include(":core-domain")
include(":core-designsystem")
include(":core-platform")

// Features
include(":features:splash")
include(":features:auth")
include(":features:home")
include(":features:onboarding")
include(":features:resume")
include(":features:settings")
include(":features:templates")
