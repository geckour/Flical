package com.geckour.flical.ui.widget

import android.content.Context
import android.graphics.Rect
import androidx.appcompat.widget.AppCompatEditText
import android.util.AttributeSet
import kotlin.math.ceil
import kotlin.math.min

/**
 * Extended [AppCompatEditText] that supports ascent/baseline alignment.
 */
open class AlignedTextView
@JvmOverloads constructor(context: Context,
                          attrs: AttributeSet? = null,
                          defStyleAttr: Int = android.R.attr.textViewStyle)
    : AppCompatEditText(context, attrs, defStyleAttr) {

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
