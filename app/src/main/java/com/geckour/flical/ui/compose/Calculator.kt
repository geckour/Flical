package com.geckour.flical.ui.compose

import android.graphics.PointF
import android.graphics.Rect
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.geckour.flical.R
import com.geckour.flical.model.Buttons
import com.geckour.flical.model.Command
import com.geckour.flical.ui.main.fontFamily
import com.geckour.flical.ui.main.montserrat
import com.geckour.flical.ui.widget.CalculatorFormula
import com.geckour.flical.ui.widget.buttons
import java.io.File
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalCoilApi::class)
@Composable
fun Calculator(
    formulaText: String,
    resultText: String,
    backgroundImagePath: String? = null,
    cursorPosition: Int,
    flickSensitivity: Float,
    onOpenSettings: () -> Unit,
    onTextPasted: (text: String?) -> Unit,
    onCursorPositionRequested: (Int) -> Unit,
    onCommand: (Command) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                backgroundImagePath?.let { Color.Transparent }
                    ?: colorResource(id = R.color.backgroundColor)
            )
    ) {
        backgroundImagePath?.let {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberImagePainter(File(it)),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier.padding(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp
            )
        ) { // TODO: Link with settings for single-handed mode
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                Formula(this, formulaText, cursorPosition, onTextPasted, onCursorPositionRequested)
                ResultPreview(resultText)
                Buttons(flickSensitivity, onCommand)
            }
            Image(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopStart)
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false)
                    ) { onOpenSettings() },
                painter = painterResource(id = R.drawable.ic_baseline_settings_20px),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorResource(id = R.color.buttonTintColor))
            )
        }
    }
}

@Composable
fun Formula(
    scope: ColumnScope,
    text: String,
    cursorPosition: Int,
    onTextPasted: (text: String?) -> Unit,
    onCursorPositionRequested: (Int) -> Unit
) {
    with(scope) {
        Box(
            modifier = Modifier
                .background(colorResource(id = R.color.inputBackgroundColor))
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .weight(1f)
        ) {
            FormulaTextField(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
                text = text,
                cursorPosition = cursorPosition,
                onTextPasted = onTextPasted,
                onSelectionChanged = { position, _ -> onCursorPositionRequested(position) }
            )
        }
    }
}

@Composable
fun FormulaTextField(
    modifier: Modifier,
    text: String,
    cursorPosition: Int,
    onTextPasted: (text: String?) -> Unit,
    onSelectionChanged: (start: Int, end: Int) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = {
            CalculatorFormula(it).apply {
                typeface = montserrat

                this.onTextPasted = onTextPasted
                this.onSelectionChanged = onSelectionChanged
            }
        },
        update = {
            it.setText(text)
            it.setSelection(cursorPosition)
        }
    )
}

@Composable
fun ResultPreview(text: String) {
    val maxFontSize = 48.sp
    var fontSize by remember { mutableStateOf(maxFontSize) }
    if (text.isEmpty()) fontSize = maxFontSize
    SelectionContainer(
        modifier = Modifier
            .background(colorResource(id = R.color.inputBackgroundColor))
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            onTextLayout = { result ->
                if (result.didOverflowWidth) fontSize =
                    (fontSize.value - 1).coerceAtLeast(1f).sp
            },
            textAlign = TextAlign.End,
            softWrap = false,
            maxLines = 1,
            fontSize = fontSize,
            fontFamily = fontFamily,
            color = colorResource(id = R.color.primaryTextColor)
        )
    }
}

@Composable
fun Buttons(flickSensitivity: Float, onCommand: (Command) -> Unit) {
    val buttons by remember { mutableStateOf(buttons) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.backgroundMaskColor))
    ) {
        buttons.list.forEach { rows ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rows.forEach { button ->
                    Button(this, button, flickSensitivity, onCommand)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Button(
    scope: RowScope,
    button: Buttons.Button,
    flickSensitivity: Float,
    onCommand: (Command) -> Unit
) {
    var area by remember { mutableStateOf(Buttons.Button.Area.UNDEFINED) }
    val bgBounds by remember { mutableStateOf(Rect()) }
    var height by remember { mutableStateOf(0) }
    var buttonCache by remember { mutableStateOf(button) }

    with(scope) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .height(with(LocalDensity.current) { height.toDp() })
            .pointerInteropFilter { event ->
                val center = PointF(bgBounds.exactCenterX(), bgBounds.exactCenterY())
                val dist = sqrt((event.x - center.x).pow(2) + (event.y - center.y).pow(2))
                val mainR = min(bgBounds.width(), bgBounds.height()) * 0.5 * (1 - flickSensitivity)
                buttonCache = buttonCache.reflectState(event, area, onCommand)
                area = when {
                    event.action == MotionEvent.ACTION_UP ||
                            event.action == MotionEvent.ACTION_POINTER_UP -> {
                        Buttons.Button.Area.UNDEFINED
                    }
                    dist <= mainR ||
                            event.action == MotionEvent.ACTION_DOWN ||
                            event.action == MotionEvent.ACTION_POINTER_DOWN -> {
                        Buttons.Button.Area.MAIN
                    }
                    else -> {
                        val x = event.x - bgBounds.centerX()
                        val y = (bgBounds.width() - event.y) - bgBounds.centerY()

                        if (y > x) {
                            if (y > -x) Buttons.Button.Area.TOP
                            else Buttons.Button.Area.LEFT
                        } else {
                            if (y > -x) Buttons.Button.Area.RIGHT
                            else Buttons.Button.Area.BOTTOM
                        }
                    }
                }

                true
            }
            .onGloballyPositioned {
                height = it.size.width
                bgBounds.set(0, 0, it.size.width, it.size.height)
            }) {
            area.bgResId?.let { bgResId ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = bgResId),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.TopCenter),
                    text = buttonCache.top.text.orEmpty(),
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    color = colorResource(id = R.color.primaryTextInvertColor)
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterStart),
                    text = buttonCache.left.text.orEmpty(),
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    color = colorResource(id = R.color.primaryTextInvertColor)
                )
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = buttonCache.main.text.orEmpty(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = colorResource(id = R.color.primaryTextInvertColor)
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    text = buttonCache.right.text.orEmpty(),
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    color = colorResource(id = R.color.primaryTextInvertColor)
                )
                Text(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    text = buttonCache.bottom.text.orEmpty(),
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                    color = colorResource(id = R.color.primaryTextInvertColor)
                )
            }
        }
    }
}

fun Buttons.Button.reflectState(
    event: MotionEvent,
    area: Buttons.Button.Area,
    onCommand: (Command) -> Unit
): Buttons.Button {
    when (event.action) {
        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
            val command = when (area) {
                Buttons.Button.Area.MAIN -> this.main
                Buttons.Button.Area.LEFT -> this.left
                Buttons.Button.Area.TOP -> this.top
                Buttons.Button.Area.RIGHT -> this.right
                Buttons.Button.Area.BOTTOM -> this.bottom
                Buttons.Button.Area.UNDEFINED -> null
            } ?: return this

            onCommand(command)
        }
    }

    return this
}