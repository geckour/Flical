object Deps {
    object Kotlin {
        const val version = "1.5.21"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"

        object Coroutines {
            private const val version = "1.5.2"
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        }
    }

    object GradlePlugin {
        const val android = "com.android.tools.build:gradle:7.0.2"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.version}"
        const val googleService = "com.google.gms:google-services:4.3.10"
        const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-gradle:2.7.1"

        const val compileSdkVersion = 31
        const val minSdkVersion = 26
        const val targetSdkVersion = 31
    }

    object Test {
        const val junit = "junit:junit:4.13.2"
        const val testRunner = "androidx.test:runner:1.4.0"
        const val instrumentTestRunner = "androidx.test.runner.AndroidJUnitRunner"
        const val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
    }

    object AndroidX {
        const val appCompat = "androidx.appcompat:appcompat:1.3.1"
        const val coreKtx = "androidx.core:core-ktx:1.6.0"
        const val design = "com.google.android.material:material:1.4.0"
        const val constraint = "androidx.constraintlayout:constraintlayout:2.1.0"
        const val preference = "androidx.preference:preference-ktx:1.1.1"

        object Lifecycle {
            private const val version = "2.3.1"
            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
        }
    }

    object Timber {
        const val timber = "com.jakewharton.timber:timber:5.0.1"
    }

    object PermissionDispatcher {
        private const val version = "4.9.1"
        const val permissionDispatcher = "com.github.permissions-dispatcher:permissionsdispatcher:$version"
        const val processor = "com.github.permissions-dispatcher:permissionsdispatcher-processor:$version"
    }

    object BigDecimalMath {
        const val bigDecimalMath = "ch.obermuhlner:big-math:2.3.0"
    }

    object Exif {
        const val exifInterface = "androidx.exifinterface:exifinterface:1.3.3"
    }

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:28.4.1"
        const val crashlytics = "com.google.firebase:firebase-analytics-ktx"
    }

    object Truth {
        const val truth = "com.google.truth:truth:1.1.3"
    }

    object MockK {
        const val mockK = "io.mockk:mockk:1.12.0"
    }
}