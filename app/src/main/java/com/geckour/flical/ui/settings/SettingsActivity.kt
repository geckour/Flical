package com.geckour.flical.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.geckour.flical.R
import com.geckour.flical.model.SettingsItem
import com.geckour.flical.util.clearBgImageUri
import com.geckour.flical.util.getFlickSensitivity
import com.geckour.flical.util.setBgImageUri
import com.geckour.flical.util.setFlickSensitivity
import kotlinx.coroutines.launch
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

private var montserrat: FontFamily? = null

@RuntimePermissions
class SettingsActivity : AppCompatActivity() {

    companion object {

        fun getIntent(context: Context): Intent =
            Intent(context, SettingsActivity::class.java)
    }

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

        montserrat = ResourcesCompat.getFont(this, R.font.montserrat)?.let { FontFamily(it) }

        setContent {
            Content(
                generalSettings = listOf(
                    SettingsItem(
                        getString(R.string.settings_item_title_set_bg_image),
                        getString(R.string.settings_item_desc_set_bg_image),
                        onClick = ::pickBgImageWithPermissionCheck
                    ),
                    SettingsItem(
                        getString(R.string.settings_item_title_clear_bg_image),
                        getString(R.string.settings_item_desc_clear_bg_image),
                        onClick = sharedPreferences::clearBgImageUri
                    ),
                ),
                defaultFlickSensitivity = sharedPreferences.getFlickSensitivity(),
                onFlickSensitivityValueChanged = sharedPreferences::setFlickSensitivity
            )
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
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

@Composable
@Preview
fun Content(
    generalSettings: List<SettingsItem> = emptyList(),
    defaultFlickSensitivity: Float = 0.4f,
    onFlickSensitivityValueChanged: (Float) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(backgroundColor = colorResource(id = R.color.primaryColor)) {
            Text(
                text = stringResource(id = R.string.title_settings),
                fontSize = 20.sp,
                color = Color.White
            )
        }
        Settings(
            scope = this,
            generalSettings = generalSettings,
            defaultFlickSensitivity = defaultFlickSensitivity,
            onFlickSensitivityValueChanged = onFlickSensitivityValueChanged
        )
    }
}

@Composable
fun Settings(
    scope: ColumnScope,
    generalSettings: List<SettingsItem>,
    defaultFlickSensitivity: Float,
    onFlickSensitivityValueChanged: (Float) -> Unit
) {
    with(scope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            generalSettings.forEach { GeneralSetting(it) }
            SliderSetting(
                title = stringResource(id = R.string.settings_item_title_flick_sensitivity),
                defaultValue = defaultFlickSensitivity,
                onValueChanged = onFlickSensitivityValueChanged
            )
        }
    }
}

@Composable
fun GeneralSetting(data: SettingsItem) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
            .clickable(onClick = data.onClick)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = data.title,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = montserrat
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = data.desc,
            fontSize = 14.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontFamily = montserrat
        )
        data.summary?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = montserrat
            )
        }
    }
}

@Composable
fun SliderSetting(title: String, defaultValue: Float, onValueChanged: (Float) -> Unit) {
    var sliderValue by remember { mutableStateOf(defaultValue) }
    Column(
        modifier = Modifier
            .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = montserrat
        )
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = sliderValue,
            onValueChange = {
                sliderValue = it
            },
            onValueChangeFinished = {
                onValueChanged(sliderValue)
            },
            colors = SliderDefaults.colors(
                thumbColor = colorResource(id = R.color.primaryColor),
                activeTrackColor = colorResource(id = R.color.primaryColor)
            )
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = sliderValue.toString(),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = montserrat
        )
    }
}