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
import androidx.annotation.IdRes
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.geckour.flical.R
import com.geckour.flical.databinding.ActivityMainBinding
import com.geckour.flical.model.Buttons
import com.geckour.flical.model.ItemType
import com.geckour.flical.ui.CrashlyticsEnabledActivity
import com.geckour.flical.ui.settings.SettingsActivity
import com.geckour.flical.ui.widget.buttons
import com.geckour.flical.util.deserialize
import com.geckour.flical.util.getBgImageUri
import com.geckour.flical.util.observe
import com.geckour.flical.util.precision
import timber.log.Timber

class MainActivity : CrashlyticsEnabledActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val mainBounds = RectF()
    private val bgBounds = Rect()

    private val onButtonTouch: (View, MotionEvent) -> Boolean = { view, event ->
        val area = when {
            event.action == MotionEvent.ACTION_UP ||
                    event.action == MotionEvent.ACTION_POINTER_UP -> {
                Buttons.Button.Area.UNDEFINED
            }
            mainBounds.contains(event.x, event.y) ||
                    event.action == MotionEvent.ACTION_DOWN ||
                    event.action == MotionEvent.ACTION_POINTER_DOWN -> {
                Buttons.Button.Area.MAIN
            }
            else -> {
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

        val indices = getButtonIndicesById(view.id)
        buttons.list[indices.first][indices.second]
            .reflectState(event)
            .tapped = area

        binding.buttons = buttons

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        injectButtons()
        binding.buttonSetting.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }
        binding.formula.apply {
            onTextPasted = { if (it != null) onTextPasted(it) }
            onSelectionChanged = { start, end -> viewModel.onSelectionChangedByUser(start, end) }
            requestFocus()
            showSoftInputOnFocus = false
        }

        binding.resultPreview.setOnLongClickListener { onLongClickResult() }

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

    private fun getButtonIndicesById(@IdRes buttonId: Int): Pair<Int, Int> =
        resources.getResourceName(buttonId).let {
            val matcher = Regex(".*bg(\\d)(\\d)")
            val row = it.replace(matcher, "$1").toInt()
            val col = it.replace(matcher, "$2").toInt()

            row to col
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
                findViewById<View>(id).setOnTouchListener(onButtonTouch)
            }
        }
    }

    private fun Buttons.Button.reflectState(event: MotionEvent): Buttons.Button {
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

    private fun onLongClickResult(): Boolean {
        val text = viewModel.resultCommands.value?.last()?.text
        if (text.isNullOrBlank()) return false

        getSystemService(ClipboardManager::class.java)?.setPrimaryClip(
            ClipData.newPlainText(null, text)
        )
        Toast.makeText(this, R.string.toast_completed_copy, Toast.LENGTH_SHORT).show()

        return true
    }
}
