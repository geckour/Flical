/*
 * Copyright (C) 2016 The Android Open Source Project
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
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView

import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import kotlin.math.max

class CalculatorScrollView
@JvmOverloads constructor(context: Context,
                          attrs: AttributeSet? = null,
                          defStyleAttr: Int = 0)
    : HorizontalScrollView(context, attrs, defStyleAttr) {

    private fun getChildMeasureSpecCompat(spec: Int, padding: Int, childDimension: Int): Int {
        if (View.MeasureSpec.getMode(spec) == UNSPECIFIED
                && (childDimension == MATCH_PARENT || childDimension == WRAP_CONTENT)) {
            val size = max(0, View.MeasureSpec.getSize(spec) - padding)
            return View.MeasureSpec.makeMeasureSpec(size, UNSPECIFIED)
        }
        return ViewGroup.getChildMeasureSpec(spec, padding, childDimension)
    }

    override fun measureChild(child: View, spec: Int,
                              parentHeightMeasureSpec: Int) {
        // Allow child to be as wide as they want.
        val wSpec = View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(spec), UNSPECIFIED)

        val lp = child.layoutParams
        val childWidthMeasureSpec = getChildMeasureSpecCompat(wSpec,
                0 /* padding */, lp.width)
        val childHeightMeasureSpec = getChildMeasureSpecCompat(parentHeightMeasureSpec,
                paddingTop + paddingBottom, lp.height)

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun measureChildWithMargins(child: View, parentWidthMeasureSpec: Int, widthUsed: Int,
                                         parentHeightMeasureSpec: Int, heightUsed: Int) {

        // Allow child to be as wide as they want.
        val wSpec = View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(parentWidthMeasureSpec), UNSPECIFIED)

        val lp = child.layoutParams as ViewGroup.MarginLayoutParams
        val childWidthMeasureSpec = getChildMeasureSpecCompat(wSpec,
                lp.leftMargin + lp.rightMargin, lp.width)
        val childHeightMeasureSpec = getChildMeasureSpecCompat(parentHeightMeasureSpec,
                paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin, lp.height)

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }
}
