package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Variable(val name: String, var value: Any, var type: String)

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
    private fun formatValueForOutput(value: Any, type: String): String {
        return when (type) {
            "double" -> {
                val doubleValue = when (value) {
                    is Number -> value.toDouble()
                    else -> value.toString().toDoubleOrNull() ?: 0.0
                }
                if (doubleValue % 1 == 0.0) "%.1f".format(doubleValue).replace(',', '.')
                else doubleValue.toString().replace(',', '.')
            }
            "string" -> "\"$value\""
            else -> value.toString()
        }
    }

    fun convertVariableType(variableName: String, newType: String) {
        _variables.value = _variables.value.map { variable ->
            if (variable.name == variableName) {
                val newValue = when {
                    variable.type == newType -> variable.value

                    newType == "double" -> when {
                        variable.value is Int -> (variable.value as Int).toDouble()
                        variable.value is Double -> variable.value
                            //variable.value is Boolean -> if (variable.value) 1.0 else 0.0
                        variable.value is String -> (variable.value as String).toDoubleOrNull() ?: 0.0
                        else -> (variable.value as? Number)?.toDouble() ?: 0.0
                    }

                    newType == "int" -> when {
                        variable.value is Double -> (variable.value as Double).toInt()
                        variable.value is Int -> variable.value
                        //variable.value is Boolean -> if (variable.value) 1 else 0
                        variable.value is String -> (variable.value as String).toIntOrNull() ?: 0
                        else -> (variable.value as? Number)?.toInt() ?: 0
                    }

                    newType == "bool" -> when {
                        variable.value is Number -> (variable.value as Number).toInt() != 0
                        variable.value is Boolean -> variable.value
                        variable.value is String -> (variable.value as String).toBooleanStrictOrNull() ?: false
                        else -> false
                    }

                    newType == "string" -> variable.value.toString()

                    else -> variable.value
                }

                variable.copy(type = newType, value = newValue)
            } else {
                variable
            }
        }

        _programBlocks.value = _programBlocks.value.map { block ->
            when (block.type) {
                BlockType.VARIABLE_DECLARE -> {
                    val varValue = block.value as? Variable
                    if (varValue?.name == variableName) {
                        _variables.value.find { it.name == variableName }?.let { newVar ->
                            block.copy(value = newVar)
                        } ?: block
                    } else {
                        block
                    }
                }
                else -> block
            }
        }

        BlockPositionTracker.redrawTrigger.intValue++
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
                val targetVar = block.inputBlocks.getOrNull(0)?.value as? Variable
                val type = targetVar?.type ?: "string"

                val parsed: Any = when (type) {
                    "int" -> rawValue.toIntOrNull() ?: 0
                    "double" -> {
                        val doubleValue = rawValue.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (doubleValue % 1 == 0.0) doubleValue.toInt().toDouble()
                        else doubleValue
                    }
                    "bool" -> rawValue.toBooleanStrictOrNull() ?: false
                    else -> rawValue
                }

                val fakeVariable = Variable(
                    name = rawValue,
                    value = parsed,
                    type = type
                )

                val ref = Block(
                    type = BlockType.VARIABLE_REFERENCE,
                    value = fakeVariable
                )

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
            bounds.inflate(MAGNETIC_PADDING).contains(position)
        } ?: return

        if (blockId == matchedSlot.blockId) {
            println("⚠️ Warning: inserting into self, possible recursive structure — allowing for now")
        }

        val targetBlock = programBlocks.find { it.id == matchedSlot.blockId } ?: return
        val slotIndex = matchedSlot.slotIndex

        if (targetBlock.type !in listOf(
                BlockType.VARIABLE_SET,
                BlockType.MATH_ADD,
                BlockType.MATH_SUBTRACT,
                BlockType.MATH_MULTIPLY,
                BlockType.MATH_DIVIDE
            )
        ) {
            println("🚫 Target block ${targetBlock.type} doesn't support slot insertion")
            return
        }

        // Гарантируем размер inputBlocks
        while (targetBlock.inputBlocks.size <= slotIndex) {
            targetBlock.inputBlocks.add(
                Block(type = BlockType.VARIABLE_REFERENCE)
            )
        }

        // Вставляем перетаскиваемый блок
        targetBlock.inputBlocks[slotIndex] = draggedBlock

        // Лог
        println("✅ Inserted ${draggedBlock.type} (${draggedBlock.id}) into slot $slotIndex of ${targetBlock.type} (${targetBlock.id})")

        _programBlocks.value = _programBlocks.value.toList()
        BlockPositionTracker.redrawTrigger.value++
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


    fun removeBlockFromParent(childId: String) {
        _programBlocks.value = _programBlocks.value.map { block ->
            when (block.type) {
                BlockType.VARIABLE_SET, BlockType.MATH_ADD, BlockType.MATH_SUBTRACT,
                BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE -> {
                    val newInputBlocks = block.inputBlocks.map { inputBlock ->
                        if (inputBlock.id == childId) {
                            Block(type = BlockType.VARIABLE_REFERENCE) // dummy-заглушка
                        } else {
                            inputBlock
                        }
                    }.toMutableList()
                    block.copy(inputBlocks = newInputBlocks)
                }
                else -> block
            }
        }
        BlockPositionTracker.redrawTrigger.intValue++
    }

    fun isRecursiveInsertion(childId: String, targetId: String): Boolean {
        if (childId == targetId) return true

        val target = programBlocks.find { it.id == targetId } ?: return false

        // Рекурсивно ищем childId внутри inputBlocks
        fun containsRecursively(block: Block): Boolean {
            if (block.inputBlocks.any { it.id == childId }) return true
            return block.inputBlocks.any { containsRecursively(it) }
        }

        return containsRecursively(target)
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
                            // Преобразуем значение к нужному типу
                            val newValue = when (memoryVar.type) {
                                "int" -> when (valueBlock.value) {
                                    is Number -> (valueBlock.value as Number).toInt()
                                    else -> (valueBlock.value.toString().toIntOrNull() ?: 0)
                                }
                                "double" -> when (valueBlock.value) {
                                    is Number -> (valueBlock.value as Number).toDouble()
                                    else -> (valueBlock.value.toString().toDoubleOrNull() ?: 0.0)
                                }
                                "bool" -> when (valueBlock.value) {
                                    is Boolean -> valueBlock.value
                                    else -> (valueBlock.value.toString().toBooleanStrictOrNull() ?: false)
                                }
                                else -> valueBlock.value.toString()
                            }

                            memoryVar.value = newValue
                            logToConsole("📝 Set ${memoryVar.name} = ${formatValueForOutput(newValue, memoryVar.type)}")
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

                            val newValue = when (target.type) {
                                "double" -> {
                                    val currentValue = when (target.value) {
                                        is Number -> (target.value as Number).toDouble()
                                        else -> target.value.toString().toDoubleOrNull() ?: 0.0
                                    }
                                    currentValue + delta
                                }
                                else -> {
                                    val currentValue = when (target.value) {
                                        is Number -> (target.value as Number).toInt()
                                        else -> target.value.toString().toIntOrNull() ?: 0
                                    }
                                    currentValue + delta
                                }
                            }

                            target.value = newValue
                            logToConsole("🔄 Changed ${target.name} by $sign$amount to ${formatValueForOutput(newValue, target.type)}")
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