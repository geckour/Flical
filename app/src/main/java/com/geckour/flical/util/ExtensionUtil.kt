package com.geckour.flical.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import ch.obermuhlner.math.big.BigDecimalMath
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import timber.log.Timber
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
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

fun List<Command>.append(command: Command): List<Command> =
        ArrayList(this).apply { add(command) }

fun List<Command>.getDisplayString(): String =
        this.normalize()
                .mapNotNull { it.text?.let { " $it" } }
                .joinToString("")
                .trim()

fun Command.parse(commandList: List<Command>): List<Command> =
        when (this.type) {
            ItemType.LEFT,
            ItemType.RIGHT,
            ItemType.DEL,
            ItemType.AC,
            ItemType.COPY,
            ItemType.PASTE,
            ItemType.M,
            ItemType.MR,
            ItemType.CALC -> commandList.invoke(this)

            else -> commandList.append(this).purify()
        }

fun List<Command>.invoke(command: Command): List<Command> =
        when (command.type) {
            ItemType.LEFT -> emptyList()
            ItemType.RIGHT -> emptyList()
            ItemType.COPY -> emptyList()
            ItemType.PASTE -> emptyList()
            ItemType.M -> this
            ItemType.MR -> this
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
            ItemType.DEL -> this.dropLast(1)
            ItemType.AC -> emptyList()
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
            val mathContext = MathContext(14, RoundingMode.CEILING)

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
                            stack.push(ExBigDecimal(BigDecimalType.NORMAL, BigDecimal(PI)))
                        }

                        ItemType.E -> {
                            stack.push(ExBigDecimal(BigDecimalType.NORMAL, BigDecimal(E)))
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
                            stack.add(when (preResult) {
                                Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.multiply(a, mathContext))
                            })
                        }

                        ItemType.DIV -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            val preResult = b.toDouble() / a.toDouble()
                            stack.add(when (preResult) {
                                Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.divide(a, mathContext))
                            })
                        }

                        ItemType.POW -> {
                            val a = stack.pop().value?.let {
                                if (it.toDouble() % 1.0 == 0.0) it.toInt()
                                else throw IllegalStateException("Must power with Int value")
                            } ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            val preResult = b.toDouble().pow(a)
                            stack.add(when (preResult) {
                                Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, b.pow(a, mathContext))
                            })
                        }

                        ItemType.MOD -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val b = stack.pop().value ?: throw IllegalStateException()
                            stack.add(ExBigDecimal(BigDecimalType.NORMAL, b.remainder(a, mathContext)))
                        }

                        ItemType.SQRT -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            stack.add(ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.sqrt(a, mathContext)))
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
                            stack.add(when (preResult) {
                                Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log(a, mathContext))
                            })
                        }

                        ItemType.LOG10 -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = log10(a.toDouble())
                            stack.add(when (preResult) {
                                Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
                                else -> ExBigDecimal(BigDecimalType.NORMAL, BigDecimalMath.log10(a, mathContext))
                            })
                        }

                        ItemType.LOG2 -> {
                            val a = stack.pop().value ?: throw IllegalStateException()
                            val preResult = log2(a.toDouble())
                            stack.add(when (preResult) {
                                Double.POSITIVE_INFINITY -> ExBigDecimal(BigDecimalType.POSITIVE_INFINITY)
                                Double.NEGATIVE_INFINITY -> ExBigDecimal(BigDecimalType.NEGATIVE_INFINITY)
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
                stack.pop().let {
                    when (it.type) {
                        BigDecimalType.POSITIVE_INFINITY -> Command(ItemType.NONE, "Infinity")
                        BigDecimalType.NEGATIVE_INFINITY -> Command(ItemType.NONE, "-Infinity")
                        else -> Command(ItemType.NUMBER, it.value?.toPlainString()
                                ?: throw IllegalStateException())
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t)
                null
            }
        }

fun Uri.extractMediaBitmap(context: Context): Bitmap? =
        try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        } catch (t: Throwable) {
            Timber.e(t)
            null
        }

fun String.toUri(): Uri? =
        try {
            Uri.parse(this)
        } catch (t: Throwable) {
            Timber.e(t)
            null
        }

fun String.parseMimeType(): Bitmap.CompressFormat? =
        when (MimeTypeMap.getSingleton().getExtensionFromMimeType(this)) {
            "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            "webp" -> Bitmap.CompressFormat.WEBP
            else -> null
        }