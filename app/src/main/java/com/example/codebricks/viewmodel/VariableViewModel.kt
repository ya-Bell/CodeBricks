package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
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
    fun addBlock(block: Block) {
        _programBlocks.value += block
        redrawTrigger.intValue++
    }

    fun clearWorkspace() {
        _variables.value = emptyList()
        _programBlocks.value = emptyList()
        shouldDrawConnections.value = false
        BlockPositionTracker.clear()
        BlockSlotTracker.clear()
        highlightedSlot.value = null
        recentlyInsertedSlot.value = null
    }

    // Создание переменной
    fun declareVariable(name: String, value: Any, type: String) {
        val newVariable = Variable(name, value, type)
        _variables.value += newVariable

        val block = Block(
            type = BlockType.VARIABLE_DECLARE, value = newVariable
        )
        redrawTrigger.intValue++
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

        redrawTrigger.intValue++
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
                        variable.value is String -> (variable.value as String).toDoubleOrNull()
                            ?: 0.0

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
                        variable.value is String -> (variable.value as String).toBooleanStrictOrNull()
                            ?: false

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

        redrawTrigger.intValue++
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
        redrawTrigger.intValue++
    }

    fun updateChangeBlockSign(blockId: String, sign: String) {
        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
                block.changeSign = sign
            }
            block
        }
        redrawTrigger.intValue++
    }

    fun updateChangeBlockAmount(blockId: String, amount: Int) {
        _programBlocks.value = _programBlocks.value.map { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
                block.changeAmount = amount
            }
            block
        }
        redrawTrigger.intValue++
    }

    // Создание блока Print(variable)
    fun declarePrintBlock(variable: Variable) {
        val referenceBlock = Block(
            type = BlockType.VARIABLE_REFERENCE, value = variable
        )

        val printBlock = Block(
            type = BlockType.IO_PRINT, inputBlocks = mutableListOf(referenceBlock)
        )

        addBlock(printBlock)
        redrawTrigger.intValue++
    }

    fun declareEmptySetVariableBlock() {
        val firstVar = variables.firstOrNull()

        val setBlock = Block(type = BlockType.VARIABLE_SET, inputBlocks = firstVar?.let {
            mutableListOf(
                Block(
                    type = BlockType.VARIABLE_REFERENCE, value = it
                )
            )
        } ?: mutableListOf())

        addBlock(setBlock)
        redrawTrigger.intValue++
    }

    fun cleanupUnattachedReferenceBlocks() {
        val attachedIds = mutableSetOf<String>()
        fun collect(block: Block) {
            if (attachedIds.add(block.id)) {
                block.inputBlocks.filterNotNull().forEach(::collect)
                block.children.forEach(::collect)
            }
        }

        programBlocks.filter { findBlockContaining(it.id) == null }.forEach(::collect)

        val before = _programBlocks.value.size
        _programBlocks.value = _programBlocks.value.filter { it.id in attachedIds }
        val after = _programBlocks.value.size

        println("Cleaned: removed ${before - after} detached blocks")
    }

    fun updateSetBlockTarget(blockId: String, variable: Variable) {
        val updatedBlocks = mutableListOf<Block>()
        val detachedIds = mutableSetOf<String>()

        _programBlocks.value.forEach { block ->
            if (block.id == blockId && block.type == BlockType.VARIABLE_SET) {
                val oldRef = block.inputBlocks.getOrNull(0)
                oldRef?.let { detachedIds.add(it.id) }

                val newRef = Block(type = BlockType.VARIABLE_REFERENCE, value = variable)
                block.inputBlocks.apply {
                    if (isEmpty()) add(newRef) else this[0] = newRef
                }
                updatedBlocks.add(block)
            } else {
                updatedBlocks.add(block)
            }
            cleanupUnattachedReferenceBlocks()
        }

        _programBlocks.value = updatedBlocks.filterNot { it.id in detachedIds }
        redrawTrigger.intValue++
    }

    fun updateSetBlockValue(blockId: String, rawValue: String) {
        if (rawValue.isBlank()) return

        val updatedBlocks = mutableListOf<Block>()
        val detachedIds = mutableSetOf<String>()

        _programBlocks.value.forEach { block ->
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
                    name = rawValue, value = parsed, type = type
                )

                val newRef = Block(
                    type = BlockType.VARIABLE_REFERENCE, value = fakeVariable
                )

                block.inputBlocks.getOrNull(1)?.let { detachedIds.add(it.id) }

                if (block.inputBlocks.size < 2) {
                    block.inputBlocks.add(newRef)
                } else {
                    block.inputBlocks[1] = newRef
                }

                updatedBlocks.add(block)
            } else {
                updatedBlocks.add(block)
            }
        }

        _programBlocks.value = updatedBlocks.filterNot { it.id in detachedIds }
        redrawTrigger.intValue++
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
        redrawTrigger.intValue++
        addBlock(controlBlock)
    }

    fun removeBlockById(blockId: String) {
        _programBlocks.value = _programBlocks.value.filterNot { it.id == blockId }
        redrawTrigger.intValue++
    }


    fun addReferenceBlock(variable: Variable) {
        println("Added insert block: ${variable.name}")
        val block = Block(
            type = BlockType.VARIABLE_REFERENCE, value = variable
        )
        redrawTrigger.intValue++
        _programBlocks.value += block
    }

    val highlightedSlot = mutableStateOf<Pair<String, Int>?>(null)

    fun setHighlightedSlot(blockId: String?, slotIndex: Int?) {
        highlightedSlot.value =
            if (blockId != null && slotIndex != null) blockId to slotIndex else null
    }

    fun tryInsertIntoSlot(position: Offset, blockId: String) {
        val draggedBlock = programBlocks.find { it.id == blockId } ?: return

        val matchedSlot = BlockSlotTracker.getAllSlots()
            .filter { it.bounds.inflate(MAGNETIC_PADDING).contains(position) }
            .minByOrNull { it.bounds.width * it.bounds.height } ?: return

        if (blockId == matchedSlot.blockId) {
            println("⚠️ Warning: inserting into self")
            return
        }

        // найти любой блок (на любой глубине) по matchedSlot.blockId
        val targetBlock = findBlockRecursivelyById(matchedSlot.blockId, programBlocks) ?: return

        val slotIndex = matchedSlot.slotIndex

        if (targetBlock.type !in listOf(
                BlockType.VARIABLE_SET,
                BlockType.MATH_ADD,
                BlockType.MATH_SUBTRACT,
                BlockType.MATH_MULTIPLY,
                BlockType.MATH_DIVIDE
            )
        ) {
            println("🚫 Target block doesn't support insertion")
            return
        }

        if (isRecursiveInsertion(blockId, targetBlock.id)) {
            println("🚫 Recursive insertion detected")
            return
        }

        while (targetBlock.inputBlocks.size <= slotIndex) {
            targetBlock.inputBlocks.add(null)
        }

        targetBlock.inputBlocks[slotIndex] = draggedBlock
        _programBlocks.value = _programBlocks.value.filterNot { it.id == draggedBlock.id }
        println("✅ Inserted into $slotIndex of ${targetBlock.type} (${targetBlock.id})")

        _programBlocks.value = _programBlocks.value.toList()
        redrawTrigger.intValue++
    }

    fun collectDescendantIds(block: Block): Set<String> {
        val result = mutableSetOf<String>()
        fun collect(current: Block) {
            if (result.add(current.id)) {
                current.inputBlocks.filterNotNull().forEach { collect(it) }
                current.children.forEach { collect(it) }
            }
        }
        collect(block)
        return result
    }

    fun findBlockById(id: String): Block? {
        fun search(block: Block): Block? {
            if (block.id == id) return block
            block.inputBlocks.filterNotNull().forEach {
                val result = search(it)
                if (result != null) return result
            }
            block.children.forEach {
                val result = search(it)
                if (result != null) return result
            }
            return null
        }

        return programBlocks.firstNotNullOfOrNull { search(it) }
    }


    private fun findBlockRecursivelyById(targetId: String, blocks: List<Block>): Block? {
        for (block in blocks) {
            if (block.id == targetId) return block

            for (child in block.inputBlocks) {
                if (child == null) continue
                val found = findBlockRecursivelyById(targetId, listOf(child))
                if (found != null) return found
            }

            for (child in block.children) {
                val found = findBlockRecursivelyById(targetId, listOf(child))
                if (found != null) return found
            }
        }
        return null
    }

    fun bringBlockToFront(id: String) {
        val block = findBlockById(id) ?: return
        _programBlocks.value = _programBlocks.value.filterNot { it.id == id } + block
        redrawTrigger.intValue++
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
            type = type, inputBlocks = mutableListOf(null, null)
        )

        addBlock(block)
        redrawTrigger.intValue++
    }

    private val recentlyInsertedSlot = mutableStateOf<Pair<String, Int>?>(null)

    fun setRecentlyInsertedSlot(blockId: String, slotIndex: Int) {
        recentlyInsertedSlot.value = blockId to slotIndex
        viewModelScope.launch {
            delay(300)
            recentlyInsertedSlot.value = null
        }
    }

    fun removeBlockRecursively(blockId: String) {
        val toRemove = collectDescendantIds(findBlockById(blockId) ?: return)
        _programBlocks.value = _programBlocks.value.filterNot { it.id in toRemove }
        redrawTrigger.intValue++
    }


    fun removeBlockFromParent(childId: String) {
        fun removeFrom(block: Block): Block {
            val newInputs = block.inputBlocks.map { input ->
                when {
                    input?.id == childId -> null
                    input != null -> removeFrom(input)
                    else -> null
                }
            }.toMutableList()

            val newChildren = block.children.map { removeFrom(it) }.toMutableList()

            return block.copy(inputBlocks = newInputs, children = newChildren)
        }

        _programBlocks.value = _programBlocks.value.map { removeFrom(it) }
        redrawTrigger.intValue++
    }

    fun isRecursiveInsertion(childId: String, targetId: String): Boolean {
        if (childId == targetId) return true
        val visited = mutableSetOf<String>()

        val rootCandidates = programBlocks

        fun containsRecursively(block: Block): Boolean {
            if (block.id == targetId) return true
            return block.inputBlocks.any { it != null && containsRecursively(it) } || block.children.any {
                containsRecursively(
                    it
                )
            }
        }

        fun findBlockRecursivelyById(targetId: String, blocks: List<Block>): Block? {
            fun recurse(block: Block): Block? {
                if (!visited.add(block.id)) return null

                if (block.id == targetId) return block

                for (child in block.inputBlocks) {
                    if (child != null) {
                        val result = recurse(child)
                        if (result != null) return result
                    }
                }

                for (child in block.children) {
                    val result = recurse(child)
                    if (result != null) return result
                }

                return null
            }

            for (block in blocks) {
                val result = recurse(block)
                if (result != null) return result
            }

            return null
        }

        val draggedBlock = findBlockRecursivelyById(childId, rootCandidates) ?: return false
        return containsRecursively(draggedBlock)
    }

    fun replaceSlotBlock(parentId: String, slotIndex: Int, newBlock: Block) {
        val parent = findBlockById(parentId) ?: return

        // Удаляем старый блок из слота
        parent.inputBlocks.getOrNull(slotIndex)?.let {
            removeBlockRecursively(it.id)
        }

        while (parent.inputBlocks.size <= slotIndex) {
            parent.inputBlocks.add(null)
        }

        parent.inputBlocks[slotIndex] = newBlock

        removeBlockById(newBlock.id)

        redrawTrigger.intValue++
    }

    fun executeProgram(onFinish: () -> Unit) {
        val blocksById = programBlocks.associateBy { it.id }
        val declaredVariables = mutableMapOf<String, Variable>()
        var current = programBlocks.find { it.type == BlockType.CONTROL_START }

        while (current != null) {
            when (current.type) {
                BlockType.CONTROL_START -> logToConsole("🟢 Program started")
                BlockType.CONTROL_STOP -> logToConsole("🔴 Program stopped")

                BlockType.VARIABLE_DECLARE -> {
                    (current.value as? Variable)?.let { variable ->
                        declaredVariables[variable.name] = variable.copy()
                        logToConsole("✅ Declared ${variable.name} = ${variable.value}")
                    }
                }

                BlockType.VARIABLE_SET -> {
                    val targetVar = current.inputBlocks.getOrNull(0)?.value as? Variable
                    val valueBlock = current.inputBlocks.getOrNull(1)

                    if (targetVar == null) {
                        logToConsole("❌ Error: No variable selected in Set block.")
                    } else {
                        declaredVariables[targetVar.name]?.let { memoryVar ->
                            val rawValue = MathExpressionEvaluator.evaluate(valueBlock)

                            val newValue = when (memoryVar.type) {
                                "int" -> rawValue.toInt()
                                "double" -> rawValue
                                "bool" -> rawValue != 0.0
                                else -> rawValue.toString()
                            }

                            memoryVar.value = newValue
                            logToConsole(
                                "📝 Set ${memoryVar.name} = ${
                                    formatValueForOutput(newValue, memoryVar.type)
                                }"
                            )
                        }
                    }
                }


                BlockType.VARIABLE_CHANGE -> {
                    (current.inputBlocks.getOrNull(0)?.value as? Variable)?.let { refVar ->
                        declaredVariables[refVar.name]?.let { target ->
                            val sign = current!!.changeSign
                            val amount = current!!.changeAmount
                            val delta = if (sign == "-") -amount else amount

                            val newValue = when (target.type) {
                                "double" -> (target.value.toString().toDoubleOrNull()
                                    ?: 0.0) + delta

                                else -> (target.value.toString().toIntOrNull() ?: 0) + delta
                            }

                            target.value = newValue
                            logToConsole(
                                "🔄 Changed ${target.name} by $sign$amount to ${
                                    formatValueForOutput(
                                        newValue, target.type
                                    )
                                }"
                            )
                        }
                    }
                }

                BlockType.IO_PRINT -> {
                    (current.inputBlocks.firstOrNull()?.value as? Variable)?.let { variable ->
                        declaredVariables[variable.name]?.let { target ->
                            logToConsole("📤 Output: ${target.name} = ${target.value}")
                        } ?: logToConsole("❌ Error: variable '${variable.name}' not declared")
                    }
                }

                BlockType.MATH_ADD, BlockType.MATH_SUBTRACT, BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE -> {
                    val result = MathExpressionEvaluator.evaluate(current)
                    logToConsole("🧮 Result of math expression: $result")
                }


                BlockType.VARIABLE_REFERENCE -> {
                }

                BlockType.COMPARISON_EQUAL -> TODO()
                BlockType.COMPARISON_GREATER -> TODO()
                BlockType.COMPARISON_LESS -> TODO()
                BlockType.LOGIC_AND -> TODO()
                BlockType.LOGIC_OR -> TODO()
                BlockType.LOGIC_NOT -> TODO()
                BlockType.LOOP_REPEAT -> TODO()
                BlockType.LOOP_WHILE -> TODO()
                BlockType.LOOP_FOR -> TODO()
                BlockType.FUNCTION_DEFINE -> TODO()
                BlockType.FUNCTION_CALL -> TODO()
                BlockType.IF -> TODO()
                BlockType.ELSE -> TODO()
                BlockType.WHILE -> TODO()
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
        redrawTrigger.intValue++
    }

    fun findBlockContaining(childId: String): Block? {
        return programBlocks.find { block ->
            block.inputBlocks.any { it?.id == childId }
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
                    val valueBlock = block.inputBlocks.getOrNull(1)

                    if (variable == null) {
                        return BlockOrderResult(
                            isValid = false,
                            errorMessage = "❌ Error: Set block is missing target variable."
                        )
                    }

                    if (valueBlock == null) {
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