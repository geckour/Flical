package com.geckour.flical.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.geckour.flical.model.Command
import com.geckour.flical.model.ItemType
import com.geckour.flical.util.*

class MainViewModel : ViewModel() {

    private val commandList: MutableList<Command> = mutableListOf()
    internal var memory: List<Command> = emptyList()

    private val _formulaCursorPosition = MutableLiveData(0)
    internal val formulaCursorPosition: LiveData<Int> = _formulaCursorPosition

    private val _formulaText = MutableLiveData("")
    internal val formulaText: LiveData<String> = _formulaText

    private val _resultCommands = MutableLiveData<List<Command>>(emptyList())
    internal val resultCommands: LiveData<List<Command>> = _resultCommands

    private val onPositionToMoveChanged: (Int) -> Unit = {
        refreshFormula()
        if (it > -1 && it <= _formulaText.value?.length ?: 0) {
            _formulaCursorPosition.value = it
        }
    }

    internal fun insertCommands(
        toInsert: List<Command>,
        position: Int = _formulaCursorPosition.value ?: 0
    ) {
        commandList.insert(toInsert, position, onPositionToMoveChanged)
    }

    private fun removeCommandAt(index: Int) {
        commandList.remove(index, onPositionToMoveChanged)
    }

    internal fun delete() {
        removeCommandAt(_formulaCursorPosition.value ?: return)
    }

    internal fun moveCursorRight() {
        onPositionToMoveChanged(_formulaCursorPosition.value?.plus(1) ?: return)
    }

    internal fun moveCursorLeft() {
        onPositionToMoveChanged(_formulaCursorPosition.value?.minus(1) ?: return)
    }

    internal fun updateMemory() {
        memory = commandList.subList(0, commandList.size)
        onPositionToMoveChanged(_formulaCursorPosition.value ?: return)
    }

    internal fun processCommand(toProcess: Command) {
        if (toProcess.isAffectOnInvoke) {
            commandList.invoke(toProcess, onPositionToMoveChanged)
            onPositionToMoveChanged(_formulaCursorPosition.value ?: 0)
        } else {
            commandList.insert(
                listOf(toProcess),
                _formulaCursorPosition.value ?: 0,
                onPositionToMoveChanged
            )
        }

        if (toProcess.type == ItemType.CALC || commandList.isEmpty())
            _resultCommands.value = emptyList()
        else refreshResult()
    }

    private fun refreshFormula() {
        _formulaText.value = commandList.getDisplayString()
    }

    internal fun refreshResult() {
        val commandInputMoreThan2 = commandList.normalize().size > 1
        val result = commandList.toList().invoke(Command(ItemType.CALC))

        _resultCommands.value =
            if (commandInputMoreThan2 && result.lastOrNull()?.type == ItemType.NUMBER)
                result
            else emptyList()
    }

    internal fun onSelectionChangedByUser(start: Int, end: Int) {
        _formulaCursorPosition.value = start
    }
}