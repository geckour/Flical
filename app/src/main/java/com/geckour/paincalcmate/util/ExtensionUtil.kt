package com.geckour.paincalcmate.util

import com.geckour.paincalcmate.model.Command
import com.geckour.paincalcmate.model.ItemType
import timber.log.Timber
import java.util.*
import kotlin.math.*

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
            ItemType.ANS -> commandList.invoke(this)

            else -> commandList.append(this)
        }

fun List<Command>.invoke(command: Command): List<Command> =
        when (command.type) {
            ItemType.LEFT -> emptyList()
            ItemType.RIGHT -> emptyList()
            ItemType.COPY -> emptyList()
            ItemType.PASTE -> emptyList()
            ItemType.MR -> emptyList()
            ItemType.MC -> emptyList()
            ItemType.M_PLUS -> emptyList()
            ItemType.M_MINUS -> emptyList()
            ItemType.CALC -> {
                this.normalize()
                        .toRpn()
                        .calculate()?.let {
                            listOf(
                                    Command(ItemType.NONE, "="),
                                    Command(ItemType.NUMBER, it.toString())
                            )
                        } ?: listOf(Command(ItemType.NONE, "ERROR!"))
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

            else -> {
                while (stack.isNotEmpty()) {
                    val latestStackWeight = stack.last().type.weight ?: break
                    val wight = it.type.weight ?: break

                    if (latestStackWeight > wight) break
                    else returnList.add(stack.pop())
                }

                stack.push(it)
            }
        }
    }

    returnList.addAll(stack.reversed())

    return returnList.apply {
        Timber.d("original: ${this@toRpn.map { it.text }}")
        Timber.d("rpn: ${this.map { it.text }}")
    }
}

fun List<Command>.calculate(): Double? =
        if (this.isEmpty()) null
        else {
            val stack: Stack<Double> = Stack()

            try {
                this.forEach {
                    when (it.type) {
                        ItemType.NUMBER -> {
                            stack.push(it.text?.toDouble()
                                    ?: throw kotlin.IllegalStateException())
                        }

                        ItemType.PI -> {
                            stack.push(PI)
                        }

                        ItemType.E -> {
                            stack.push(E)
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

                        ItemType.POW -> {
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.add(b.pow(a))
                        }

                        ItemType.MOD -> {
                            val a = stack.pop()
                            val b = stack.pop()
                            stack.add(b % a)
                        }

                        ItemType.SQRT -> {
                            val a = stack.pop()
                            stack.add(sqrt(a))
                        }

                        ItemType.COS -> {
                            val a = stack.pop()
                            stack.add(cos(a))
                        }

                        ItemType.SIN -> {
                            val a = stack.pop()
                            stack.add(sin(a))
                        }

                        ItemType.TAN -> {
                            val a = stack.pop()
                            stack.add(tan(a))
                        }

                        ItemType.A_COS -> {
                            val a = stack.pop()
                            stack.add(acos(a))
                        }

                        ItemType.A_SIN -> {
                            val a = stack.pop()
                            stack.add(asin(a))
                        }

                        ItemType.A_TAN -> {
                            val a = stack.pop()
                            stack.add(atan(a))
                        }

                        ItemType.LN -> {
                            val a = stack.pop()
                            stack.add(ln(a))
                        }

                        ItemType.LOG10 -> {
                            val a = stack.pop()
                            stack.add(log10(a))
                        }

                        ItemType.LOG2 -> {
                            val a = stack.pop()
                            stack.add(log2(a))
                        }

                        ItemType.ABS -> {
                            val a = stack.pop()
                            stack.add(abs(a))
                        }

                        else -> {
                        }
                    }
                }

                val accuracy = 10.0.pow(14)
                (stack.pop() * accuracy).roundToLong().toDouble() / accuracy
            } catch (t: Throwable) {
                Timber.e(t)
                null
            }
        }