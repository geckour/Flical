package com.geckour.flical.util

import ch.obermuhlner.math.big.BigDecimalMath
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import timber.log.Timber
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import kotlin.math.*

enum class BigDecimalType {
    NAN,
    POSITIVE_INFINITY,
    NEGATIVE_INFINITY,
    NORMAL
}

data class ExBigDecimal(
    val type: BigDecimalType,
    val value: BigDecimal? = null
)

var precision: Int = 0

fun List<Command>.getDisplayString(): String =
    normalize()
        .mapNotNull { it.text?.let { " $it" } }
        .joinToString("")
        .trim()
        .clean()

private fun String.clean(): String =
    replace(Regex("^(.*\\.\\d+?)0+$"), "$1")

fun String.deserialize(): List<Command> =
    split(" ")
        .filter { it.isNotBlank() }
        .map { it.trim() }
        .mapNotNull {
            when {
                it.matches(Regex("^\\d+?\\.?\\d*?$")) -> Command(ItemType.NUMBER, it)
                it == ItemType.ZERO2.defaultText -> Command(ItemType.ZERO2)
                it == ItemType.PI.defaultText -> Command(ItemType.PI)
                it == ItemType.E.defaultText -> Command(ItemType.E)
                it == ItemType.PLUS.defaultText -> Command(ItemType.PLUS)
                it == ItemType.MINUS.defaultText -> Command(ItemType.MINUS)
                it == ItemType.MULTI.defaultText -> Command(ItemType.MULTI)
                it == ItemType.DIV.defaultText -> Command(ItemType.DIV)
                it == ItemType.POW.defaultText -> Command(ItemType.POW)
                it == ItemType.FACTOR.defaultText -> Command(ItemType.FACTOR)
                it == ItemType.MOD.defaultText -> Command(ItemType.MOD)
                it == ItemType.SQRT.defaultText -> Command(ItemType.SQRT)
                it == ItemType.SIN.defaultText -> Command(ItemType.SIN)
                it == ItemType.COS.defaultText -> Command(ItemType.COS)
                it == ItemType.TAN.defaultText -> Command(ItemType.TAN)
                it == ItemType.A_SIN.defaultText -> Command(ItemType.A_SIN)
                it == ItemType.A_COS.defaultText -> Command(ItemType.A_COS)
                it == ItemType.A_TAN.defaultText -> Command(ItemType.A_TAN)
                it == ItemType.LN.defaultText -> Command(ItemType.LN)
                it == ItemType.LOG10.defaultText -> Command(ItemType.LOG10)
                it == ItemType.LOG2.defaultText -> Command(ItemType.LOG2)
                it == ItemType.ABS.defaultText -> Command(ItemType.ABS)
                else -> null
            }
        }

val Command.isAffectOnInvoke: Boolean
    get() = this.type == ItemType.AC || this.type == ItemType.CALC

val Command.isNotShown: Boolean
    get() = when (this.type) {
        ItemType.LEFT,
        ItemType.RIGHT,
        ItemType.DEL,
        ItemType.AC,
        ItemType.M,
        ItemType.MR,
        ItemType.CALC -> true

        else -> false
    }

fun List<Command>.invoke(command: Command): List<Command> =
    when (command.type) {
        ItemType.AC -> emptyList()
        ItemType.CALC -> {
            normalize()
                .toRpn()
                .calculate()?.let {
                    listOf(
                        Command(ItemType.NONE, "="),
                        it
                    ).mutilateNumbers()
                } ?: listOf(Command(ItemType.NONE, "ERROR!"))
        }
        else -> this
    }

fun MutableList<Command>.invoke(command: Command, onInvoked: (position: Int) -> Unit = {}) {
    val result = this.toList().invoke(command)
    this.clear()
    this.addAll(result)
    onInvoked(getDisplayString().length)
}

fun List<Command>.normalize(): List<Command> = // Combine numbers
    fold(mutableListOf()) { mutableList, command ->
        val last = mutableList.lastOrNull()
        if (command.type == ItemType.NUMBER
            && (last?.type == ItemType.NUMBER ||
                    (last?.type == ItemType.MINUS && // Process negative number
                            mutableList.getOrNull(mutableList.lastIndex - 1)?.type // Avoid reckoning multiple operation for example: 5 - 3 -> 5 -3 -> 5 * -3
                            != ItemType.NUMBER))
        ) {
            mutableList[mutableList.lastIndex] = Command(ItemType.NUMBER, last.text + command.text)
        } else mutableList.add(command)

        return@fold mutableList
    }

private fun MutableList<Command>.purify() {
    removeAll { it.type == ItemType.NONE }
}

fun MutableList<Command>.insert(
    commands: List<Command>,
    position: Int = 0,
    onInserted: (position: Int) -> Unit = {}
) {
    if (commands.none { it.isNotShown }) {
        val textLengthBeforePurify = getDisplayString().length
        purify()
        mutilateNumbers()
        val normalizedPosition = (position + getDisplayString().length - textLengthBeforePurify).let {
            if (it < 0) 0 else it
        }
        val index = getIndexFromPosition(normalizedPosition) + 1
        val textLength = getDisplayString().length
        addAll(index, commands.mutilateNumbers())
        val positionToMove = normalizedPosition + getDisplayString().length - textLength
        onInserted(positionToMove)
    }
}

private fun MutableList<Command>.mutilateNumbers() {
    val result = this.toList().mutilateNumbers()
    this.clear()
    this.addAll(result)
}

