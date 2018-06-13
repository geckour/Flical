package com.geckour.paincalcmate.util

import android.content.SharedPreferences
import android.net.Uri

enum class SettingsKey(val default: Any? = null) {
    BG_IMAGE_URI
}

fun SharedPreferences.getBgImageUri(): Uri? = getSettingsValue<String>(SettingsKey.BG_IMAGE_URI)?.toUri()
fun SharedPreferences.setBgImageUri(uri: Uri?) {
    edit().putString(SettingsKey.BG_IMAGE_URI.name, uri?.toString()).apply()
}

fun <T> SharedPreferences.getSettingsValue(key: SettingsKey): T? =
        if (contains(key.name))
            (all[key.name] as? T) ?: (key.default as? T)
        else key.default as? T