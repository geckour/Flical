package com.geckour.flical.model

enum class ItemType(val weight: Int? = null) {
    NONE,
    POSITIVE_INFINITY,
    NEGATIVE_INFINITY,
    NAN,
    NUMBER(0),
    ZERO2,
    PI(0),
    E(0),
    LEFT_BRA,
    RIGHT_BRA,
    PLUS(4),
    MINUS(4),
    MULTI(3),
    DIV(3),
    POW(2),
    MOD(2),
    SQRT(1),
    SIN(1),
    COS(1),
    TAN(1),
    A_SIN(1),
    A_COS(1),
    A_TAN(1),
    LN(1),
    LOG10(1),
    LOG2(1),
    ABS(1),
    COPY,
    PASTE,
    M,
    MR,
    DEL,
    AC,
    LEFT,
    RIGHT,
    CALC
}