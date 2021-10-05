package com.geckour.flical.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.flical.R
import com.geckour.flical.model.SettingsItem
import com.geckour.flical.ui.main.fontFamily

@Composable
fun Settings(
    generalSettings: List<SettingsItem>,
    defaultFlickSensitivity: Float,
    onFlickSensitivityValueChanged: (Float) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .clickable(enabled = false) {}
        .background(Color.White.copy(alpha = 0.75f))
    ) {
        TopAppBar(backgroundColor = colorResource(id = R.color.primaryColor)) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(id = R.string.title_settings),
                fontSize = 20.sp,
                color = Color.White
            )
        }
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
            fontFamily = fontFamily
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = data.desc,
            fontSize = 14.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontFamily = fontFamily
        )
        data.summary?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = fontFamily
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
            fontFamily = fontFamily
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
            fontFamily = fontFamily
        )
    }
}