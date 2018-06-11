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
            BOTTOM
        }
    }
}