private fun List<Command>.mutilateNumbers(): List<Command> =
    flatMap {
        if (it.type == ItemType.NUMBER && it.text != null && it.text.length > 1)
            it.text.map { Command(ItemType.NUMBER, it.toString()) }
        else listOf(it)
    }

fun MutableList<Command>.remove(position: Int = 0, onRemoved: (position: Int) -> Unit = {}) {
    if (position > 0) {
        val index = getIndexFromPosition(position)
        val textLength = getDisplayString().length
        removeAt(index)
        val positionToMove = position + getDisplayString().length - textLength
        onRemoved(positionToMove)
    }
}

private fun List<Command>.getIndexFromPosition(position: Int): Int {
    if (position <= 0) return -1

    repeat(this.size) {
        val length = this.subList(0, it + 1).getDisplayString().length
        val spacesAfterItCount =
            if (this.lastIndex > it)
                this.subList(it, it + 2).getDisplayString().count { it == ' ' }
            else 0
        if (length + spacesAfterItCount >= position)
            return it
    }

    return this.lastIndex
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
                    repeat(leftBraIndex) {
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

    return returnList
}

fun List<Command>.calculate(): Command? =
    if (this.isEmpty()) null
    else {
        val mathContext = MathContext(100)

        val stack: Stack<ExBigDecimal> = Stack()

        try {
            this.forEach {
                when (it.type) {
                    ItemType.NUMBER -> {
                        stack.push(
                            ExBigDecimal(
                                BigDecimalType.NORMAL,
                                BigDecimal(
                                    it.text
                                        ?: throw IllegalStateException()
                                )
                            )
                        )
                    }

                    ItemType.PI -> {
                        stack.push(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.pi(mathContext)))
                    }

                    ItemType.E -> {
                        stack.push(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.e(mathContext)))
                    }

                    ItemType.PLUS -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val b = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, b.add(a, mathContext)))
                    }

                    ItemType.MINUS -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val b = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, b.subtract(a, mathContext)))
                    }

                    ItemType.MULTI -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val b = stack.pop().value ?: throw IllegalStateException()
                        val preResult = b.toDouble() * a.toDouble()
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.multiply(a, mathContext))
                            }
                        )
                    }

                    ItemType.DIV -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val b = stack.pop().value ?: throw IllegalStateException()
                        val preResult = b.toDouble() / a.toDouble()
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.divide(a, mathContext))
                            }
                        )
                    }

                    ItemType.FACTOR -> {
                        val a = stack.pop().value?.let {
                            if (BigDecimalMath.isIntValue(it)) it.toInt()
                            else throw IllegalStateException("Must factor with Int value")
                        } ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.factorial(a)))
                    }

                    ItemType.POW -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val b = stack.pop().value ?: throw IllegalStateException()
                        val preResult = b.toDouble().pow(a.toDouble())
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.pow(b, a, mathContext))
                            }
                        )
                    }

                    ItemType.MOD -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val b = stack.pop().value ?: throw IllegalStateException()
                        val preResult = b.toDouble() % a.toDouble()
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.remainder(a, mathContext))
                            }
                        )
                    }

                    ItemType.SQRT -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val preResult = sqrt(a.toDouble())
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.sqrt(a, mathContext))
                            }
                        )
                    }

                    ItemType.COS -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.cos(a, mathContext)))
                    }

                    ItemType.SIN -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.sin(a, mathContext)))
                    }

                    ItemType.TAN -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.tan(a, mathContext)))
                    }

                    ItemType.A_COS -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.acos(a, mathContext)))
                    }

                    ItemType.A_SIN -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.asin(a, mathContext)))
                    }

                    ItemType.A_TAN -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.atan(a, mathContext)))
                    }

                    ItemType.LN -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val preResult = ln(a.toDouble())
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log(a, mathContext))
                            }
                        )
                    }

                    ItemType.LOG10 -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val preResult = log10(a.toDouble())
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log10(a, mathContext))
                            }
                        )
                    }

                    ItemType.LOG2 -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        val preResult = log2(a.toDouble())
                        stack.add(
                            when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log2(a, mathContext))
                            }
                        )
                    }

                    ItemType.ABS -> {
                        val a = stack.pop().value ?: throw IllegalStateException()
                        stack.add(ExBigDecimal(BigDecimalType.NORMAL, a.abs(mathContext)))
                    }

                    else -> {
                    }
                }
            }
            if (stack.size > 1) {
                stack.toList()
                    .asReversed()
                    .map {
                        Command(ItemType.NUMBER, it.value?.toPlainString())
                    }.let {
                        val returnList: ArrayList<Command> = ArrayList()

                        it.forEach {
                            returnList.add(it)
                            returnList.add(Command(ItemType.MULTI))
                        }
                        returnList.removeAt(returnList.lastIndex)

                        return@let returnList
                    }.toRpn()
                    .calculate()
            } else {
                stack.pop().let {
                    return@let when (it.type) {
                        BigDecimalType.POSITIVE_INFINITY -> {
                            Command(ItemType.POSITIVE_INFINITY, "Infinity")
                        }
                        BigDecimalType.NEGATIVE_INFINITY -> {
                            Command(ItemType.NEGATIVE_INFINITY, "-Infinity")
                        }
                        BigDecimalType.NAN -> {
                            Command(ItemType.NAN, "NaN")
                        }
                        else -> {
                            val text = it.value
                                ?.setScale(precision, RoundingMode.HALF_UP)
                                ?.let {
                                    if (BigDecimalMath.isIntValue(it))
                                        it.toInt().toString()
                                    else it.toPlainString()
                                } ?: throw IllegalStateException()

                            Command(ItemType.NUMBER, text)
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Timber.e(t)
            null
        }
    }