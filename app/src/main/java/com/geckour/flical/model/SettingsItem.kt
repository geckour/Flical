package com.geckour.flical.model

data class SettingsItem(
        val title: String,
        val desc: String,
        var summary: String? = null,
        val onClick: () -> Unit
)