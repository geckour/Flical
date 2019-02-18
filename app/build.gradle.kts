import com.android.build.gradle.internal.packaging.getDefaultDebugKeystoreLocation

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("io.fabric")
    id("com.google.gms.google-services") apply false
}

android {
    compileSdkVersion(Deps.GradlePlugin.compileSdkVersion)
    defaultConfig {
        applicationId = "com.geckour.flical"
        minSdkVersion(Deps.GradlePlugin.minSdkVersion)
        targetSdkVersion(Deps.GradlePlugin.targetSdkVersion)
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = Deps.Test.instrumentTestRunner

        dataBinding.isEnabled = true

        val filesAuthorityValue = "$applicationId.files"
        manifestPlaceholders = mapOf("filesAuthority" to filesAuthorityValue)
        buildConfigField("String", "FILES_AUTHORITY", "\"$filesAuthorityValue\"")
    }
    signingConfigs {
        getByName("debug") {
            storeFile = getDefaultDebugKeystoreLocation()
        }
        create("release") {
            val releaseSettingGradleFile = File("${project.rootDir}/app/signing/release.gradle")
            if (releaseSettingGradleFile.exists())
                apply(from = releaseSettingGradleFile, to = android)
            else
                throw GradleException("Missing ${releaseSettingGradleFile.absolutePath} . Generate the file by copying and modifying ${project.rootDir}/app/signing/release.gradle.sample .")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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

    // Firebase
    implementation(Deps.Firebase.core)
    implementation(Deps.Firebase.crashlytics) { isTransitive = true }

    // Logging
    implementation(Deps.Timber.timber)

    // ViewModel
    implementation(Deps.AndroidX.Lifecycle.extensions)
    implementation(Deps.AndroidX.Lifecycle.viewModelKtx)
    kapt(Deps.AndroidX.Lifecycle.compiler)

    // Permission
    implementation(Deps.PermissionDispatcher.permissionDispatcher)
    kapt(Deps.PermissionDispatcher.processor)

    // BigDecimal Math
    implementation(Deps.BigDecimalMath.bigDecimalMath)

    // Exif
    implementation(Deps.Exif.exifInterface)
}

apply(plugin = "com.google.gms.google-services")
