import dependence.serializationImplementation

plugins {
    id("plugin.kotlin.lib")
    id("plugin.koin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

serializationImplementation()
