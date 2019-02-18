package com.geckour.flical.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.geckour.flical.R
import com.geckour.flical.databinding.ActivityMainBinding
import com.geckour.flical.ui.CrashlyticsEnabledActivity
import com.geckour.flical.ui.settings.SettingsActivity
import com.geckour.flical.util.getBgImageUri
import com.geckour.flical.util.observe
import com.geckour.flical.util.precision

class MainActivity : CrashlyticsEnabledActivity() {

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
        binding.formula.onTextPasted = {
            if (it != null) viewModel.onTextPasted(binding, it)
        }
        binding.formula.requestFocus()
        binding.formula.showSoftInputOnFocus = false
    }

    override fun onResume() {
        super.onResume()

        viewModel.injectBackgroundImage(binding, sharedPreferences.getBgImageUri())
    }

    private fun observeEvent() {
        viewModel.positionToMove.observe(this) {
            it ?: return@observe
            if (it > -1 && it <= binding.formula.text?.length ?: 0)
                binding.formula.setSelection(it)
        }
    }
}
