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
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView

import com.geckour.flical.R
import com.geckour.flical.model.Command
import com.geckour.flical.util.*
import kotlin.math.min

/**
 * TextView adapted for displaying the formula and allowing pasting.
 */
class CalculatorFormula
@JvmOverloads constructor(context: Context,
                          attrs: AttributeSet? = null,
                          defStyleAttr: Int = 0) : AlignedTextView(context, attrs, defStyleAttr) {

    // Temporary paint for use in layout methods.
    private val tempPaint = TextPaint()

    private val maximumTextSize: Float
    private val minimumTextSize: Float
    private val stepTextSize: Float

    private var widthConstraint = -1
    private var onTextSizeChangeListener: OnTextSizeChangeListener? = null

    init {
        val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.CalculatorFormula, defStyleAttr, 0)
        maximumTextSize = typedArray.getDimension(
                R.styleable.CalculatorFormula_maxTextSize, textSize)
        minimumTextSize = typedArray.getDimension(
                R.styleable.CalculatorFormula_minTextSize, textSize)
        stepTextSize = typedArray.getDimension(R.styleable.CalculatorFormula_stepTextSize,
                (maximumTextSize - minimumTextSize) / 3)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isLaidOut) {
            // Prevent shrinking/resizing with our variable textSize.
            setTextSizeInternal(TypedValue.COMPLEX_UNIT_PX, maximumTextSize, false)
            minimumHeight = (lineHeight + compoundPaddingBottom + compoundPaddingTop)
        }

        // Ensure we are at least as big as our parent.
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        if (minimumWidth != width) {
            minimumWidth = width
        }

        // Re-calculate our textSize based on new width.
        widthConstraint = (View.MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight)
        val textSize = getVariableTextSize(text)
        if (getTextSize() != textSize) {
            setTextSizeInternal(TypedValue.COMPLEX_UNIT_PX, textSize, false)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text.toString()))
    }

    private fun setTextSizeInternal(unit: Int, size: Float, notifyListener: Boolean) {
        val oldTextSize = textSize
        super.setTextSize(unit, size)
        if (notifyListener && onTextSizeChangeListener != null && textSize != oldTextSize) {
            onTextSizeChangeListener!!.onTextSizeChanged(this, oldTextSize)
        }
    }

    override fun setTextSize(unit: Int, size: Float) {
        setTextSizeInternal(unit, size, true)
    }

    fun getVariableTextSize(text: CharSequence): Float {
        if (widthConstraint < 0 || maximumTextSize <= minimumTextSize) {
            // Not measured, bail early.
            return textSize
        }

        // Capture current paint state.
        tempPaint.set(paint)

        // Step through increasing text sizes until the text would no longer fit.
        var lastFitTextSize = minimumTextSize
        while (lastFitTextSize < maximumTextSize) {
            tempPaint.textSize = min(lastFitTextSize + stepTextSize, maximumTextSize)
            if (Layout.getDesiredWidth(text, tempPaint) > widthConstraint) {
                break
            }
            lastFitTextSize = tempPaint.textSize
        }

        return lastFitTextSize
    }

    fun setOnTextSizeChangeListener(listener: OnTextSizeChangeListener) {
        onTextSizeChangeListener = listener
    }

    interface OnTextSizeChangeListener {
        fun onTextSizeChanged(textView: TextView, oldSize: Float)
    }
}
