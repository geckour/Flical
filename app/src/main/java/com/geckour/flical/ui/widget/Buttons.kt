package com.geckour.flical.ui.widget

import com.geckour.flical.model.Buttons
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType

internal val buttons =
    Buttons(
        listOf(
            listOf(
                Buttons.Button(
                    Command(ItemType.M),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.MR),
                ),
                Buttons.Button(
                    Command(ItemType.LEFT),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.RIGHT),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.DEL),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.AC),
                ),
            ),
            listOf(
                Buttons.Button(
                    Command(ItemType.NUMBER, "7"),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.NUMBER, "8"),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.NUMBER, "9"),
                    Command(ItemType.LOG10),
                    Command(ItemType.NONE),
                    Command(ItemType.LOG2),
                    Command(ItemType.LN),
                ),
                Buttons.Button(
                    Command(ItemType.DIV),
                    Command(ItemType.MOD),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
            ),
            listOf(
                Buttons.Button(
                    Command(ItemType.NUMBER, "4"),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.NUMBER, "5"),
                    Command(ItemType.LEFT_BRA),
                    Command(ItemType.NONE),
                    Command(ItemType.RIGHT_BRA),
                    Command(ItemType.ABS),
                ),
                Buttons.Button(
                    Command(ItemType.NUMBER, "6"),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.MULTI),
                    Command(ItemType.POW),
                    Command(ItemType.FACTOR),
                    Command(ItemType.NONE),
                    Command(ItemType.SQRT),
                ),
            ),
            listOf(
                Buttons.Button(
                    Command(ItemType.NUMBER, "1"),
                    Command(ItemType.A_COS),
                    Command(ItemType.A_SIN),
                    Command(ItemType.A_TAN),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.NUMBER, "2"),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.NUMBER, "3"),
                    Command(ItemType.COS),
                    Command(ItemType.SIN),
                    Command(ItemType.TAN),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.MINUS),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
            ),
            listOf(
                Buttons.Button(
                    Command(ItemType.NUMBER, "."),
                    Command(ItemType.NONE),
                    Command(ItemType.PI),
                    Command(ItemType.E),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.NUMBER, "0"),
                    Command(ItemType.NONE),
                    Command(ItemType.NUMBER, "00"),
                    Command(ItemType.NUMBER, "1.08"),
                    Command(ItemType.NUMBER, "1.1"),
                ),
                Buttons.Button(
                    Command(ItemType.CALC),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
                Buttons.Button(
                    Command(ItemType.PLUS),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                    Command(ItemType.NONE),
                ),
            ),
        )
    )