plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(Deps.GradlePlugin.compileSdkVersion)
    defaultConfig {
        applicationId = "com.geckour.flical"
        minSdkVersion(Deps.GradlePlugin.minSdkVersion)
        targetSdkVersion(Deps.GradlePlugin.targetSdkVersion)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = Deps.Test.instrumentTestRunner

        dataBinding.setEnabled(true)

        val filesAuthorityValue = "$applicationId.files"
        manifestPlaceholders = mapOf("filesAuthority" to filesAuthorityValue)
        buildConfigField("String", "FILES_AUTHORITY", "\"$filesAuthorityValue\"")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(Deps.Kotlin.stdlib)
    implementation(Deps.AndroidX.appCompat)
    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.design)
    implementation(Deps.AndroidX.constraint)
    testImplementation(Deps.Test.junit)
    androidTestImplementation(Deps.Test.testRunner)
    androidTestImplementation(Deps.Test.espressoCore)

    // Coroutines
    implementation(Deps.Kotlin.Coroutines.core)
    implementation(Deps.Kotlin.Coroutines.android)

    // Logging
    implementation(Deps.Timber.timber)

    // Permission
    implementation(Deps.PermissionDispatcher.permissionDispatcher)
    kapt(Deps.PermissionDispatcher.processor)

    // BigDecimal Math
    implementation(Deps.BigDecimalMath.bigDecimalMath)
}
