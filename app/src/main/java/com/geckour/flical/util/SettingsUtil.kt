package com.geckour.flical.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.edit
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

enum class SettingsKey {
    BG_IMAGE_URI,
    FLICK_SENSITIVITY,
    UI_BIAS
}

fun SharedPreferences.getBgImageUri(): Uri? =
    getSettingsValue<String>(SettingsKey.BG_IMAGE_URI)?.toUri()

fun SharedPreferences.setBgImageUri(context: Context, uri: Uri): String? {
    val dirName = "images"
    val fileName = "bg_image"
    val dir = File(context.filesDir, dirName)
    val file = File(dir, fileName)

    if (file.exists()) file.delete()
    if (dir.exists().not()) dir.mkdir()

    val bitmap = uri.extractMediaBitmap(context)
        ?.rotate(uri.getRotation(context))
        ?: return null

    FileOutputStream(file).use {
        val type = context.contentResolver.getType(uri)?.parseMimeType()
            ?: Bitmap.CompressFormat.JPEG
        bitmap.compress(type, 100, it)
        it.flush()

        bitmap.recycle()

        val result = Uri.fromFile(file).toString()
        edit(commit = true) {
            putString(SettingsKey.BG_IMAGE_URI.name, result)
        }
        return result
    }
}

fun SharedPreferences.getFlickSensitivity(): Float =
    getSettingsValue<Float>(SettingsKey.FLICK_SENSITIVITY) ?: 0.4f

fun SharedPreferences.setFlickSensitivity(sensitivity: Float) {
    edit {
        putFloat(SettingsKey.FLICK_SENSITIVITY.name, sensitivity)
    }
}

fun SharedPreferences.getUIBias(): Float =
    getSettingsValue<Float>(SettingsKey.UI_BIAS) ?: 0.5f

fun SharedPreferences.setUIBias(sensitivity: Float) {
    edit {
        putFloat(SettingsKey.UI_BIAS.name, sensitivity)
    }
}

private fun Uri.getRotation(context: Context): Int =
    context.contentResolver.openInputStream(this)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

private fun Bitmap.rotate(orientation: Int): Bitmap =
    Bitmap.createBitmap(
        this,
        0, 0,
        this.width, this.height,
        Matrix().apply {
            postRotate(
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            )
        },
        false
    )

fun SharedPreferences.clearBgImageUri() {
    edit().putString(SettingsKey.BG_IMAGE_URI.name, null).apply()
}

fun <T> SharedPreferences.getSettingsValue(key: SettingsKey): T? = all[key.name] as? T?