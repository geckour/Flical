package com.geckour.flical.model

data class Command(
        val type: ItemType,
        val text: String? = null
)