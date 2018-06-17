/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geckour.flical.ui.view

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.widget.TextView
import kotlin.math.ceil
import kotlin.math.min

/**
 * Extended [TextView] that supports ascent/baseline alignment.
 */
open class AlignedTextView
@JvmOverloads constructor(context: Context,
                           attrs: AttributeSet? = null,
                           defStyleAttr: Int = android.R.attr.textViewStyle)
    : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private const val LATIN_CAPITAL_LETTER = "H"
    }

    // temporary rect for use during layout
    private val tempRect = Rect()

    private var topOffset: Int = 0
    private var bottomOffset: Int = 0

    init {

        // Disable any included font padding by default.
        includeFontPadding = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val paint = paint

        // Always align text to the default capital letter height.
        paint.getTextBounds(LATIN_CAPITAL_LETTER, 0, 1, tempRect)

        topOffset = min(paddingTop, ceil((tempRect.top - paint.ascent()).toDouble()).toInt())
        bottomOffset = min(paddingBottom, ceil(paint.descent().toDouble()).toInt())

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun getCompoundPaddingTop(): Int {
        return super.getCompoundPaddingTop() - topOffset
    }

    override fun getCompoundPaddingBottom(): Int {
        return super.getCompoundPaddingBottom() - bottomOffset
    }
}
