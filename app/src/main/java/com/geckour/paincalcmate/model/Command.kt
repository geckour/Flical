package com.geckour.paincalcmate.model

data class Command(
        val type: ItemType,
        val text: String? = null
)