pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidBase"
include(":app", ":core-domain", ":core-data", ":core-platform", ":core-designsystem")
include(":session", ":navigation", ":features:auth", ":features:home")
