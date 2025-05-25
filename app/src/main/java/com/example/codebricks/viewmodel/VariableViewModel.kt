package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import androidx.compose.runtime.State

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
    fun isVariableAlreadyDeclared(name: String): Boolean {
        return variables.any { it.name == name }
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

    private val _highlightedSlot = mutableStateOf<Pair<String, Int>?>(null)
    val highlightedSlot: State<Pair<String, Int>?> = _highlightedSlot

    fun declareEmptySetVariableBlock() {
        val firstVar = variables.firstOrNull()
        val referenceBlock = firstVar?.let {
            Block(type = BlockType.VARIABLE_REFERENCE, value = it)
        }

        val setBlock = Block(
            type = BlockType.VARIABLE_SET,
            inputBlocks = referenceBlock?.let { mutableListOf(it) } ?: mutableListOf()
        )

        BlockPositionTracker.redrawTrigger.value++
        addBlock(setBlock)
    }

    fun updateSetBlockTarget(blockId: String, variable: Variable) {
        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_SET) {
                val refBlock = Block(
                    type = BlockType.VARIABLE_REFERENCE,
                    value = variable
                )
                block.inputBlocks.apply {
                    if (isEmpty()) {
                        add(refBlock)
                    } else {
                        this[0] = refBlock
                    }
                }
            }
            block
        }
        BlockPositionTracker.redrawTrigger.value++
    }

    fun updateSetBlockValue(blockId: String, rawValue: String) {
        if (rawValue.isBlank()) return

        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_SET) {
                val type = (block.inputBlocks.getOrNull(0)?.value as? Variable)?.type ?: "string"

                val parsed: Any = when (type) {
                    "int" -> rawValue.toIntOrNull() ?: 0
                    "double" -> rawValue.replace(",", ".").toDoubleOrNull() ?: 0.0
                    "bool" -> rawValue.toBooleanStrictOrNull() ?: false
                    else -> rawValue
                }

                val fakeVariable = Variable(name = rawValue, value = parsed, type = type)
                val ref = Block(type = BlockType.VARIABLE_REFERENCE, value = fakeVariable)

                if (block.inputBlocks.size < 2) {
                    block.inputBlocks.add(ref)
                } else {
                    block.inputBlocks[1] = ref
                }
            }
            block
        }

        BlockPositionTracker.redrawTrigger.value++
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

    fun removeBlockById(blockId: String) {
        _programBlocks.value = _programBlocks.value.filterNot { it.id == blockId }
        BlockPositionTracker.redrawTrigger.value++
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

    fun tryInsertReferenceBlock(position: Offset, referenceBlockId: String) {
        val refBlock = programBlocks.find { it.id == referenceBlockId } ?: return
        println(">> Drop at: $position")

        val matchedSlot = BlockSlotTracker.getAllSlots().find { (_, _, bounds) ->
            bounds.contains(position)
        }

        if (matchedSlot != null) {
            val targetBlock = programBlocks.find { it.id == matchedSlot.blockId }
            if (targetBlock != null && targetBlock.type == BlockType.VARIABLE_SET) {
                val slotIndex = matchedSlot.slotIndex

                // Проверка типа переменной
                val targetVar = targetBlock.inputBlocks.getOrNull(0)?.value as? Variable
                val refVar = refBlock.value as? Variable

                if (slotIndex == 1 && targetVar != null && refVar != null) {
                    if (targetVar.type != refVar.type) {
                        logToConsole("❌ Type mismatch: ${refVar.type} cannot be assigned to ${targetVar.type}")
                        return
                    }
                }

                while (targetBlock.inputBlocks.size <= slotIndex) {
                    targetBlock.inputBlocks.add(Block(type = BlockType.VARIABLE_REFERENCE))
                }

                targetBlock.inputBlocks[slotIndex] = refBlock

                _programBlocks.value = _programBlocks.value.toList()
                BlockPositionTracker.redrawTrigger.value++
            }
        }
    }


    fun removeReferenceFromParent(referenceId: String) {
        _programBlocks.value.forEach { block ->
            val index = block.inputBlocks.indexOfFirst { it.id == referenceId }
            if (index != -1) {
                block.inputBlocks.removeAt(index)
            }
        }
        _programBlocks.value = _programBlocks.value.toList()
        BlockPositionTracker.redrawTrigger.value++
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
                    val targetVar = current.inputBlocks.getOrNull(0)?.value as? Variable
                    val valueBlock = current.inputBlocks.getOrNull(1)?.value as? Variable

                    if (targetVar == null) {
                        logToConsole("❌ Error: No variable selected in Set block.")
                    } else if (valueBlock == null) {
                        logToConsole("❌ Error: No value provided for ${targetVar.name}")
                    } else {
                        val memoryVar = declaredVariables[targetVar.name]

                        if (memoryVar != null) {
                            val refName = valueBlock.name
                            val referenced = declaredVariables[refName]

                            if (referenced != null) {
                                memoryVar.value = referenced.value
                                logToConsole("📝 Set ${memoryVar.name} to ${referenced.value} (from ${refName})")
                            } else {
                                memoryVar.value = valueBlock.value
                                logToConsole("📝 Set ${memoryVar.name} to ${valueBlock.value}")
                            }
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
    fun findBlockContaining(childId: String): Block? {
        return programBlocks.find { block ->
            block.inputBlocks.any { it.id == childId }
        }
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
                BlockType.VARIABLE_SET -> {
                    val variable = block.inputBlocks.getOrNull(0)?.value as? Variable
                    val value = block.inputBlocks.getOrNull(1)?.value as? Variable

                    if (variable == null) {
                        return BlockOrderResult(
                            isValid = false,
                            errorMessage = "❌ Error: Set block is missing target variable."
                        )
                    }

                    if (value == null) {
                        return BlockOrderResult(
                            isValid = false,
                            errorMessage = "❌ Error: Set block for '${variable.name}' is missing a value."
                        )
                    }

                    if (variable.name !in declared) {
                        return BlockOrderResult(
                            isValid = false,
                            errorMessage = "❌ Error: Variable '${variable.name}' is used before it is declared."
                        )
                    }
                }

                BlockType.IO_PRINT -> {
                    val variable = block.inputBlocks.firstOrNull()?.value as? Variable
                    if (variable != null && variable.name !in declared) {
                        return BlockOrderResult(
                            isValid = false,
                            errorMessage = "❌ Error: Variable '${variable.name}' is used before it is declared."
                        )
                    }
                }

                else -> continue
            }
        }

        return BlockOrderResult(isValid = true)
    }


}