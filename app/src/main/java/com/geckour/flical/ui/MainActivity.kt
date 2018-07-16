package com.geckour.flical.ui

import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.geckour.flical.R
import com.geckour.flical.databinding.ActivityMainBinding
import com.geckour.flical.model.Buttons
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import com.geckour.flical.util.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var commandList: List<Command> = emptyList()

    private var memory: List<Command> = emptyList()

    private val mainBounds = RectF()

    private val bgBounds = Rect()

    private val buttons by lazy {
        Buttons(
                listOf(
                        listOf(
                                Buttons.Button(Command(ItemType.M, "M"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.MR, "MR"),
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
                                Buttons.Button(Command(ItemType.LEFT, "◀"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.NUMBER, "8"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.NUMBER, "5"),
                                        Command(ItemType.LEFT_BRA, "("),
                                        Command(ItemType.NONE),
                                        Command(ItemType.RIGHT_BRA, ")"),
                                        Command(ItemType.ABS, "ABS"),
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
                                        Command(ItemType.NUMBER, getString(R.string.tax)),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED)
                        ),
                        listOf(
                                Buttons.Button(Command(ItemType.RIGHT, "▶"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.NUMBER, "9"),
                                        Command(ItemType.LOG10, "log₁₀"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.LOG2, "log₂"),
                                        Command(ItemType.LN, "ln"),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.NUMBER, "6"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.NUMBER, "3"),
                                        Command(ItemType.COS, "cos"),
                                        Command(ItemType.SIN, "sin"),
                                        Command(ItemType.TAN, "tan"),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.CALC, "="),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
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
                                Buttons.Button(Command(ItemType.DIV, "÷"),
                                        Command(ItemType.MOD, "%"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.MULTI, "×"),
                                        Command(ItemType.POW, "^"),
                                        Command(ItemType.FACTOR, "!"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.SQRT, "√"),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.MINUS, "-"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED),
                                Buttons.Button(Command(ItemType.PLUS, "+"),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Command(ItemType.NONE),
                                        Buttons.Button.Area.UNDEFINED)
                        )
                )
        )
    }

    private val onInserted: (positionToMove: Int) -> Unit = {
        binding.formula.cursorPosition = it
    }

    private val onRemoved: (positionToMove: Int) -> Unit = {
        binding.formula.cursorPosition = it
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        precision = 20

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        injectButtons()
        binding.buttonSetting.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()

        injectBackgroundImage()
    }

    private fun injectButtons() {
        binding.buttons = buttons

        binding.bg00.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            bgBounds.set(0, 0, right - left, bottom - top)
            mainBounds.set(
                    bgBounds.width() * 0.3f,
                    bgBounds.height() * 0.3f,
                    bgBounds.width() * 0.7f,
                    bgBounds.height() * 0.7f)

            Timber.d("bgBounds: $bgBounds, mainBounds: $mainBounds")
        }

        (0..buttons.list.lastIndex).forEach { column ->
            (0..buttons.list[0].lastIndex).forEach { row ->
                val id = resources.getIdentifier("bg$column$row", "id", packageName)
                findViewById<View>(id).setOnTouchListener { v, event -> onButtonTouch(v, event) }
            }
        }
    }

    private fun onButtonTouch(view: View, event: MotionEvent): Boolean {
        val area =
                if (event.action == MotionEvent.ACTION_UP
                        || event.action == MotionEvent.ACTION_POINTER_UP) {
                    Buttons.Button.Area.UNDEFINED
                } else {
                    if (event.action == MotionEvent.ACTION_DOWN
                            || event.action == MotionEvent.ACTION_POINTER_DOWN
                            || mainBounds.contains(event.x, event.y)) {
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
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val command = when (this.tapped) {
                    Buttons.Button.Area.MAIN -> this.main
                    Buttons.Button.Area.LEFT -> this.left
                    Buttons.Button.Area.TOP -> this.top
                    Buttons.Button.Area.RIGHT -> this.right
                    Buttons.Button.Area.BOTTOM -> this.bottom
                    Buttons.Button.Area.UNDEFINED -> null
                } ?: return

                when {
                    command.type == ItemType.RIGHT -> {
                        binding.formula.cursorPosition++
                    }

                    command.type == ItemType.LEFT -> {
                        binding.formula.cursorPosition--
                    }

                    command.type == ItemType.M -> {
                        memory = commandList
                    }

                    command.type == ItemType.MR -> {
                        commandList = commandList.insert(memory, binding.formula.cursorPosition, onInserted)
                    }

                    command.type == ItemType.DEL -> {
                        commandList = commandList.remove(binding.formula.cursorPosition, onRemoved)
                    }
                }

                commandList =
                        if (command.isSpecial) commandList.invoke(command)
                        else commandList.insert(listOf(command), binding.formula.cursorPosition, onInserted).purify()

                if (command.type == ItemType.CALC || commandList.isEmpty()) {
                    binding.resultPreview.setText(null)
                } else {
                    val result = commandList.invoke(Command(ItemType.CALC))

                    if (commandList.normalize().size > 1
                            && result.lastOrNull()?.type == ItemType.NUMBER) {
                        binding.resultPreview.onEvaluate(result, precision)
                    } else {
                        binding.resultPreview.redisplay()
                    }
                }

                binding.formula.setText(commandList.getDisplayString())
            }
        }

        this.tapped = area
    }

    private fun injectBackgroundImage() {
        sharedPreferences.getBgImageUri().apply {
            binding.background = this?.let {
                try {
                    BitmapFactory.decodeFile(it.path)
                } catch (t: Throwable) {
                    Timber.e(t)
                    null
                }
            }
        }
    }
}
