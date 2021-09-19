package com.geckour.flical.model

import com.geckour.flical.R

data class Buttons(
    val list: List<List<Button>>
) {
    data class Button(
        val main: Command,
        val left: Command,
        val top: Command,
        val right: Command,
        val bottom: Command
    ) {
        enum class Area {
            UNDEFINED,
            MAIN,
            LEFT,
            TOP,
            RIGHT,
            BOTTOM;

            val bgResId
                get(): Int? = when (this) {
                    MAIN -> R.drawable.bg_button_highlight_main
                    LEFT -> R.drawable.bg_button_highlight_left
                    TOP -> R.drawable.bg_button_highlight_top
                    RIGHT -> R.drawable.bg_button_highlight_right
                    BOTTOM -> R.drawable.bg_button_highlight_bottom
                    else -> null
                }
        }
    }
}