package com.geckour.flical.ui.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import com.geckour.flical.util.*

class MainViewModel : ViewModel() {

    private var commandList: List<Command> = mutableListOf()
    internal var memory: List<Command> = emptyList()

    private val _formulaCursorPosition = mutableStateOf(0)
    internal val formulaCursorPosition: State<Int> = _formulaCursorPosition

    private val _formulaText = mutableStateOf("")
    internal val formulaText: State<String> = _formulaText

    private val _resultCommands = mutableStateOf<List<Command>>(emptyList())
    internal val resultCommands: State<List<Command>> = _resultCommands

    private val onFormulaTextChanged: (String, Int) -> Unit = { formulaText, cursorPosition ->
        if (cursorPosition > -1 && cursorPosition <= formulaText.length) {
            _formulaText.value = formulaText
            _formulaCursorPosition.value = cursorPosition
        }
    }

    internal val backgroundImagePath = mutableStateOf<String?>(null)
    internal val flickSensitivity = mutableStateOf(0.4f)
    internal val uiBias = mutableStateOf(0.5f)

    internal fun insertCommands(
        toInsert: List<Command>,
        position: Int = _formulaCursorPosition.value
    ) {
        commandList = commandList.inserted(toInsert, position, onFormulaTextChanged)
    }

    private fun removeCommandAt(index: Int) {
        commandList = commandList.removed(index, onFormulaTextChanged)
    }

    internal fun delete() {
        removeCommandAt(_formulaCursorPosition.value)
    }

    internal fun moveCursorRight() {
        onFormulaTextChanged(
            _formulaText.value,
            _formulaCursorPosition.value + 1
        )
    }

    internal fun moveCursorLeft() {
        onFormulaTextChanged(
            _formulaText.value,
            _formulaCursorPosition.value - 1
        )
    }

    internal fun updateMemory() {
        memory = commandList.subList(0, commandList.size)
        onFormulaTextChanged(_formulaText.value, _formulaCursorPosition.value)
    }

    internal fun processCommand(toProcess: Command) {
        if (toProcess.isAffectOnInvoke) {
            commandList = commandList.invoke(toProcess, onFormulaTextChanged)
            onFormulaTextChanged(_formulaText.value, _formulaCursorPosition.value)
        } else {
            commandList = commandList.inserted(
                listOf(toProcess),
                _formulaCursorPosition.value,
                onFormulaTextChanged
            )
        }

        if (toProcess.type == ItemType.CALC || commandList.isEmpty())
            _resultCommands.value = emptyList()
        else refreshResult()
    }

    internal fun refreshResult() {
        val commandInputMoreThan2 = commandList.normalized().size > 1
        val result = commandList.invoke(Command(ItemType.CALC))

        _resultCommands.value =
            if (commandInputMoreThan2 && result.lastOrNull()?.type == ItemType.NUMBER)
                result
            else emptyList()
    }

    internal fun onCursorPositionChangedByUser(position: Int) {
        _formulaCursorPosition.value = position
    }
}