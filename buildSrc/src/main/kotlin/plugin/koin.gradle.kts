package plugin

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins { id("com.google.devtools.ksp") }

val koinLibs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(koinLibs.findLibrary("koin-annotations").get())
    "ksp"(koinLibs.findLibrary("koin-compiler").get())
}

ksp { arg("KOIN_DEFAULT_MODULE", "false") }
