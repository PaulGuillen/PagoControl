package dependence

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ProductFlavor

private const val DIMENSION_ENVIRONMENT = "environment"
private const val PROD_VERSION_CODE = 3
private const val PROD_VERSION_NAME = "1.0.2"
private const val DEV_VERSION_CODE = 1
private const val DEV_VERSION_NAME = "1.0.0"

fun ApplicationExtension.configureEnvironments() {
    flavorDimensions.add(DIMENSION_ENVIRONMENT)
    productFlavors {
        create("prod") {
            setupProd(this)
            versionCode = PROD_VERSION_CODE
            versionName = PROD_VERSION_NAME
            versionNameSuffix = "-prod"
        }
        create("dev") {
            setupDev(this)
            versionCode = DEV_VERSION_CODE
            versionName = DEV_VERSION_NAME
            versionNameSuffix = "-dev"
        }
    }
}

fun LibraryExtension.configureEnvironments() {
    flavorDimensions.add(DIMENSION_ENVIRONMENT)
    productFlavors {
        create("prod") {
            setupProd(this)
            buildConfigField("String", "VERSION_NAME", "\"$PROD_VERSION_NAME\"")
            buildConfigField("int", "VERSION_CODE", PROD_VERSION_CODE.toString())
        }
        create("dev") {
            setupDev(this)
            buildConfigField("String", "VERSION_NAME", "\"$DEV_VERSION_NAME\"")
            buildConfigField("int", "VERSION_CODE", DEV_VERSION_CODE.toString())
        }
    }
}

private fun setupProd(flavor: ProductFlavor) {
    flavor.dimension = DIMENSION_ENVIRONMENT
    flavor.buildConfigField("String", "ENVIRONMENT", "\"prod\"")
}

private fun setupDev(flavor: ProductFlavor) {
    flavor.dimension = DIMENSION_ENVIRONMENT
    flavor.buildConfigField("String", "ENVIRONMENT", "\"dev\"")
}
