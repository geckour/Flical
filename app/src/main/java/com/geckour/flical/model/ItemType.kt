package com.geckour.flical.model

enum class ItemType(
        val weight: Int? = null,
        val defaultText: String? = null,
        val isDeserializable: Boolean = false
) {
    NONE,
    POSITIVE_INFINITY,
    NEGATIVE_INFINITY,
    NAN,
    NUMBER(0),
    PI(0, "π", true),
    E(0, "e", true),
    LEFT_BRA(defaultText = "(", isDeserializable = true),
    RIGHT_BRA(defaultText = ")", isDeserializable = true),
    PLUS(4, "+", true),
    MINUS(4, "-", true),
    MULTI(3, "×", true),
    DIV(3, "÷", true),
    POW(2, "^", true),
    FACTOR(2, "!", true),
    MOD(2, "%", true),
    SQRT(1, "√", true),
    SIN(1, "sin", true),
    COS(1, "cos", true),
    TAN(1, "tan", true),
    A_SIN(1, "sin⁻¹", true),
    A_COS(1, "cos⁻¹", true),
    A_TAN(1, "tan⁻¹", true),
    LN(1, "ln", true),
    LOG10(1, "log₁₀", true),
    LOG2(1, "log₂", true),
    ABS(1, "ABS", true),
    M(defaultText = "M"),
    MR(defaultText = "MR"),
    DEL(defaultText = "DEL"),
    AC(defaultText = "AC"),
    LEFT(defaultText = "◀"),
    RIGHT(defaultText = "▶"),
    CALC(defaultText = "=");

    companion object {

        fun from(text: String): ItemType? =
                values().find { it.defaultText == text && it.isDeserializable }
    }
}