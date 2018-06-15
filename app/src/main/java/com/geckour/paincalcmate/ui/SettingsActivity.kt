package com.geckour.paincalcmate.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.geckour.paincalcmate.R
import com.geckour.paincalcmate.databinding.ActivitySettingsBinding
import com.geckour.paincalcmate.model.SettingsItem
import com.geckour.paincalcmate.util.setBgImageUri
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class SettingsActivity : AppCompatActivity() {

    private enum class RequestCode {
        REQUEST_CODE_PICK_MEDIA
    }

    companion object {
        fun getIntent(context: Context): Intent =
                Intent(context, SettingsActivity::class.java)
    }

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        supportActionBar?.setTitle(R.string.title_settings)

        binding.settingsItemSetBgImage.apply {
            data = SettingsItem(getString(R.string.settings_item_title_set_bg_image),
                    getString(R.string.settings_item_desc_set_bg_image))
            root.setOnClickListener { pickBgImage() }
        }

        binding.settingsItemClearBgImage.apply {
            data = SettingsItem(getString(R.string.settings_item_title_clear_bg_image),
                    getString(R.string.settings_item_desc_clear_bg_image))
            root.setOnClickListener { sharedPreferences.setBgImageUri() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCode.REQUEST_CODE_PICK_MEDIA.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.apply { sharedPreferences.setBgImageUri(this@SettingsActivity, this) }
                }
            }
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    private fun pickBgImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        startActivityForResult(intent, RequestCode.REQUEST_CODE_PICK_MEDIA.ordinal)
    }
}