package com.geckour.flical.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.crashlytics.android.Crashlytics
import com.geckour.flical.BuildConfig
import io.fabric.sdk.android.Fabric
import timber.log.Timber


fun Uri.extractMediaBitmap(context: Context): Bitmap? =
    try {
        MediaStore.Images.Media.getBitmap(context.contentResolver, this)
    } catch (t: Throwable) {
        Timber.e(t)
        null
    }

fun String.toUri(): Uri? =
    try {
        Uri.parse(this)
    } catch (t: Throwable) {
        Timber.e(t)
        null
    }

fun String.parseMimeType(): Bitmap.CompressFormat? =
    when (MimeTypeMap.getSingleton().getExtensionFromMimeType(this)) {
        "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
        "png" -> Bitmap.CompressFormat.PNG
        "webp" -> Bitmap.CompressFormat.WEBP
        else -> null
    }

fun Activity.setCrashlytics() {
    if (BuildConfig.DEBUG.not()) Fabric.with(this, Crashlytics())
}