package com.geckour.flical.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import com.geckour.flical.util.*

class MainViewModel : ViewModel() {

    private var commandList: List<Command> = mutableListOf()
    internal var memory: List<Command> = emptyList()

    private val _formulaCursorPosition = MutableLiveData(0)
    internal val formulaCursorPosition: LiveData<Int> = _formulaCursorPosition

    private val _formulaText = MutableLiveData("")
    internal val formulaText: LiveData<String> = _formulaText

    private val _resultCommands = MutableLiveData<List<Command>>(emptyList())
    internal val resultCommands: LiveData<List<Command>> = _resultCommands

    private val onFormulaTextChanged: (String, Int) -> Unit = { formulaText, cursorPosition ->
        if (cursorPosition > -1 && cursorPosition <= formulaText.length) {
            _formulaText.value = formulaText
            _formulaCursorPosition.value = cursorPosition
        }
    }

    internal fun insertCommands(
        toInsert: List<Command>,
        position: Int = _formulaCursorPosition.value ?: 0
    ) {
        commandList = commandList.inserted(toInsert, position, onFormulaTextChanged)
    }

    private fun removeCommandAt(index: Int) {
        commandList = commandList.removed(index, onFormulaTextChanged)
    }

    internal fun delete() {
        removeCommandAt(_formulaCursorPosition.value ?: return)
    }

    internal fun moveCursorRight() {
        onFormulaTextChanged(
            _formulaText.value ?: return,
            _formulaCursorPosition.value?.plus(1) ?: return
        )
    }

    internal fun moveCursorLeft() {
        onFormulaTextChanged(
            _formulaText.value ?: return,
            _formulaCursorPosition.value?.minus(1) ?: return
        )
    }

    internal fun updateMemory() {
        memory = commandList.subList(0, commandList.size)
        onFormulaTextChanged(_formulaText.value ?: return, _formulaCursorPosition.value ?: return)
    }

    internal fun processCommand(toProcess: Command) {
        if (toProcess.isAffectOnInvoke) {
            commandList = commandList.invoke(toProcess, onFormulaTextChanged)
            onFormulaTextChanged(_formulaText.value ?: "", _formulaCursorPosition.value ?: 0)
        } else {
            commandList = commandList.inserted(
                listOf(toProcess),
                _formulaCursorPosition.value ?: 0,
                onFormulaTextChanged
            )
        }

        if (toProcess.type == ItemType.CALC || commandList.isEmpty())
            _resultCommands.value = emptyList()
        else refreshResult()
    }

    internal fun refreshResult() {
        val commandInputMoreThan2 = commandList.normalize().size > 1
        val result = commandList.invoke(Command(ItemType.CALC))

        _resultCommands.value =
            if (commandInputMoreThan2 && result.lastOrNull()?.type == ItemType.NUMBER)
                result
            else emptyList()
    }

    internal fun onSelectionChangedByUser(start: Int, end: Int) {
        _formulaCursorPosition.value = start
    }
}