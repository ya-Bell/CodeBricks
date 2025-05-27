package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            type = BlockType.VARIABLE_DECLARE, value = newVariable
        )
        BlockPositionTracker.redrawTrigger.intValue++
        addBlock(block)
    }

    fun isVariableAlreadyDeclared(name: String): Boolean {
        return variables.any { it.name == name }
    }

    fun declareEmptyChangeVariableBlock() {
        val firstVar = variables.firstOrNull()
        val referenceBlock = firstVar?.let {
            Block(type = BlockType.VARIABLE_REFERENCE, value = it)
        }

        val changeBlock = Block(
            type = BlockType.VARIABLE_CHANGE,
            inputBlocks = referenceBlock?.let { mutableListOf(it) } ?: mutableListOf()).apply {
            changeSign = "+"
            changeAmount = 0
        }

        BlockPositionTracker.redrawTrigger.intValue++
        addBlock(changeBlock)
    }

    fun updateChangeBlockVariable(blockId: String, variable: Variable) {
        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
                val refBlock = Block(type = BlockType.VARIABLE_REFERENCE, value = variable)
                if (block.inputBlocks.isEmpty()) {
                    block.inputBlocks.add(refBlock)
                } else {
                    block.inputBlocks[0] = refBlock
                }
            }
            block
        }
        BlockPositionTracker.redrawTrigger.intValue++
    }

    fun updateChangeBlockSign(blockId: String, sign: String) {
        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
                block.changeSign = sign
            }
            block
        }
        BlockPositionTracker.redrawTrigger.intValue++
    }

    fun updateChangeBlockAmount(blockId: String, amount: Int) {
        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
                block.changeAmount = amount
            }
            block
        }
        BlockPositionTracker.redrawTrigger.intValue++
    }

    // Создание блока Print(variable)
    fun declarePrintBlock(variable: Variable) {
        val referenceBlock = Block(
            type = BlockType.VARIABLE_REFERENCE, value = variable
        )
        val printBlock = Block(
            type = BlockType.IO_PRINT, inputBlocks = mutableListOf(referenceBlock)
        )
        BlockPositionTracker.redrawTrigger.intValue++
        addBlock(printBlock)

    }

    fun declareEmptySetVariableBlock() {
        val firstVar = variables.firstOrNull()
        val referenceBlock = firstVar?.let {
            Block(type = BlockType.VARIABLE_REFERENCE, value = it)
        }

        val setBlock = Block(
            type = BlockType.VARIABLE_SET,
            inputBlocks = referenceBlock?.let { mutableListOf(it) } ?: mutableListOf())

        BlockPositionTracker.redrawTrigger.intValue++
        addBlock(setBlock)
    }

    fun updateSetBlockTarget(blockId: String, variable: Variable) {
        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_SET) {
                val refBlock = Block(
                    type = BlockType.VARIABLE_REFERENCE, value = variable
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
        BlockPositionTracker.redrawTrigger.intValue++
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

        BlockPositionTracker.redrawTrigger.intValue++
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
        BlockPositionTracker.redrawTrigger.intValue++
        addBlock(controlBlock)
    }

    fun removeBlockById(blockId: String) {
        _programBlocks.value = _programBlocks.value.filterNot { it.id == blockId }
        BlockPositionTracker.redrawTrigger.intValue++
    }


    fun addReferenceBlock(variable: Variable) {
        println("Added insert block: ${variable.name}")
        val block = Block(
            type = BlockType.VARIABLE_REFERENCE, value = variable
        )
        BlockPositionTracker.redrawTrigger.intValue++
        _programBlocks.value += block
    }

    val highlightedSlot = mutableStateOf<Pair<String, Int>?>(null)

    fun setHighlightedSlot(blockId: String?, slotIndex: Int?) {
        highlightedSlot.value =
            if (blockId != null && slotIndex != null) blockId to slotIndex else null
    }

    fun tryInsertIntoSlot(position: Offset, blockId: String) {
        val draggedBlock = programBlocks.find { it.id == blockId } ?: return

        val matchedSlot = BlockSlotTracker.getAllSlots().find { (_, _, bounds) ->
            bounds.contains(position)
        } ?: return
        if (blockId == matchedSlot.blockId) {
            println("🚫 Can't insert block into itself")
            return
        }
        println("➡️ Inserting $blockId into ${matchedSlot.blockId} at index ${matchedSlot.slotIndex}")

        val targetBlock = programBlocks.find { it.id == matchedSlot.blockId } ?: return
        val slotIndex = matchedSlot.slotIndex

        // Только в определённых блоках поддерживается вставка
        if (targetBlock.type !in listOf(
                BlockType.VARIABLE_SET,
                BlockType.MATH_ADD,
                BlockType.MATH_SUBTRACT,
                BlockType.MATH_MULTIPLY,
                BlockType.MATH_DIVIDE
            )
        ) return

        // Удостоверимся, что inputBlocks имеет нужный размер
        while (targetBlock.inputBlocks.size <= slotIndex) {
            targetBlock.inputBlocks.add(Block(type = BlockType.VARIABLE_REFERENCE)) // dummy
        }

        // Вставляем draggedBlock внутрь
        targetBlock.inputBlocks[slotIndex] = draggedBlock

        // Обновим список, чтобы Jetpack отреагировал
        _programBlocks.value = _programBlocks.value.toList()
        BlockPositionTracker.redrawTrigger.intValue++
    }


    fun declareMathBlock(type: BlockType) {
        if (type !in listOf(
                BlockType.MATH_ADD,
                BlockType.MATH_SUBTRACT,
                BlockType.MATH_MULTIPLY,
                BlockType.MATH_DIVIDE
            )
        ) return

        val block = Block(
            type = type, inputBlocks = mutableListOf(null, null).map {
                Block(type = BlockType.VARIABLE_REFERENCE)
            }.toMutableList()
        )

        addBlock(block)
        BlockPositionTracker.redrawTrigger.intValue++
    }

    private val recentlyInsertedSlot = mutableStateOf<Pair<String, Int>?>(null)

    fun setRecentlyInsertedSlot(blockId: String, slotIndex: Int) {
        recentlyInsertedSlot.value = blockId to slotIndex
        viewModelScope.launch {
            delay(300)
            recentlyInsertedSlot.value = null
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
        BlockPositionTracker.redrawTrigger.intValue++
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

                BlockType.VARIABLE_CHANGE -> {
                    val refVar = current.inputBlocks.getOrNull(0)?.value as? Variable
                    if (refVar != null) {
                        val target = declaredVariables[refVar.name]
                        if (target != null) {
                            val sign = current.changeSign
                            val amount = current.changeAmount
                            val delta = if (sign == "-") -amount else amount
                            val oldValue = target.value as? Int ?: 0
                            target.value = oldValue + delta
                            logToConsole("🔄 Changed ${target.name} by $sign$amount to ${target.value}")
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
        BlockPositionTracker.redrawTrigger.intValue++
    }

    fun findBlockContaining(childId: String): Block? {
        return programBlocks.find { block ->
            block.inputBlocks.any { it.id == childId }
        }
    }

    data class BlockOrderResult(
        val isValid: Boolean, val errorMessage: String? = null
    )

    fun checkBlockOrder(): BlockOrderResult {
        val declared = mutableSetOf<String>()

        val orderedBlocks = programBlocks.mapNotNull { block ->
                BlockPositionTracker.getPosition(block.id)?.let { block to it }
            }.sortedBy { it.second.y }.map { it.first }

        if (orderedBlocks.firstOrNull()?.type != BlockType.CONTROL_START) {
            return BlockOrderResult(
                isValid = false, errorMessage = "❌ Error: Start block must be the first block."
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