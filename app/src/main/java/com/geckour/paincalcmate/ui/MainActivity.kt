package com.geckour.paincalcmate.ui

import android.databinding.DataBindingUtil
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.geckour.paincalcmate.model.Buttons
import com.geckour.paincalcmate.model.Command
import com.geckour.paincalcmate.model.ItemType
import com.geckour.paincalcmate.R
import com.geckour.paincalcmate.databinding.ActivityMainBinding
import com.geckour.paincalcmate.util.getDisplayString
import com.geckour.paincalcmate.util.parse

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var commandList: List<Command> = emptyList()

    private val mainBounds = RectF()

    private val bgBounds = Rect()

    private val buttons = Buttons(
            listOf(
                    listOf(
                            Buttons.Button(Command(ItemType.COPY, "COPY"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.PASTE, "PASTE"),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "7"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "4"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "1"),
                                    Command(ItemType.A_COS, "cos⁻¹"),
                                    Command(ItemType.A_SIN, "sin⁻¹"),
                                    Command(ItemType.A_TAN, "tan⁻¹"),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "."),
                                    Command(ItemType.NONE),
                                    Command(ItemType.PI, "π"),
                                    Command(ItemType.E, "e"),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED)
                    ),
                    listOf(
                            Buttons.Button(Command(ItemType.MR, "MR"),
                                    Command(ItemType.MC, "MC"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.M_MINUS, "M-"),
                                    Command(ItemType.M_PLUS, "M+"),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "8"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "5"),
                                    Command(ItemType.LEFT_BRA, "("),
                                    Command(ItemType.ABS, "ABS"),
                                    Command(ItemType.RIGHT_BRA, ")"),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "2"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "0"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.ZERO2, "00"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED)
                    ),
                    listOf(
                            Buttons.Button(Command(ItemType.DEL, "DEL"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.AC, "AC"),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "9"),
                                    Command(ItemType.DIFF, "÷"),
                                    Command(ItemType.X, "x"),
                                    Command(ItemType.INTEGRAL, "∫"),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "6"),
                                    Command(ItemType.LOG10, "log₁₀"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.LOG2, "log₂"),
                                    Command(ItemType.LN, "ln"),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.NUMBER, "3"),
                                    Command(ItemType.COS, "cos"),
                                    Command(ItemType.SIN, "sin"),
                                    Command(ItemType.TAN, "tan"),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.CALC, "="),
                                    Command(ItemType.NONE),
                                    Command(ItemType.ANS, "ANS"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED)
                    ),
                    listOf(
                            Buttons.Button(Command(ItemType.NONE),
                                    Command(ItemType.LEFT, "◀"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.RIGHT, "▶"),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.DIV, "÷"),
                                    Command(ItemType.MOD, "%"),
                                    Command(ItemType.SQRT, "√"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.MULTI, "×"),
                                    Command(ItemType.POW, "^"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.MINUS, "-"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED),
                            Buttons.Button(Command(ItemType.PLUS, "+"),
                                    Command(ItemType.SUM, "Σ"),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Command(ItemType.NONE),
                                    Buttons.Button.Area.UNDEFINED)
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
                    bgBounds.width() * -0.15f,
                    bgBounds.height() * -0.15f,
                    bgBounds.width() * 0.15f,
                    bgBounds.height() * 0.15f)
        }

        (0..buttons.list.lastIndex).forEach { column ->
            (0..buttons.list[0].lastIndex).forEach { row ->
                val id = resources.getIdentifier("bg$column$row", "id", packageName)
                findViewById<View>(id).setOnTouchListener { v, event -> onButtonTouch(v, event) }
            }
        }
    }

    private fun onButtonTouch(view: View, event: MotionEvent): Boolean {
        view.tag = when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> PointF(event.x, event.y)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> null

            else -> view.tag
        }


        buttons.list.forEach {
            it.forEach { it.tapped = Buttons.Button.Area.UNDEFINED }
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

        when (view.id) {
            R.id.bg00 -> buttons.list[0][0]
            R.id.bg10 -> buttons.list[1][0]
            R.id.bg20 -> buttons.list[2][0]
            R.id.bg30 -> buttons.list[3][0]
            R.id.bg01 -> buttons.list[0][1]
            R.id.bg11 -> buttons.list[1][1]
            R.id.bg21 -> buttons.list[2][1]
            R.id.bg31 -> buttons.list[3][1]
            R.id.bg02 -> buttons.list[0][2]
            R.id.bg12 -> buttons.list[1][2]
            R.id.bg22 -> buttons.list[2][2]
            R.id.bg32 -> buttons.list[3][2]
            R.id.bg03 -> buttons.list[0][3]
            R.id.bg13 -> buttons.list[1][3]
            R.id.bg23 -> buttons.list[2][3]
            R.id.bg33 -> buttons.list[3][3]
            R.id.bg04 -> buttons.list[0][4]
            R.id.bg14 -> buttons.list[1][4]
            R.id.bg24 -> buttons.list[2][4]
            R.id.bg34 -> buttons.list[3][4]
            else -> null
        }?.reflectState(area, event)

        binding.buttons = buttons

        return true
    }

    private fun Buttons.Button.reflectState(area: Buttons.Button.Area, event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                this.tapped = area
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val key = when (area) {
                    Buttons.Button.Area.MAIN -> this.main
                    Buttons.Button.Area.LEFT -> this.left
                    Buttons.Button.Area.TOP -> this.top
                    Buttons.Button.Area.RIGHT -> this.right
                    Buttons.Button.Area.BOTTOM -> this.bottom
                    Buttons.Button.Area.UNDEFINED -> null
                } ?: return

                commandList = key.parse(commandList)
                binding.inputField.setText(commandList.getDisplayString())
            }
        }
    }
}
