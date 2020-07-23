package com.geckour.flical.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.geckour.flical.R
import com.geckour.flical.databinding.ActivityMainBinding
import com.geckour.flical.model.Buttons
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import com.geckour.flical.ui.CrashlyticsEnabledActivity
import com.geckour.flical.ui.settings.SettingsActivity
import com.geckour.flical.ui.widget.CalculatorResult
import com.geckour.flical.util.*
import timber.log.Timber

class MainActivity : CrashlyticsEnabledActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val mainBounds = RectF()
    private val bgBounds = Rect()

    private val buttons =
        Buttons(
            listOf(
                listOf(
                    Buttons.Button(
                        Command(ItemType.M),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.MR),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "7"),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "4"),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "1"),
                        Command(ItemType.A_COS),
                        Command(ItemType.A_SIN),
                        Command(ItemType.A_TAN),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "."),
                        Command(ItemType.NONE),
                        Command(ItemType.PI),
                        Command(ItemType.E),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    )
                ),
                listOf(
                    Buttons.Button(
                        Command(ItemType.LEFT),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "8"),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "5"),
                        Command(ItemType.LEFT_BRA),
                        Command(ItemType.NONE),
                        Command(ItemType.RIGHT_BRA),
                        Command(ItemType.ABS),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "2"),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "0"),
                        Command(ItemType.NONE),
                        Command(ItemType.NUMBER, "00"),
                        Command(ItemType.NUMBER, "1.08"),
                        Command(ItemType.NUMBER, "1.1"),
                        Buttons.Button.Area.UNDEFINED
                    )
                ),
                listOf(
                    Buttons.Button(
                        Command(ItemType.RIGHT),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "9"),
                        Command(ItemType.LOG10),
                        Command(ItemType.NONE),
                        Command(ItemType.LOG2),
                        Command(ItemType.LN),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "6"),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.NUMBER, "3"),
                        Command(ItemType.COS),
                        Command(ItemType.SIN),
                        Command(ItemType.TAN),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.CALC),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    )
                ),
                listOf(
                    Buttons.Button(
                        Command(ItemType.DEL),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.AC),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.DIV),
                        Command(ItemType.MOD),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.MULTI),
                        Command(ItemType.POW),
                        Command(ItemType.FACTOR),
                        Command(ItemType.NONE),
                        Command(ItemType.SQRT),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.MINUS),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    ),
                    Buttons.Button(
                        Command(ItemType.PLUS),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Command(ItemType.NONE),
                        Buttons.Button.Area.UNDEFINED
                    )
                )
            )
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        precision = 20

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        injectButtons()
        binding.buttonSetting.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }
        binding.formula.apply {
            onTextPasted = {
                if (it != null) onTextPasted(it)
            }
            onSelectionChanged = { start, end ->
                viewModel.onSelectionChangedByUser(start, end)
            }
            requestFocus()
            showSoftInputOnFocus = false
        }

        binding.resultPreview.setOnLongClickListener {
            onLongClickResult(it as CalculatorResult)
        }

        observeEvents()
    }

    override fun onResume() {
        super.onResume()

        injectBackgroundImage(PreferenceManager.getDefaultSharedPreferences(this).getBgImageUri())
    }

    private fun observeEvents() {
        viewModel.formulaCursorPosition.observe(this) {
            it ?: return@observe

            binding.formula.setSelection(it)
        }

        viewModel.formulaText.observe(this) {
            binding.formula.setText(it)
        }

        viewModel.resultCommands.observe(this) {
            if (it.isNullOrEmpty()) {
                binding.resultPreview.redisplay()
            } else {
                binding.resultPreview.onEvaluate(it, precision)
            }
        }
    }

    private fun injectButtons() {
        binding.buttons = buttons
        binding.bg00.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            bgBounds.set(0, 0, right - left, bottom - top)
            mainBounds.set(
                bgBounds.width() * 0.2f,
                bgBounds.height() * 0.2f,
                bgBounds.width() * 0.8f,
                bgBounds.height() * 0.8f
            )
        }

        repeat(buttons.list.size) { column ->
            repeat(buttons.list[0].size) { row ->
                val id = resources.getIdentifier("bg$column$row", "id", packageName)
                findViewById<View>(id).setOnTouchListener { v, event ->
                    onButtonTouch(v, event)
                }
            }
        }
    }

    private fun onButtonTouch(
        view: View,
        event: MotionEvent
    ): Boolean {
        val area =
            if (event.action == MotionEvent.ACTION_UP
                || event.action == MotionEvent.ACTION_POINTER_UP
            ) {
                Buttons.Button.Area.UNDEFINED
            } else {
                if (event.action == MotionEvent.ACTION_DOWN
                    || event.action == MotionEvent.ACTION_POINTER_DOWN
                    || mainBounds.contains(event.x, event.y)
                ) {
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
        }
            ?.reflectState(event)
            ?.tapped = area

        binding.buttons = buttons

        return true
    }

    private fun Buttons.Button.reflectState(
        event: MotionEvent
    ): Buttons.Button {
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val command = when (this.tapped) {
                    Buttons.Button.Area.MAIN -> this.main
                    Buttons.Button.Area.LEFT -> this.left
                    Buttons.Button.Area.TOP -> this.top
                    Buttons.Button.Area.RIGHT -> this.right
                    Buttons.Button.Area.BOTTOM -> this.bottom
                    Buttons.Button.Area.UNDEFINED -> null
                } ?: return this

                if (command.type == ItemType.NONE) return this

                when (command.type) {
                    ItemType.RIGHT -> {
                        viewModel.moveCursorRight()
                    }
                    ItemType.LEFT -> {
                        viewModel.moveCursorLeft()
                    }
                    ItemType.M -> {
                        viewModel.updateMemory()
                    }
                    ItemType.MR -> {
                        viewModel.insertCommands(viewModel.memory)
                    }
                    ItemType.DEL -> {
                        viewModel.delete()
                    }
                }

                viewModel.processCommand(command)
            }
        }

        return this
    }

    private fun onTextPasted(text: String) {
        if (text.isBlank()) return

        val deserialized = text.deserialize()
        if (deserialized.isEmpty()) {
            Toast.makeText(binding.root.context, R.string.toast_failed_paste, Toast.LENGTH_SHORT)
                .show()
            return
        }
        viewModel.insertCommands(deserialized)
        viewModel.refreshResult()
    }

    private fun injectBackgroundImage(uri: Uri?) {
        binding.background = uri?.let {
            try {
                BitmapFactory.decodeFile(it.path)
            } catch (t: Throwable) {
                Timber.e(t)
                null
            }
        }
    }

    private fun onLongClickResult(resultView: CalculatorResult): Boolean {
        val resultText = resultView.text?.toString()
        if (resultText.isNullOrBlank()) return false

        resultView.context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(
            ClipData.newPlainText(null, resultText.replace(Regex("^=\\s*(.+?)$"), "$1"))
        )
        Toast.makeText(resultView.context, R.string.toast_completed_copy, Toast.LENGTH_SHORT).show()

        return true
    }
}
