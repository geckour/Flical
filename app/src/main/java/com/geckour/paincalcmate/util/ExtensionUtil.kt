package com.geckour.paincalcmate.util

import com.geckour.paincalcmate.model.Command
import com.geckour.paincalcmate.model.ItemType

fun List<Command>.append(command: Command): List<Command> =
        ArrayList(this).apply { add(command) }

fun List<Command>.getDisplayString(): String =
        this.mapNotNull {
            return@mapNotNull it.text?.let { text ->
                if (it.type == ItemType.NUMBER) text else " $text "
            }
        }.joinToString("").trim()

fun Command.parse(commandList: List<Command>): List<Command> =
        when (this.type) {
            ItemType.LEFT,
            ItemType.RIGHT,
            ItemType.DEL,
            ItemType.AC,
            ItemType.COPY,
            ItemType.PASTE,
            ItemType.MR,
            ItemType.MC,
            ItemType.M_PLUS,
            ItemType.M_MINUS,
            ItemType.CALC,
            ItemType.ANS -> {
                commandList.invoke(this)
            }

            else -> {
                commandList.append(this)
            }
        }

fun List<Command>.invoke(command: Command): List<Command> =
        when (command.type) {
            ItemType.LEFT -> this
            ItemType.RIGHT -> this
            ItemType.COPY -> this
            ItemType.PASTE -> emptyList()
            ItemType.MR -> emptyList()
            ItemType.MC -> this
            ItemType.M_PLUS -> this
            ItemType.M_MINUS -> this
            ItemType.CALC -> emptyList()
            ItemType.ANS -> emptyList()
            ItemType.DEL -> this.dropLast(1)
            ItemType.AC -> emptyList()
            else -> this
        }