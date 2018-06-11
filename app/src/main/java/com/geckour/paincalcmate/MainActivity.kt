package com.geckour.paincalcmate

import android.databinding.DataBindingUtil
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.geckour.paincalcmate.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainBounds = RectF()

    private val bgBounds = Rect()

    private val buttons = Buttons(
            listOf(
                    listOf(
                            Buttons.Button("COPY",
                                    null,
                                    "PASTE",
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("7",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("4",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("1",
                                    "cos⁻¹",
                                    "sin⁻¹",
                                    "tan⁻¹",
                                    null,
                                    null
                            ),
                            Buttons.Button(".",
                                    null,
                                    "π",
                                    "e",
                                    null,
                                    null
                            )
                    ),
                    listOf(
                            Buttons.Button("MR",
                                    "MC",
                                    "M-",
                                    "M+",
                                    null,
                                    null
                            ),
                            Buttons.Button("8",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("5",
                                    "(",
                                    "ABS",
                                    ")",
                                    null,
                                    null
                            ),
                            Buttons.Button("2",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("0",
                                    null,
                                    "00",
                                    null,
                                    null,
                                    null
                            )
                    ),
                    listOf(
                            Buttons.Button("DEL",
                                    null,
                                    "AC",
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("9",
                                    "d⁄dx",
                                    "x",
                                    "∫",
                                    null,
                                    null
                            ),
                            Buttons.Button("6",
                                    "log₁₀",
                                    null,
                                    "log₂",
                                    "ln",
                                    null
                            ),
                            Buttons.Button("3",
                                    "cos",
                                    "sin",
                                    "tan",
                                    null,
                                    null
                            ),
                            Buttons.Button("=",
                                    null,
                                    "ANS",
                                    null,
                                    null,
                                    null
                            )
                    ),
                    listOf(
                            Buttons.Button(null,
                                    "◀",
                                    "▲",
                                    "▶",
                                    "▼",
                                    null
                            ),
                            Buttons.Button("÷",
                                    "%",
                                    "√",
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("×",
                                    "^",
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("-",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ),
                            Buttons.Button("+",
                                    "Σ",
                                    null,
                                    null,
                                    null,
                                    null
                            )
                    )
            )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        injectButtons()
    }

    private fun injectButtons() {
        binding.buttons = buttons

        binding.bg00.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            bgBounds.set(0, 0, right - left, bottom - top)
            mainBounds.set(
                    bgBounds.width() * -0.2f,
                    bgBounds.height() * -0.2f,
                    bgBounds.width() * 0.2f,
                    bgBounds.height() * 0.2f)
        }

        (0..buttons.list.lastIndex).forEach { column ->
            (0..buttons.list[0].lastIndex).forEach { row ->
                val id = resources.getIdentifier("bg$column$row", "id", packageName)
                findViewById<View>(id).setOnTouchListener { v, event -> onButtonTapped(v, event) }
            }
        }
    }

    private fun onButtonTapped(view: View, event: MotionEvent): Boolean {
        view.tag = when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> PointF(event.x, event.y)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> null

            else -> view.tag
        }


        buttons.list.forEach {
            it.forEach { it.tapped = null }
        }

        val diP = (view.tag as? PointF?)?.let {
            PointF(it.x - event.x, it.y - event.y)
        } ?: PointF(0f, 0f)

        val area =
                if (event.action == MotionEvent.ACTION_DOWN
                        || event.action == MotionEvent.ACTION_POINTER_DOWN
                        || (bgBounds.contains(event.x.toInt(), event.y.toInt())
                                && mainBounds.contains(diP.x, diP.y))) {
                    Buttons.Button.Area.MAIN
                } else {
                    val x = event.x - bgBounds.centerX()
                    val y = (bgBounds.width() - event.y) - bgBounds.centerY()

                    if (y > x) {
                        if (y > -x) Buttons.Button.Area.TOP
                        else Buttons.Button.Area.LEFT
                    } else {
                        if (y > -x) Buttons.Button.Area.RIGHT
                        else Buttons.Button.Area.BOTTOM
                    }
                }

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                when (view.id) {
                    R.id.bg00 -> {
                        buttons.list[0][0].tapped = area
                    }
                    R.id.bg10 -> {
                        buttons.list[1][0].tapped = area
                    }
                    R.id.bg20 -> {
                        buttons.list[2][0].tapped = area
                    }
                    R.id.bg30 -> {
                        buttons.list[3][0].tapped = area
                    }
                    R.id.bg01 -> {
                        buttons.list[0][1].tapped = area
                    }
                    R.id.bg11 -> {
                        buttons.list[1][1].tapped = area
                    }
                    R.id.bg21 -> {
                        buttons.list[2][1].tapped = area
                    }
                    R.id.bg31 -> {
                        buttons.list[3][1].tapped = area
                    }
                    R.id.bg02 -> {
                        buttons.list[0][2].tapped = area
                    }
                    R.id.bg12 -> {
                        buttons.list[1][2].tapped = area
                    }
                    R.id.bg22 -> {
                        buttons.list[2][2].tapped = area
                    }
                    R.id.bg32 -> {
                        buttons.list[3][2].tapped = area
                    }
                    R.id.bg03 -> {
                        buttons.list[0][3].tapped = area
                    }
                    R.id.bg13 -> {
                        buttons.list[1][3].tapped = area
                    }
                    R.id.bg23 -> {
                        buttons.list[2][3].tapped = area
                    }
                    R.id.bg33 -> {
                        buttons.list[3][3].tapped = area
                    }
                    R.id.bg04 -> {
                        buttons.list[0][4].tapped = area
                    }
                    R.id.bg14 -> {
                        buttons.list[1][4].tapped = area
                    }
                    R.id.bg24 -> {
                        buttons.list[2][4].tapped = area
                    }
                    R.id.bg34 -> {
                        buttons.list[3][4].tapped = area
                    }
                }
            }
        }

        binding.buttons = buttons

        return true
    }
}
