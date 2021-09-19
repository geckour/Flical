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

const val PRECISION: Int = 20

fun List<Command>.getDisplayString(): String =
        normalized()
                .mapNotNull { it.text?.let { " ${it.clean()}" } }
                .joinToString("")
                .trim()

private fun String.clean(): String =
        replace(Regex("^(.*\\.\\d+?)0+$"), "$1")

fun String.deserialize(): List<Command> =
        split(" ")
                .filter { it.isNotBlank() }
                .map { it.trim().replace(",", "") }
                .mapNotNull {
                    when {
                        it.matches(Regex("^\\d+?\\.?\\d*?$")) -> {
                            Command(ItemType.NUMBER, it)
                        }
                        else -> {
                            ItemType.from(it)?.let { itemType -> Command(itemType, it) }
                        }
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

fun List<Command>.invoke(
        command: Command,
        onInvoked: (formulaText: String, position: Int) -> Unit = { _, _ -> }
): List<Command> =
        when (command.type) {
            ItemType.AC -> emptyList()
            ItemType.CALC -> {
                normalized()
                        .toRpn()
                        .calculate()
                        ?.let { listOf(Command(ItemType.NONE, "="), it).numbersMutilated() }
                        ?: listOf(Command(ItemType.NONE, "ERROR!"))
            }
            else -> this
        }.apply {
            val displayString = getDisplayString()
            onInvoked(displayString, displayString.length)
        }

fun List<Command>.normalized(): List<Command> = // Combine numbers
        fold(mutableListOf()) { mutableList, command ->
            val isLastNumber = mutableList.lastOrNull().let {
                if (it?.type == ItemType.NUMBER) return@let true
                // Process negative number
                if (it?.type == ItemType.MINUS) {
                    // Avoid reckoning multiple operation for example: 5 - 3 -> 5 -3 -> 5 * -3
                    if (mutableList.getOrNull(mutableList.lastIndex - 1)?.type != ItemType.NUMBER) {
                        return@let true
                    }
                }
                return@let false
            }
            if (command.type == ItemType.NUMBER && isLastNumber) {
                mutableList[mutableList.lastIndex] =
                        Command(ItemType.NUMBER, mutableList.last().text + command.text)
            } else mutableList.add(command)

            return@fold mutableList
        }

private fun List<Command>.purified(): List<Command> = filterNot { it.type == ItemType.NONE }

fun List<Command>.inserted(
        commands: List<Command>,
        position: Int = 0,
        onInserted: (formulaText: String, position: Int) -> Unit = { _, _ -> }
): List<Command> =
        if (commands.none { it.isNotShown }) {
            val textLengthBeforePurify = getDisplayString().length

            val purified = purified().numbersMutilated()
            val normalizedPosition =
                    (position + purified.getDisplayString().length - textLengthBeforePurify).coerceAtLeast(0)
            val index = purified.getIndexFromPosition(normalizedPosition) + 1
            val textLength = purified.getDisplayString().length

            val result = purified.toMutableList().apply { addAll(index, commands.numbersMutilated()) }.toList()

            val positionToMove = normalizedPosition + result.getDisplayString().length - textLength
            onInserted(result.getDisplayString(), positionToMove)

            result
        } else this

private fun List<Command>.numbersMutilated(): List<Command> =
        flatMap { command ->
            if (command.type == ItemType.NUMBER && command.text?.let { it.length > 1 } == true)
                command.text.map { Command(ItemType.NUMBER, it.toString()) }
            else listOf(command)
        }

fun List<Command>.removed(
        position: Int = 0,
        onRemoved: (formulaText: String, position: Int) -> Unit = { _, _ -> }
): List<Command> {
    if (position < 1) return this

    val index = getIndexFromPosition(position)
    val textLength = getDisplayString().length
    val result = filterIndexed { i, _ -> i != index }
    val positionToMove = position + result.getDisplayString().length - textLength
    onRemoved(result.getDisplayString(), positionToMove)

    return result
}

private fun List<Command>.getIndexFromPosition(position: Int): Int {
    if (position < 1) return -1

    repeat(this.size) { i ->
        val length = this.subList(0, i + 1).getDisplayString().length
        val spacesCount =
                if (i < this.lastIndex)
                    this.subList(i, i + 2).getDisplayString().count { it.isWhitespace() }
                else 0

        if (position <= length + spacesCount) return i
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
                        stack.reversed()
                                .withIndex()
                                .firstOrNull { it.value.type == ItemType.LEFT_BRA }
                                ?.index

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
                this.forEach { command ->
                    when (command.type) {
                        ItemType.NUMBER -> {
                            stack.push(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimal(command.text
                                                    ?: throw IllegalStateException())
                                    )
                            )
                        }

                        ItemType.PI -> {
                            stack.push(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.pi(mathContext)
                                    )
                            )
                        }

                        ItemType.E -> {
                            stack.push(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.e(mathContext)
                                    )
                            )
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
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                b.multiply(a, mathContext)
                                        )
                                    }
                            )
                        }

                        ItemType.DIV -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            val preResult = b.toDouble() / a.toDouble()
                            stack.add(
                                    when {
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                b.divide(a, mathContext)
                                        )
                                    }
                            )
                        }

                        ItemType.FACTOR -> {
                            val a = stack.pop().value?.let {
                                if (BigDecimalMath.isIntValue(it)) it.toInt()
                                else throw IllegalArgumentException("Must factor with Int value")
                            } ?: throw IllegalStateException()
                            stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.factorial(a)))
                        }

                        ItemType.POW -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            val preResult = b.toDouble().pow(a.toDouble())
                            stack.add(
                                    when {
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                BigDecimalMath.pow(b, a, mathContext)
                                        )
                                    }
                            )
                        }

                        ItemType.MOD -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            val preResult = b.toDouble() % a.toDouble()
                            stack.add(
                                    when {
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                b.remainder(a, mathContext)
                                        )
                                    }
                            )
                        }

                        ItemType.SQRT -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = sqrt(a.toDouble())
                            stack.add(
                                    when {
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                BigDecimalMath.sqrt(a, mathContext)
                                        )
                                    }
                            )
                        }

                        ItemType.COS -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            stack.add(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.cos(a, mathContext)
                                    )
                            )
                        }

                        ItemType.SIN -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            stack.add(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.sin(a, mathContext)
                                    )
                            )
                        }

                        ItemType.TAN -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            stack.add(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.tan(a, mathContext)
                                    )
                            )
                        }

                        ItemType.A_COS -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            stack.add(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.acos(a, mathContext)
                                    )
                            )
                        }

                        ItemType.A_SIN -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            stack.add(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.asin(a, mathContext)
                                    )
                            )
                        }

                        ItemType.A_TAN -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            stack.add(
                                    ExBigDecimal(
                                            BigDecimalType.NORMAL,
                                            BigDecimalMath.atan(a, mathContext)
                                    )
                            )
                        }

                        ItemType.LN -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = ln(a.toDouble())
                            stack.add(
                                    when {
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                BigDecimalMath.log(a, mathContext)
                                        )
                                    }
                            )
                        }

                        ItemType.LOG10 -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = log10(a.toDouble())
                            stack.add(
                                    when {
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                BigDecimalMath.log10(a, mathContext)
                                        )
                                    }
                            )
                        }

                        ItemType.LOG2 -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = log2(a.toDouble())
                            stack.add(
                                    when {
                                        preResult == Double.POSITIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                        }
                                        preResult == Double.NEGATIVE_INFINITY -> {
                                            ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                        }
                                        preResult.isNaN() -> {
                                            ExBigDecimal(BigDecimalType.NAN)
                                        }
                                        else -> ExBigDecimal(
                                                BigDecimalType.NORMAL,
                                                BigDecimalMath.log2(a, mathContext)
                                        )
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
                            .flatMap {
                                listOf(
                                        Command(ItemType.NUMBER, it.value?.toPlainString()),
                                        Command((ItemType.MULTI))
                                )
                            }
                            .dropLast(1)
                            .toRpn()
                            .calculate()
                } else {
                    val exBigDecimal = stack.pop()

                    when (exBigDecimal.type) {
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
                            val text = exBigDecimal.value
                                    ?.setScale(PRECISION, RoundingMode.HALF_UP)
                                    ?.toPlainString()
                                    ?: throw IllegalStateException()

                            Command(ItemType.NUMBER, text.trim().clean())
                        }
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t)
                null
            }
        }