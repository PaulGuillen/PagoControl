import dependence.coroutinesImplementation
import dependence.datastoreImplementation
import dependence.koinAndroidImplementation
import dependence.unitTestingImplementation

plugins {
    id("plugin.android.lib")
    id("plugin.koin")
}
android { namespace = "com.devpaul.pagocontrol.data" }
dependencies {
    implementation(project(":core-domain"))
    implementation(project(":session"))
}

coroutinesImplementation()
datastoreImplementation()
koinAndroidImplementation()
unitTestingImplementation()
