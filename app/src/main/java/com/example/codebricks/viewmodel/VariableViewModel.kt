package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker

data class Variable(val name: String, var value: Any, val type: String)

class VariableViewModel : ViewModel() {

    val shouldDrawConnections = mutableStateOf(false)

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
        shouldDrawConnections.value = false // выключаем стрелки
        BlockPositionTracker.clear() // очищаем позиции блоков
    }

    // Создание переменной
    fun declareVariable(name: String, value: Any, type: String) {
        val newVariable = Variable(name, value, type)
        _variables.value += newVariable

        val block = Block(
            type = BlockType.VARIABLE_DECLARE,
            value = newVariable
        )
        BlockPositionTracker.redrawTrigger.value++
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
        BlockPositionTracker.redrawTrigger.value++
        addBlock(printBlock)

    }
    fun declareSetVariable(variable: Variable, newValue: Any) {

        val updatedVariable = variable.copy(value = newValue)

        _variables.value = _variables.value.map {
            if (it.name == variable.name) updatedVariable else it
        }

        val setVariableBlock = Block(
            type = BlockType.VARIABLE_SET,
            value = updatedVariable
        )
        BlockPositionTracker.redrawTrigger.value++
        addBlock(setVariableBlock)
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
        BlockPositionTracker.redrawTrigger.value++
        addBlock(controlBlock)
    }

    fun addReferenceBlock(variable: Variable) {
        println("Added insert block: ${variable.name}")
        val block = Block(
            type = BlockType.VARIABLE_REFERENCE,
            value = variable
        )
        BlockPositionTracker.redrawTrigger.value++
        _programBlocks.value += block
    }

    fun executeProgram(onFinish: () -> Unit) {
        val blocksById = programBlocks.associateBy { it.id }
        val declaredVariables = mutableMapOf<String, Variable>()

        var current = programBlocks.find { it.type == BlockType.CONTROL_START }

        while (current != null) {
            when (current.type) {
                BlockType.CONTROL_START -> {
                    logToConsole("🟢 Program started")
                }

                BlockType.VARIABLE_DECLARE -> {
                    val variable = current.value as? Variable
                    if (variable != null) {
                        declaredVariables[variable.name] = variable.copy()
                        logToConsole("✅ Declared ${variable.name} = ${variable.value}")
                    }
                }

                BlockType.VARIABLE_SET -> {
                    val variable = current.value as? Variable

                    if (variable == null) {
                        logToConsole("❌ Error: VARIABLE_SET block has no variable")
                    } else {
                        val target = declaredVariables[variable.name]

                        if (target != null) {
                            target.value = variable.value
                            logToConsole("📝 Set ${target.name} to ${target.value}")
                        } else {
                            logToConsole("❌ Error: variable '${variable.name}' not declared")
                        }
                    }
                }

                BlockType.IO_PRINT -> {
                    val input = current.inputBlocks.firstOrNull()
                    val variable = input?.value as? Variable
                    val target = variable?.name?.let { declaredVariables[it] }

                    if (target != null) {
                        logToConsole("📤 Output: ${target.name} = ${target.value}")
                    } else {
                        logToConsole("❌ Error: variable '${variable?.name}' not declared")
                    }
                }

                BlockType.CONTROL_STOP -> {
                    logToConsole("🔴 Program stopped")
                }

                BlockType.VARIABLE_REFERENCE -> {
                }

                else -> {
                    logToConsole("⚠️ Block '${current.type}' not implemented")
                }
            }

            current = current.nextBlockId?.let { blocksById[it] }
        }

        onFinish()
    }

    fun linkBlocksByPosition() {
        val positionedBlocks = programBlocks.mapNotNull { block ->
            val position = BlockPositionTracker.getPosition(block.id)
            position?.let { block to it }
        }

        val sortedBlocks = positionedBlocks.sortedBy { it.second.y }

        //обнуляем связи
        programBlocks.forEach { it.nextBlockId = null }

        //по порядку
        for (i in 0 until sortedBlocks.size - 1) {
            val currentBlock = sortedBlocks[i].first
            val nextBlock = sortedBlocks[i + 1].first
            currentBlock.nextBlockId = nextBlock.id
        }
        BlockPositionTracker.redrawTrigger.value++
    }

    data class BlockOrderResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun checkBlockOrder(): BlockOrderResult {
        val declared = mutableSetOf<String>()

        val orderedBlocks = programBlocks
            .mapNotNull { block -> BlockPositionTracker.getPosition(block.id)?.let { block to it } }
            .sortedBy { it.second.y }
            .map { it.first }

        if (orderedBlocks.firstOrNull()?.type != BlockType.CONTROL_START) {
            return BlockOrderResult(
                isValid = false,
                errorMessage = "❌ Error: Start block must be the first block."
            )
        }

        for (block in orderedBlocks) {
            when (block.type) {
                BlockType.VARIABLE_DECLARE -> {
                    val variable = block.value as? Variable
                    if (variable != null) {
                        declared.add(variable.name)
                    }
                }

                BlockType.IO_PRINT, BlockType.VARIABLE_SET -> {
                    val usedVar = when (block.type) {
                        BlockType.IO_PRINT -> block.inputBlocks.firstOrNull()?.value as? Variable
                            ?: block.value as? Variable
                        BlockType.VARIABLE_SET -> block.value as? Variable
                        else -> null
                    }

                    if (usedVar != null && usedVar.name !in declared) {
                        return BlockOrderResult(
                            isValid = false,
                            errorMessage = "❌ Error: Variable '${usedVar.name}' is used before it is declared."
                        )
                    }
                }

                else -> continue
            }
        }

        return BlockOrderResult(isValid = true)
    }


}