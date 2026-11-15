package dependence

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

fun Project.getLibs(): VersionCatalog =
    extensions.getByType<VersionCatalogsExtension>().named("libs")

private operator fun VersionCatalog.get(alias: String) = findLibrary(alias).get()

fun Project.composeImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(platform(libs["androidx-compose-bom"]))
        "implementation"(libs["androidx-compose-ui"])
        "implementation"(libs["androidx-compose-material3"])
        "implementation"(libs["androidx-compose-ui-tooling-preview"])
        "implementation"(libs["androidx-compose-icons"])
        "implementation"(libs["androidx-activity-compose"])
        "debugImplementation"(libs["androidx-compose-ui-tooling"])
    }
}

fun Project.featureImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["androidx-navigation-compose"])
        "implementation"(libs["androidx-lifecycle-runtime-compose"])
        "implementation"(libs["androidx-lifecycle-viewmodel-compose"])
        "implementation"(libs["koin-compose"])
    }
}

fun Project.unitTestingImplementation() {
    val libs = getLibs()
    dependencies {
        "testImplementation"(libs["junit"])
        "testImplementation"(libs["coroutines-test"])
    }
}

fun Project.koinImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["koin-core"])
        "implementation"(libs["koin-annotations"])
        "ksp"(libs["koin-compiler"])
    }
}

fun Project.coroutinesImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["coroutines-core"])
    }
}

fun Project.datastoreImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["androidx-datastore-preferences"])
    }
}

fun Project.koinAndroidImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["koin-android"])
    }
}

fun Project.firestoreImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(platform(libs["firebase-bom"]))
        "implementation"(libs["firebase-firestore"])
        "implementation"(libs["coroutines-play-services"])
    }
}

fun Project.firebaseAuthImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(platform(libs["firebase-bom"]))
        "implementation"(libs["firebase-auth"])
    }
}

fun Project.roomImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["androidx-room-runtime"])
        "ksp"(libs["androidx-room-compiler"])
    }
}

fun Project.firebaseEmulatorTestingImplementation() {
    val libs = getLibs()
    dependencies {
        "androidTestImplementation"(platform(libs["firebase-bom"]))
        "androidTestImplementation"(libs["firebase-auth"])
        "androidTestImplementation"(libs["androidx-junit"])
        "androidTestImplementation"(libs["androidx-test-runner"])
    }
}

fun Project.serializationImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["serialization-json"])
    }
}

fun Project.applicationImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["koin-compose"])
        "implementation"(libs["androidx-navigation-compose"])
        "implementation"(libs["androidx-lifecycle-runtime-compose"])
        "implementation"(libs["androidx-compose-ui-graphics"])
        "implementation"(libs["androidx-core-ktx"])
        "implementation"(libs["androidx-lifecycle-runtime-ktx"])
        "androidTestImplementation"(platform(libs["androidx-compose-bom"]))
        "androidTestImplementation"(libs["androidx-compose-ui-test-junit4"])
        "androidTestImplementation"(libs["androidx-espresso-core"])
        "androidTestImplementation"(libs["androidx-junit"])
        "androidTestImplementation"(libs["androidx-room-runtime"])
        "debugImplementation"(libs["androidx-room-runtime"])
        "debugImplementation"(libs["androidx-compose-ui-test-manifest"])
    }
}

fun Project.platformImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(platform(libs["firebase-bom"]))
        "implementation"(libs["firebase-analytics"])
        "implementation"(libs["firebase-crashlytics"])
        "implementation"(libs["firebase-appcheck-playintegrity"])
        "debugImplementation"(libs["firebase-appcheck-debug"])
        "implementation"(libs["timber"])
        "implementation"(libs["androidx-lifecycle-viewmodel-ktx"])
    }
}
