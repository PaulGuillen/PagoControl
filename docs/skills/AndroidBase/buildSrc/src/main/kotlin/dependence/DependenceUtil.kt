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
        "implementation"(libs["androidx-activity-compose"])
        "implementation"(platform(libs["androidx-compose-bom"]))
        "implementation"(libs["androidx-compose-ui"])
        "implementation"(libs["androidx-compose-ui-graphics"])
        "implementation"(libs["androidx-compose-ui-tooling-preview"])
        "implementation"(libs["androidx-compose-material3"])
        "implementation"(libs["androidx-compose-material-icons-extended"])
        "implementation"(libs["androidx-lifecycle-runtime-ktx"])
        "implementation"(libs["androidx-lifecycle-viewmodel-compose"])
        "implementation"(libs["navigation-compose"])
        "androidTestImplementation"(platform(libs["androidx-compose-bom"]))
        "androidTestImplementation"(libs["androidx-compose-ui-test-junit4"])
        "debugImplementation"(libs["androidx-compose-ui-tooling"])
        "debugImplementation"(libs["androidx-compose-ui-test-manifest"])
    }
}

fun Project.datastoreImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["datastore-preferences"])
        "implementation"(libs["datastore-core"])
    }
}

fun Project.koinImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["koin"])
        "implementation"(libs["koin-core-annotations"])
        "implementation"(libs["koin-annotations"])
        "ksp"(libs["koin-compiler"])
    }
}

fun Project.uiCoreLibsImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["material"])
        "implementation"(libs["glide"])
        "implementation"(libs["androidx-appcompat"])
    }
}

fun Project.androidCoreImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["androidx-core-ktx"])
        "implementation"(libs["timber"])
        "implementation"(libs["jwtdecode"])
        "implementation"(libs["androidx-lifecycle-runtime-ktx"])
        "implementation"(libs["androidx-lifecycle-viewmodel-ktx"])
        "implementation"(libs["androidx-lifecycle-livedata-ktx"])
        "implementation"(libs["coroutines-android"])
    }
}

fun Project.firebaseImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(platform(libs["firebase-bom"]))
        "implementation"(libs["firebase-analytics"])
        "implementation"(libs["firebase-auth"])
        "implementation"(libs["firebase-firestore"])
        "implementation"(libs["firebase-messaging"])
    }
}

fun Project.coroutinesPlayServicesImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["coroutines-play-services"])
    }
}

fun Project.navigationFragmentImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["navigation-fragment"])
        "implementation"(libs["navigation-ui"])
        "implementation"(libs["kotlinx-serialization-json"])
    }
}

fun Project.androidTestingImplementation() {
    val libs = getLibs()
    dependencies {
        "testImplementation"(libs["junit"])
        "androidTestImplementation"(libs["androidx-junit"])
        "androidTestImplementation"(libs["androidx-espresso-core"])
        "testImplementation"(libs["mockito-core"])
        "testImplementation"(libs["mockito-inline"])
    }
}

fun Project.retrofitImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["retrofit"])
        "implementation"(libs["retrofit-gson-converter"])
        "implementation"(libs["okhttp3-loggin-interceptor"])
    }
}

fun Project.pdfBoxImplementation() {
    val libs = getLibs()
    dependencies {
        "implementation"(libs["pdfbox-android"])
    }
}

fun Project.chuckerImplementation() {
    val libs = getLibs()
    dependencies {
        "debugImplementation"(libs["chucker"])
        "releaseImplementation"(libs["chucker-no-op"])
    }
}
