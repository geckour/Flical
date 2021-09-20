import com.android.build.gradle.internal.packaging.getDefaultDebugKeystoreLocation

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("com.google.gms.google-services") apply false
    id("com.google.firebase.crashlytics") apply false
}

android {
    compileSdk = Deps.GradlePlugin.compileSdkVersion
    defaultConfig {
        applicationId = "com.geckour.flical"
        minSdk = Deps.GradlePlugin.minSdkVersion
        targetSdk = Deps.GradlePlugin.targetSdkVersion
        versionCode = 9
        versionName = "1.1.0"
        testInstrumentationRunner = Deps.Test.instrumentTestRunner

        dataBinding.isEnabled = true

        val filesAuthorityValue = "$applicationId.files"
        manifestPlaceholders["filesAuthority"] = filesAuthorityValue
        buildConfigField("String", "FILES_AUTHORITY", "\"$filesAuthorityValue\"")
    }
    signingConfigs {
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Deps.Compose.version
    }
}

dependencies {
    implementation(platform(Deps.Firebase.bom))

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
    implementation(Deps.Firebase.crashlytics) { isTransitive = true }

    // Logging
    implementation(Deps.Timber.timber)

    // ViewModel
    implementation(Deps.AndroidX.Lifecycle.viewModelKtx)
    kapt(Deps.AndroidX.Lifecycle.compiler)

    // Permission
    implementation(Deps.PermissionDispatcher.permissionDispatcher)
    kapt(Deps.PermissionDispatcher.processor)

    implementation(Deps.AndroidX.preference)

    // BigDecimal Math
    implementation(Deps.BigDecimalMath.bigDecimalMath)

    // Exif
    implementation(Deps.Exif.exifInterface)

    // Test
    testImplementation(Deps.Truth.truth)
    testImplementation(Deps.MockK.mockK)

    // Compose
    implementation(Deps.Compose.ui)
    implementation(Deps.Compose.activity)
    implementation(Deps.Compose.material)
    implementation(Deps.Compose.uiTooling)
    androidTestImplementation(Deps.Compose.uiTest)

    // Image Processing
    implementation(Deps.Image.coilCompose)
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")
