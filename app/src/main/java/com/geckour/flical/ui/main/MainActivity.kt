package com.geckour.flical.ui.main

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.geckour.flical.util.clearBgImageUri
import com.geckour.flical.util.deserialized
import com.geckour.flical.util.getBgImageUri
import com.geckour.flical.util.getDisplayString
import com.geckour.flical.util.getFlickSensitivity
import com.geckour.flical.util.getUIBias
import com.geckour.flical.util.setBgImageUri
import com.geckour.flical.util.setFlickSensitivity
import com.geckour.flical.util.setUIBias
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import permissions.dispatcher.RuntimePermissions
import kotlin.time.Duration.Companion.milliseconds

var montserrat: Typeface? = null
val fontFamily get() = montserrat?.let { FontFamily(it) }

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private var showingSettings = mutableStateOf(false)

    private val backgroundImagePath = mutableStateOf<String?>(null)
    private val flickSensitivity = mutableFloatStateOf(0.4f)
    private val uiBias = mutableFloatStateOf(0.5f)

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                sharedPreferences.setBgImageUri(this@MainActivity, it)
                backgroundImagePath.value = null
                delay(50.milliseconds)
                backgroundImagePath.value = sharedPreferences.getBgImageUri()?.path
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback {
            if (showingSettings.value) {
                flickSensitivity.floatValue = sharedPreferences.getFlickSensitivity()
                showingSettings.value = false
            }
        }

        montserrat = ResourcesCompat.getFont(this, R.font.montserrat)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        backgroundImagePath.value = sharedPreferences.getBgImageUri()?.path
        uiBias.floatValue = sharedPreferences.getUIBias()

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Calculator(
                    formulaText = viewModel.formulaText.value,
                    resultText = viewModel.resultCommands.value.getDisplayString(),
                    backgroundImagePath = backgroundImagePath.value,
                    cursorPosition = viewModel.formulaCursorPosition.value,
                    flickSensitivity = flickSensitivity.floatValue,
                    uiBias = uiBias.floatValue * 2 - 1,
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
                if (showingSettings.value) {
                    Settings(
                        generalSettings = listOf(
                            SettingsItem(
                                getString(R.string.settings_item_title_set_bg_image),
                                getString(R.string.settings_item_desc_set_bg_image),
                                onClick = {
                                    launcher.launch("image/*")
                                }
                            ),
                            SettingsItem(
                                getString(R.string.settings_item_title_clear_bg_image),
                                getString(R.string.settings_item_desc_clear_bg_image),
                                onClick = {
                                    sharedPreferences.clearBgImageUri()
                                    backgroundImagePath.value = null
                                }
                            ),
                        ),
                        presetFlickSensitivity = sharedPreferences.getFlickSensitivity(),
                        presetUIBias = sharedPreferences.getUIBias(),
                        onResetFlickSensitivity = {
                            sharedPreferences.setFlickSensitivity(it)
                            flickSensitivity.floatValue = it
                        },
                        onResetUIBias = {
                            sharedPreferences.setUIBias(it)
                            uiBias.floatValue = it
                        },
                        onFlickSensitivityValueChanged = sharedPreferences::setFlickSensitivity,
                        onUIBiasValueChanged = {
                            sharedPreferences.setUIBias(it)
                            uiBias.floatValue = it
                        }
                    )
                }
            }
        }
    }

    private fun openSettings() {
        showingSettings.value = true
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
}