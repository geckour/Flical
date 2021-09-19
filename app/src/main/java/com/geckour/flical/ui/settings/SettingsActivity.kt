package com.geckour.flical.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.geckour.flical.R
import com.geckour.flical.databinding.ActivitySettingsBinding
import com.geckour.flical.model.SettingsItem
import com.geckour.flical.util.clearBgImageUri
import com.geckour.flical.util.setBgImageUri
import kotlinx.coroutines.launch
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class SettingsActivity : AppCompatActivity() {

    companion object {

        fun getIntent(context: Context): Intent =
            Intent(context, SettingsActivity::class.java)
    }

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                sharedPreferences.setBgImageUri(this@SettingsActivity, it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setSupportActionBar(binding.toolbar)

        binding.settingsItemSetBgImage.apply {
            data = SettingsItem(
                getString(R.string.settings_item_title_set_bg_image),
                getString(R.string.settings_item_desc_set_bg_image)
            )
            root.setOnClickListener { pickBgImageWithPermissionCheck() }
        }

        binding.settingsItemClearBgImage.apply {
            data = SettingsItem(
                getString(R.string.settings_item_title_clear_bg_image),
                getString(R.string.settings_item_desc_clear_bg_image)
            )
            root.setOnClickListener { sharedPreferences.clearBgImageUri() }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun pickBgImage() {
        launcher.launch("image/*")
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onStoragePermissionError() {
        pickBgImageWithPermissionCheck()
    }
}