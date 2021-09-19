package com.geckour.flical.ui.widget

import android.content.ClipboardManager
import android.content.Context
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.geckour.flical.R
import kotlin.math.min

class CalculatorFormula @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AlignedTextView(context, attrs, defStyleAttr) {

    // Temporary paint for use in layout methods.
    private val tempPaint = TextPaint()

    private val maximumTextSize: Float
    private val minimumTextSize: Float
    private val stepTextSize: Float

    private var widthConstraint = -1

    var onTextPasted: ((String?) -> Unit)? = null
    var onSelectionChanged: ((start: Int, end: Int) -> Unit)? = null

    private var cursorPosition: Int = 0
        get() = when {
            field < 0 || field > text?.length ?: 0 -> text?.length ?: 0
            else -> field
        }

    init {
        isSingleLine = true
        setTextIsSelectable(true)

        maximumTextSize = context.resources.getDimension(R.dimen.formula_max_textsize)
        minimumTextSize = context.resources.getDimension(R.dimen.formula_min_textsize)
        stepTextSize = context.resources.getDimension(R.dimen.formula_step_textsize)

        gravity = Gravity.CENTER_VERTICAL or Gravity.END
        setTextColor(context.getColor(R.color.primaryTextColor))

        showSoftInputOnFocus = false
        requestFocus()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isLaidOut) {
            // Prevent shrinking/resizing with our variable textSize.
            setTextSize(TypedValue.COMPLEX_UNIT_PX, maximumTextSize)
            minimumHeight = (lineHeight + compoundPaddingBottom + compoundPaddingTop)
        }

        // Ensure we are at least as big as our parent.
        val width = MeasureSpec.getSize(widthMeasureSpec)
        if (minimumWidth != width) {
            minimumWidth = width
        }

        // Re-calculate our textSize based on new width.
        widthConstraint = (MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight)
        val textSize = getVariableTextSize(text ?: "")
        if (getTextSize() != textSize) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text.toString()))
        cursorPosition = cursorPosition // Refresh
        setSelection(cursorPosition)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)

        cursorPosition = selStart
        onSelectionChanged?.invoke(selStart, selEnd)
    }

    private fun getVariableTextSize(text: CharSequence): Float {
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

    override fun onTextContextMenuItem(id: Int): Boolean {
        return when (id) {
            android.R.id.paste -> {
                val pasted = context?.getSystemService(ClipboardManager::class.java)
                    ?.primaryClip?.let {
                        if (it.itemCount > 0)
                            it.getItemAt(it.itemCount - 1).text.toString()
                        else null
                    }
                onTextPasted?.invoke(pasted)
                true
            }
            else -> super.onTextContextMenuItem(id)
        }
    }
}
