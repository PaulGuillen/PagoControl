package plugin

import dependence.koinImplementation

plugins { id("com.google.devtools.ksp") }

koinImplementation()

ksp { arg("KOIN_DEFAULT_MODULE", "false") }
