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
        this.normalize()
                .mapNotNull { it.text?.let { " $it" } }
                .joinToString("")
                .trim()

val Command.isSpecial: Boolean
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
                this.normalize()
                        .toRpn()
                        .calculate()?.let {
                            listOf(
                                    Command(ItemType.NONE, "="),
                                    it
                            )
                        } ?: listOf(Command(ItemType.NONE, "ERROR!"))
            }
            else -> this
        }

fun List<Command>.normalize(): List<Command> {
    fun List<Command>.isNumberConsecutive(): Boolean =
            this.size > 1
                    && (this.last().type == ItemType.NUMBER
                    && this[this.lastIndex - 1].type == ItemType.NUMBER)

    fun List<Command>.isNumberNegative(): Boolean =
            this.size > 1
                    && this.last().type == ItemType.NUMBER
                    && this[this.lastIndex - 1].type == ItemType.MINUS
                    && (this.size < 3 || this[this.lastIndex - 2].type != ItemType.NUMBER)

    fun List<Command>.isNeedConcat(): Boolean =
            this.isNumberConsecutive() || this.isNumberNegative()

    fun ArrayList<Command>.concatNumber() {
        if (this.isNeedConcat()) {
            val last = this.removeAt(this.lastIndex).let { l -> Command(ItemType.NUMBER, this.last().text + l.text) }
            this[this.lastIndex] = last
        }
    }

    val resultList: ArrayList<Command> = ArrayList()

    this.forEach {
        resultList.add(it)
        resultList.concatNumber()
    }

    return resultList
}

fun List<Command>.purify(): List<Command> =
        this.filter { it.type != ItemType.NONE }

fun List<Command>.insert(commands: List<Command>, position: Int = -1,
                         onInserted: (position: Int) -> Unit = {}): List<Command> =
        ArrayList(this).apply {
            val index = this@insert.getIndexFromPosition(position) + 1
            addAll(index, commands)
            val positionToMove =
                    if (this@insert.lastIndex < index) -1
                    else {
                        this.getDisplayString().length -
                                this.subList(index + commands.size, this.size)
                                        .getDisplayString().length
                    }
            onInserted(positionToMove)
        }

fun List<Command>.remove(position: Int = -1, onRemoved: (position: Int) -> Unit = {}): List<Command> =
        if (position <= 0) this
        else {
            ArrayList(this).apply {
                val index = this@remove.getIndexFromPosition(position)
                removeAt(index)
                val positionToMove = this.getDisplayString().length -
                        this.subList(index, this.size).getDisplayString().length
                onRemoved(positionToMove)
            }
        }

private fun List<Command>.getIndexFromPosition(position: Int): Int {
    if (position < 0) return this.lastIndex

    (1..this.lastIndex).forEach {
        val length = this.subList(0, it).getDisplayString().apply { Timber.d("subListString: $this") }.length +
                this.subList(it - 1, it + 1).getDisplayString().count { it == ' ' }
        Timber.d("length: $length, position: $position")
        if (length >= position) {
            return it - 1
        }
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
                                    ExBigDecimal(BigDecimalType.NORMAL,
                                            BigDecimal(it.text
                                                    ?: throw IllegalStateException()))
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
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.multiply(a, mathContext))
                            })
                        }

                        ItemType.DIV -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            val preResult = b.toDouble() / a.toDouble()
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.divide(a, mathContext))
                            })
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
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.pow(b, a, mathContext))
                            })
                        }

                        ItemType.MOD -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            val preResult = b.toDouble() % a.toDouble()
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.remainder(a, mathContext))
                            })
                        }

                        ItemType.SQRT -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = sqrt(a.toDouble())
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.sqrt(a, mathContext))
                            })
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
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log(a, mathContext))
                            })
                        }

                        ItemType.LOG10 -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = log10(a.toDouble())
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log10(a, mathContext))
                            })
                        }

                        ItemType.LOG2 -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = log2(a.toDouble())
                            stack.add(when {
                                preResult == Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                preResult == Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                preResult.isNaN() -> ExBigDecimal(BigDecimalType.NAN)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log2(a, mathContext))
                            })
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