object Deps {
    object Kotlin {
        const val version = "1.3.72"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"

        object Coroutines {
            private const val version = "1.3.8"
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        }
    }

    object GradlePlugin {
        const val android = "com.android.tools.build:gradle:4.0.1"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.version}"
        const val googleService = "com.google.gms:google-services:4.3.3"
        const val fabric = "io.fabric.tools:gradle:1.+"

        const val compileSdkVersion = 29
        const val minSdkVersion = 24
        const val targetSdkVersion = 29
    }

    object Test {
        const val junit = "junit:junit:4.13"
        const val testRunner = "androidx.test:runner:1.2.0"
        const val instrumentTestRunner = "androidx.test.runner.AndroidJUnitRunner"
        const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
    }

    object AndroidX {
        const val appCompat = "androidx.appcompat:appcompat:1.2.0-rc01"
        const val coreKtx = "androidx.core:core-ktx:1.3.0"
        const val design = "com.google.android.material:material:1.2.0-rc01"
        const val constraint = "androidx.constraintlayout:constraintlayout:2.0.0-beta8"
        const val preference = "androidx.preference:preference-ktx:1.1.1"

        object Lifecycle {
            private const val version = "2.2.0"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
        }
    }

    object Timber {
        const val timber = "com.jakewharton.timber:timber:4.7.1"
    }

    object PermissionDispatcher {
        private const val version = "3.3.1"
        const val permissionDispatcher = "com.github.hotchemi:permissionsdispatcher:$version"
        const val processor = "com.github.hotchemi:permissionsdispatcher-processor:$version"
    }

    object BigDecimalMath {
        const val bigDecimalMath = "ch.obermuhlner:big-math:2.3.0"
    }

    object Exif {
        const val exifInterface = "androidx.exifinterface:exifinterface:1.2.0"
    }

    object Firebase {
        const val core = "com.google.firebase:firebase-core:17.4.4"
        const val crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1"
    }

    object Truth {
        const val truth = "com.google.truth:truth:1.0.1"
    }

    object MockK {
        const val mockK = "io.mockk:mockk:1.10.0"
    }
}