package com.geckour.flical.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.geckour.flical.R
import com.geckour.flical.databinding.ActivitySettingsBinding
import com.geckour.flical.model.SettingsItem
import com.geckour.flical.util.setBgImageUri
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
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
            root.setOnClickListener { pickBgImageWithPermissionCheck() }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun pickBgImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        startActivityForResult(intent, RequestCode.REQUEST_CODE_PICK_MEDIA.ordinal)
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onStoragePermissionError() {
        pickBgImageWithPermissionCheck()
    }
}