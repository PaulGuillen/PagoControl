import dependence.datastoreImplementation
import dependence.firebaseEmulatorTestingImplementation
import dependence.firestoreImplementation
import dependence.roomImplementation

plugins { id("plugin.feature") }
android { namespace = "com.devpaul.pagocontrol.features.home" }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

firestoreImplementation()
datastoreImplementation()
roomImplementation()
firebaseEmulatorTestingImplementation()
