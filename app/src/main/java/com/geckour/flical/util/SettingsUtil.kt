package com.geckour.flical.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class SettingsKey(val default: Any? = null) {
    BG_IMAGE_URI
}

fun SharedPreferences.getBgImageUri(): Uri? = getSettingsValue<String>(SettingsKey.BG_IMAGE_URI)?.toUri()
fun SharedPreferences.setBgImageUri(context: Context, coroutineScope: CoroutineScope, uri: Uri) {
    coroutineScope.launch {
        val dirName = "images"
        val fileName = "bg_image"
        val dir = File(context.filesDir, dirName)
        val file = File(dir, fileName)

        if (file.exists()) file.delete()
        if (dir.exists().not()) dir.mkdir()

        val bitmap = uri.extractMediaBitmap(context) ?: return@launch

        FileOutputStream(file).use {
            val type = context.contentResolver.getType(uri)?.parseMimeType()
                    ?: Bitmap.CompressFormat.JPEG
            bitmap.compress(type, 100, it)
            it.flush()

            bitmap.recycle()

            edit().putString(SettingsKey.BG_IMAGE_URI.name, Uri.fromFile(file).toString()).apply()
        }
    }
}

fun SharedPreferences.setBgImageUri() {
    edit().putString(SettingsKey.BG_IMAGE_URI.name, null).apply()
}

fun <T> SharedPreferences.getSettingsValue(key: SettingsKey): T? =
        if (contains(key.name))
            (all[key.name] as? T) ?: (key.default as? T)
        else key.default as? T