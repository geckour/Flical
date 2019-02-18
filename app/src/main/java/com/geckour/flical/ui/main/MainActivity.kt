package com.geckour.flical.ui.main

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.geckour.flical.R
import com.geckour.flical.databinding.ActivityMainBinding
import com.geckour.flical.ui.settings.SettingsActivity
import com.geckour.flical.util.getBgImageUri
import com.geckour.flical.util.observe
import com.geckour.flical.util.precision
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this)[MainViewModel::class.java]
    }

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeEvent()

        precision = 20

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel.injectButtons(this, binding)
        binding.buttonSetting.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()

        injectBackgroundImage()
    }

    private fun observeEvent() {
        viewModel.positionToMove.observe(this) {
            it ?: return@observe
            binding.formula.cursorPosition = it
        }
    }

    private fun injectBackgroundImage() {
        sharedPreferences.getBgImageUri().apply {
            binding.background = this?.let {
                try {
                    BitmapFactory.decodeFile(it.path)
                } catch (t: Throwable) {
                    Timber.e(t)
                    null
                }
            }
        }
    }
}
