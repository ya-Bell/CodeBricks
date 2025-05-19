package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType

data class Variable(val name: String, val value: Any, val type: String)

class VariableViewModel : ViewModel() {

    // Переменные как "память"
    private val _variables = mutableStateOf<List<Variable>>(emptyList())
    val variables: List<Variable> get() = _variables.value

    // Программа = список блоков
    private val _programBlocks = mutableStateOf<List<Block>>(emptyList())
    val programBlocks: List<Block> get() = _programBlocks.value

    // Вывод консоли
    val consoleOutput = mutableStateOf("Console ready.")

    // Очистка консоли
    fun clearConsole() {
        consoleOutput.value = "Console cleared."
    }

    // Добавить сообщение в консоль
    private fun logToConsole(message: String) {
        consoleOutput.value += "\n$message"
    }

    // Добавить блок в программу
    private fun addBlock(block: Block) {
        _programBlocks.value += block
    }

    // Очистка рабочей зоны
    fun clearWorkspace() {
        _variables.value = emptyList()
        _programBlocks.value = emptyList()
    }

    // Создание переменной
    fun declareVariable(name: String, value: Any, type: String) {
        val newVariable = Variable(name, value, type)
        _variables.value += newVariable

        val block = Block(
            type = BlockType.VARIABLE_DECLARE,
            value = newVariable
        )
        addBlock(block)
    }

    // Создание блока Print(variable)
    fun declarePrintBlock(variable: Variable) {
        val referenceBlock = Block(
            type = BlockType.VARIABLE_REFERENCE,
            value = variable
        )
        val printBlock = Block(
            type = BlockType.IO_PRINT,
            inputBlocks = mutableListOf(referenceBlock)
        )
        addBlock(printBlock)
    }

    // Start / Stop
    fun declareControlBlock(type: String) {
        val blockType = when (type) {
            "Start" -> BlockType.CONTROL_START
            "Stop" -> BlockType.CONTROL_STOP
            else -> BlockType.CONTROL_START
        }

        val controlBlock = Block(
            type = blockType
        )
        addBlock(controlBlock)
    }

    fun executeProgram(onFinish: () -> Unit = {}) {
        consoleOutput.value = "Compiling..."
        logToConsole("Running program...")

        for (block in programBlocks) {
            when (block.type) {
                BlockType.CONTROL_START -> logToConsole("🟢 Program started")
                BlockType.CONTROL_STOP -> logToConsole("🔴 Program stopped")
                BlockType.IO_PRINT -> {
                    val input = block.inputBlocks.firstOrNull()
                    val variable = input?.value as? Variable
                    logToConsole("Output: ${variable?.value}")
                }
                else -> logToConsole("Block '${block.type}' not yet implemented")
            }
        }

        logToConsole("Done.")
        onFinish()
    }
}