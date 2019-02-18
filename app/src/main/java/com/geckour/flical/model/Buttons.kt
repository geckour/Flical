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
            val bottom: Command,
            var tapped: Area
    ) {
        enum class Area {
            UNDEFINED,
            MAIN,
            LEFT,
            TOP,
            RIGHT,
            BOTTOM;
        }

        fun getBgResId(): Int = when (tapped) {
            Area.MAIN -> R.drawable.bg_button_highlight_main
            Area.LEFT -> R.drawable.bg_button_highlight_left
            Area.TOP -> R.drawable.bg_button_highlight_top
            Area.RIGHT -> R.drawable.bg_button_highlight_right
            Area.BOTTOM -> R.drawable.bg_button_highlight_bottom
            else -> 0
        }
    }
}