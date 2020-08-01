package com.geckour.flical.model

enum class ItemType(val weight: Int? = null, val defaultText: String? = null) {
    NONE,
    POSITIVE_INFINITY,
    NEGATIVE_INFINITY,
    NAN,
    NUMBER(0),
    PI(0, "π"),
    E(0, "e"),
    LEFT_BRA(defaultText = "("),
    RIGHT_BRA(defaultText = ")"),
    PLUS(4, "+"),
    MINUS(4, "-"),
    MULTI(3, "×"),
    DIV(3, "÷"),
    POW(2, "^"),
    FACTOR(2, "!"),
    MOD(2, "%"),
    SQRT(1, "√"),
    SIN(1, "sin"),
    COS(1, "cos"),
    TAN(1, "tan"),
    A_SIN(1, "sin⁻¹"),
    A_COS(1, "cos⁻¹"),
    A_TAN(1, "tan⁻¹"),
    LN(1, "ln"),
    LOG10(1, "log₁₀"),
    LOG2(1, "log₂"),
    ABS(1, "ABS"),
    M(defaultText = "M"),
    MR(defaultText = "MR"),
    DEL(defaultText = "DEL"),
    AC(defaultText = "AC"),
    LEFT(defaultText = "◀"),
    RIGHT(defaultText = "▶"),
    CALC(defaultText = "=");

    companion object {

        fun from(text: String): ItemType? = ItemType.values().find { it.defaultText == text }
    }
}