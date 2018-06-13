package com.geckour.paincalcmate.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.geckour.paincalcmate.R
import com.geckour.paincalcmate.databinding.ActivitySettingsBinding
import com.geckour.paincalcmate.model.SettingsItem

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    companion object {
        fun getIntent(context: Context): Intent =
                Intent(context, SettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        supportActionBar?.setTitle(R.string.title_settings)

        binding.settingsItemBgImage.apply {
            data = SettingsItem(getString(R.string.settings_item_title_bg_image),
                    getString(R.string.settings_item_desc_bg_image))
            root.setOnClickListener { pickBgImage() }
        }
    }

    private fun pickBgImage() {
        
    }
}