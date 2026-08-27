package com.geckour.flical.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import timber.log.Timber

fun Uri.extractMediaBitmap(context: Context): Bitmap? =
    try {
        if (Build.VERSION.SDK_INT > 27) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    context.contentResolver,
                    this
                )
            )
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
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