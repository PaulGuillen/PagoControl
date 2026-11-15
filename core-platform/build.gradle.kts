import dependence.coroutinesImplementation
import dependence.platformImplementation
import dependence.unitTestingImplementation

plugins { id("plugin.android.lib") }
android {
    namespace = "com.devpaul.pagocontrol.platform"
    buildFeatures { buildConfig = true }
}

platformImplementation()
coroutinesImplementation()
unitTestingImplementation()
