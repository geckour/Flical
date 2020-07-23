package com.geckour.flical.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

enum class SettingsKey(val default: Any? = null) {
    BG_IMAGE_URI
}

fun SharedPreferences.getBgImageUri(): Uri? =
    getSettingsValue<String>(SettingsKey.BG_IMAGE_URI)?.toUri()

fun SharedPreferences.setBgImageUri(context: Context, uri: Uri) {
    val dirName = "images"
    val fileName = "bg_image"
    val dir = File(context.filesDir, dirName)
    val file = File(dir, fileName)

    if (file.exists()) file.delete()
    if (dir.exists().not()) dir.mkdir()

    val bitmap = uri.extractMediaBitmap(context)
        ?.rotate(uri.getRotation(context))
        ?: return

    FileOutputStream(file).use {
        val type = context.contentResolver.getType(uri)?.parseMimeType()
            ?: Bitmap.CompressFormat.JPEG
        bitmap.compress(type, 100, it)
        it.flush()

        bitmap.recycle()

        edit().putString(SettingsKey.BG_IMAGE_URI.name, Uri.fromFile(file).toString()).apply()
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

fun <T> SharedPreferences.getSettingsValue(key: SettingsKey): T? =
    if (contains(key.name))
        (all[key.name] as? T) ?: (key.default as? T)
    else key.default as? T