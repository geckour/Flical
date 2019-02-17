package com.geckour.flical.adapter

import androidx.databinding.BindingAdapter
import android.graphics.Bitmap
import android.widget.ImageView

@BindingAdapter("app:srcBitmap")
fun loadImage(imageView: ImageView, bitmap: Bitmap?) {
    imageView.setImageBitmap(bitmap)
}