package com.geckour.flical.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geckour.flical.util.setCrashlytics

abstract class CrashlyticsEnabledActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCrashlytics()
    }
}