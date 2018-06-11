package com.geckour.paincalcmate

data class Buttons(
        val list: List<List<Button>>
) {
    data class Button(
            val mainText: String?,
            val leftText: String?,
            val topText: String?,
            val rightText: String?,
            val bottomText: String?,
            var tapped: Area?
    ) {
        enum class Area {
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