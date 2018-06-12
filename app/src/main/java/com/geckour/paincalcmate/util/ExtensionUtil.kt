package com.geckour.paincalcmate.util

import com.geckour.paincalcmate.model.Command
import com.geckour.paincalcmate.model.ItemType
import java.util.*

fun List<Command>.append(command: Command): List<Command> =
        ArrayList(this).apply { add(command) }

fun List<Command>.getDisplayString(): String =
        this.mapNotNull {
            if (it.type == ItemType.NUMBER)
                it.text
            else
                it.text?.let { " $it " }
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
            ItemType.CALC -> {
                this.normalize()
                        .toRpn()
                        .calculate().let {
                            listOf(Command(ItemType.NUMBER, it?.toString()))
                        }
            }
            ItemType.ANS -> emptyList()
            ItemType.DEL -> this.dropLast(1)
            ItemType.AC -> emptyList()
            else -> this
        }

fun List<Command>.normalize(): List<Command> {
    val returnList: ArrayList<Command> = ArrayList()

    this.forEach {
        if (returnList.isEmpty() ||
                (it.type == ItemType.NUMBER
                        && returnList.last().type == ItemType.NUMBER).not()) {
            returnList.add(it)
        } else {
            val new = returnList.last().let { n -> n.copy(text = n.text + it.text) }
            returnList[returnList.lastIndex] = new
        }
    }

    return returnList
}

fun List<Command>.toRpn(): List<Command> {
    val returnList: ArrayList<Command> = ArrayList()
    val stack: Stack<Command> = Stack()

    this.forEach {
        when (it.type) {
            ItemType.PLUS, ItemType.MINUS -> {
                while (stack.isNotEmpty()) {
                    val stackFirstType = stack.first().type

                    if (stackFirstType == ItemType.MULTI || stackFirstType == ItemType.DIV) {
                        returnList.add(stack.pop())
                    } else break
                }

                stack.push(it)
            }

            ItemType.MULTI, ItemType.DIV, ItemType.LEFT_BRA -> {
                stack.push(it)
            }

            ItemType.RIGHT_BRA -> {
                val leftBraIndex =
                        stack.reversed().withIndex()
                                .firstOrNull {
                                    it.value.type == ItemType.LEFT_BRA
                                }?.index

                if (leftBraIndex != null) {
                    (0 until leftBraIndex).forEach {
                        returnList.add(stack.pop())
                    }
                    stack.pop()
                }
            }

            ItemType.NUMBER -> {
                returnList.add(it)
            }
        }
    }

    returnList.addAll(stack.reversed())

    return returnList
}

fun List<Command>.calculate(): Double? =
        if (this.isEmpty()) null
        else {
            val stack: Stack<Double> = Stack()

            this.forEach {
                when (it.type) {
                    ItemType.NUMBER -> {
                        stack.push(it.text?.toDouble() ?: 0.0)
                    }

                    ItemType.PLUS -> {
                        val a = stack.pop()
                        val b = stack.pop()
                        stack.add(b + a)
                    }

                    ItemType.MINUS -> {
                        val a = stack.pop()
                        val b = stack.pop()
                        stack.add(b - a)
                    }

                    ItemType.MULTI -> {
                        val a = stack.pop()
                        val b = stack.pop()
                        stack.add(b * a)
                    }

                    ItemType.DIV -> {
                        val a = stack.pop()
                        val b = stack.pop()
                        stack.add(b / a)
                    }

                    else -> {
                    }
                }
            }

            stack.pop()
        }