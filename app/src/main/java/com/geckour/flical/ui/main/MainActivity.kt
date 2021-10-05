package com.geckour.flical.ui.main

import android.Manifest
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.geckour.flical.R
import com.geckour.flical.model.ItemType
import com.geckour.flical.model.SettingsItem
import com.geckour.flical.ui.compose.Calculator
import com.geckour.flical.ui.compose.Settings
import com.geckour.flical.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

var montserrat: Typeface? = null
val fontFamily get() = montserrat?.let { FontFamily(it) }

@RuntimePermissions
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private var showSettings = mutableStateOf(false)

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                sharedPreferences.setBgImageUri(this@MainActivity, it)
                viewModel.backgroundImagePath.value = null
                delay(50)
                viewModel.backgroundImagePath.value = sharedPreferences.getBgImageUri()?.path
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        montserrat = ResourcesCompat.getFont(this, R.font.montserrat)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.backgroundImagePath.value = sharedPreferences.getBgImageUri()?.path

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Calculator(
                    formulaText = viewModel.formulaText.value,
                    resultText = viewModel.resultCommands.value.getDisplayString(),
                    backgroundImagePath = viewModel.backgroundImagePath.value,
                    cursorPosition = viewModel.formulaCursorPosition.value,
                    flickSensitivity = viewModel.flickSensitivity.value,
                    onOpenSettings = ::openSettings,
                    onTextPasted = ::onTextPasted,
                    onCursorPositionRequested = viewModel::onCursorPositionChangedByUser
                ) { command ->
                    if (command.type == ItemType.NONE) return@Calculator

                    when (command.type) {
                        ItemType.RIGHT -> {
                            viewModel.moveCursorRight()
                        }
                        ItemType.LEFT -> {
                            viewModel.moveCursorLeft()
                        }
                        ItemType.M -> {
                            viewModel.updateMemory()
                        }
                        ItemType.MR -> {
                            viewModel.insertCommands(viewModel.memory)
                        }
                        ItemType.DEL -> {
                            viewModel.delete()
                        }
                        else -> Unit
                    }

                    viewModel.processCommand(command)
                }
                if (showSettings.value) {
                    Settings(
                        generalSettings = listOf(
                            SettingsItem(
                                getString(R.string.settings_item_title_set_bg_image),
                                getString(R.string.settings_item_desc_set_bg_image),
                                onClick = ::pickBgImageWithPermissionCheck
                            ),
                            SettingsItem(
                                getString(R.string.settings_item_title_clear_bg_image),
                                getString(R.string.settings_item_desc_clear_bg_image),
                                onClick = {
                                    sharedPreferences.clearBgImageUri()
                                    viewModel.backgroundImagePath.value = null
                                }
                            ),
                        ),
                        defaultFlickSensitivity = sharedPreferences.getFlickSensitivity(),
                        onFlickSensitivityValueChanged = sharedPreferences::setFlickSensitivity
                    )
                }
            }
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

    override fun onBackPressed() {
        if (showSettings.value) {
            viewModel.flickSensitivity.value = sharedPreferences.getFlickSensitivity()
            showSettings.value = false
        }
        else super.onBackPressed()
    }

    private fun openSettings() {
        showSettings.value = true
    }

    private fun onTextPasted(text: String?) {
        if (text.isNullOrBlank()) return

        val deserialized = text.deserialized()
        if (deserialized.isEmpty()) {
            Toast.makeText(this, R.string.toast_failed_paste, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.insertCommands(deserialized)
        viewModel.refreshResult()
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