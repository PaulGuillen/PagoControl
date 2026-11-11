plugins {
    id("plugin.application")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.devpaul.cvfree"

    defaultConfig {
        applicationId = "com.devpaul.cvfree"
        buildConfigField("String", "PREMIUM_PRODUCT_ID", "\"cvfree_premium\"")
        buildConfigField("String", "PREMIUM_BASE_PLAN_ID", "\"monthly\"")
    }

    flavorDimensions += "access"
    productFlavors {
        create("free") {
            dimension = "access"
            buildConfigField("String", "ACCESS_PLAN", "\"free\"")
            buildConfigField("boolean", "PREMIUM_ACCESS_OVERRIDE", "false")
        }
        create("premium") {
            dimension = "access"
            buildConfigField("String", "ACCESS_PLAN", "\"premium\"")
            buildConfigField("boolean", "PREMIUM_ACCESS_OVERRIDE", "true")
        }
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { variant ->
        if (variant.productFlavors.any { (_, flavor) -> flavor == "premium" }) {
            variant.enable = false
        }
    }
}

dependencies {
    implementation(project(":navigation"))
    implementation(project(":core-platform"))
    implementation(project(":core-designsystem"))
    implementation(project(":core-data"))
    implementation(project(":core-domain"))

    // Features
    implementation(project(":features:splash"))
    implementation(project(":features:auth"))
    implementation(project(":features:home"))
    implementation(project(":features:onboarding"))
    implementation(project(":features:resume"))
    implementation(project(":features:settings"))
    implementation(project(":features:templates"))

    implementation(libs.androidx.activity.ktx)
    implementation(libs.app.update.ktx)
    implementation(libs.play.billing)
    implementation(libs.koin.compose)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.firebase.appcheck.debug)
}
