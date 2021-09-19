package com.geckour.flical.util

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("srcBitmap")
fun loadImage(imageView: ImageView, bitmap: Bitmap?) {
    imageView.setImageBitmap(bitmap)
}