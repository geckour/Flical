package com.geckour.flical.ui.view

import android.content.Context
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import com.geckour.flical.util.getDisplayString
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round

class CalculatorResult(context: Context, attrs: AttributeSet) : AlignedTextView(context, attrs) {

    companion object {
        internal const val MAX_RIGHT_SCROLL = 10000000
        internal const val INVALID = MAX_RIGHT_SCROLL + 10000

        // Compute maximum digit width the hard way.
        private fun getMaxDigitWidth(paint: TextPaint): Float {
            // Compute the maximum advance width for each digit, thus accounting for between-character
            // spaces. If we ever support other kinds of digits, we may have to avoid kerning effects
            // that could reduce the advance width within this particular string.
            val allDigits = "0123456789"
            val widths = FloatArray(allDigits.length).apply {
                paint.getTextWidths(allDigits, this)
            }
            return widths.max() ?: 0f
        }
    }

    // A larger value is unlikely to avoid running out of space
    internal val scroller: OverScroller = OverScroller(context)
    internal val gestureDetector: GestureDetector = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float,
                                     velocityY: Float): Boolean {
                    if (!scroller.isFinished) {
                        currentPos = scroller.finalX
                    }
                    scroller.forceFinished(true)
                    this@CalculatorResult.cancelLongPress()
                    // Ignore scrolls of error string, etc.
                    if (!isScrollable) return true
                    scroller.fling(currentPos, 0,
                            -velocityX.toInt(), 0  /* horizontal only */,
                            minPos, maxPos, 0, 0)
                    postInvalidateOnAnimation()
                    return true
                }

                override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float,
                                      distanceY: Float): Boolean {
                    var distance = distanceX.toInt()
                    if (!scroller.isFinished) {
                        currentPos = scroller.finalX
                    }
                    scroller.forceFinished(true)
                    this@CalculatorResult.cancelLongPress()
                    if (!isScrollable) return true
                    if (currentPos + distance < minPos) {
                        distance = minPos - currentPos
                    } else if (currentPos + distance > maxPos) {
                        distance = maxPos - currentPos
                    }
                    var duration = (e2.eventTime - e1.eventTime).toInt()
                    if (duration < 1 || duration > 100) duration = 10
                    scroller.startScroll(currentPos, 0, distance, 0, duration)
                    postInvalidateOnAnimation()
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    performLongClick()
                }
            })
    var isScrollable = false
        private set
    // A suffix of "Pos" denotes a pixel offset.  Zero represents a scroll position
    // in which the decimal point is just barely visible on the right of the display.
    private var currentPos: Int = 0
    // Position of right of display relative to decimal point, in pixels.
    // Large positive values mean the decimal point is scrolled off the
    // left of the display.  Zero means decimal point is barely displayed
    // on the right.
    private var lastPos: Int = 0   // Position already reflected in display. Pixels.
    private var minPos: Int = 0    // Minimum position to avoid unnecessary blanks on the left. Pixels.
    private var maxPos: Int = 0    // Maximum position before we start displaying the infinite
    private val widthLock = Any()
    private var widthConstraint: Int = 0
    private var charWidth = 1f

    private val maxChars: Int
        get() = synchronized(widthLock) {
            return floor((widthConstraint / charWidth).toDouble()).toInt()
        }

    init {
        val slop = ViewConfiguration.get(context).scaledTouchSlop
        setOnTouchListener(object : View.OnTouchListener {
            // Used to determine whether a touch event should be intercepted.
            private var initialDownX: Float = 0f
            private var initialDownY: Float = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val action = event.actionMasked

                val x = event.x
                val y = event.y
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialDownX = x
                        initialDownY = y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = abs(x - initialDownX)
                        val deltaY = abs(y - initialDownY)
                        if (deltaX > slop && deltaX > deltaY) {
                            // Prevent the DragLayout from intercepting horizontal scrolls.
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                }
                return gestureDetector.onTouchEvent(event)
            }
        })

        isCursorVisible = false
        isLongClickable = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isLaidOut) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            // Set a minimum height so scaled error messages won't affect our layout.
            minimumHeight = (lineHeight + compoundPaddingBottom
                    + compoundPaddingTop)
        }

        val paint = paint
        val newCharWidth = getMaxDigitWidth(paint)
        val newWidthConstraint = MeasureSpec.getSize(widthMeasureSpec) - (paddingLeft + paddingRight)

        synchronized(widthLock) {
            charWidth = newCharWidth
            widthConstraint = newWidthConstraint
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setText(text: String?) {
        if (text == null) clear()

        super.setText(text, null)
    }

    /**
     * Initiate display of a new result.
     * Only called from UI thread.
     * The parameters specify various properties of the result.
     *
     * @param initPrecision Initial display precision computed by evaluator. (1 = tenths digit)
     */
    fun onEvaluate(commands: List<Command>, initPrecision: Int) {
        initPositions(commands, initPrecision)
        redisplay(commands.getDisplayString())
    }

    private fun initPositions(commands: List<Command>, initPrecisionOffset: Int) {
        val whole = commands.getDisplayString()
        val maxChars = maxChars
        val mWholeLen = whole.length

        val mWholePartFits = mWholeLen <= maxChars
        lastPos = INVALID
        // Prevent scrolling past initial position, which is calculated to show leading digits.
        minPos = round(initPrecisionOffset * charWidth).toInt()
        currentPos = minPos
        // Possible zero value
        val mMaxCharOffset: Int
        val negative = whole.firstOrNull() == '-'
        if (negative) {
            if (mWholeLen < MAX_RIGHT_SCROLL) {
                mMaxCharOffset = mWholeLen
                isScrollable = mMaxCharOffset >= maxChars
                if (mWholePartFits || isScrollable) {
                    maxPos = min(Math.round(mMaxCharOffset * charWidth), MAX_RIGHT_SCROLL)
                }
                if (!isScrollable) {
                    // Position the number consistently with our assumptions to make sure it
                    // actually fits.
                    currentPos = maxPos
                }
            } else {
                maxPos = MAX_RIGHT_SCROLL
                isScrollable = true
            }
        } else {
            val last = commands.lastOrNull()
            if (last?.type === ItemType.NUMBER && last.text == "0") {
                // Definite zero value.
                maxPos = minPos
                isScrollable = false
            } else {
                // May be very small nonzero value.  Allow user to find out.
                maxPos = MAX_RIGHT_SCROLL
                minPos -= charWidth.toInt()  // Allow for future minus sign.
                isScrollable = true
            }
        }
    }

    /**
     * Map pixel position to digit offset.
     */
    private fun getCharOffset(pos: Int): Int {
        return round(pos / charWidth).toInt()  // Lock not needed.
    }

    private fun clear() {
        isScrollable = false
        isLongClickable = false
    }

    fun redisplay(text: String? = null) {
        if (maxChars < 4) {
            // Display currently too small to display a reasonable result. Punt to avoid crash.
            return
        }
        if (scroller.isFinished && length() > 0) {
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        setText(text)
        if (text.isNullOrEmpty().not()) {
            isLongClickable = true
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int,
                               lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (!isScrollable || scroller.isFinished) {
            if (lengthBefore == 0 && lengthAfter > 0) {
                accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
                contentDescription = null
            } else if (lengthBefore > 0 && lengthAfter == 0) {
                accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_NONE
            }
        }
    }

    override fun computeScroll() {
        if (!isScrollable) {
            return
        }

        if (scroller.computeScrollOffset()) {
            currentPos = scroller.currX
            if (getCharOffset(currentPos) != getCharOffset(lastPos)) {
                lastPos = currentPos
                redisplay(text.toString())
            }
        }

        if (!scroller.isFinished) {
            postInvalidateOnAnimation()
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_NONE
        } else if (length() > 0) {
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
    }
}
