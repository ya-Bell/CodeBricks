package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.conversion.formatValueForOutput
import com.example.codebricks.viewmodel.tree.findBlockById

// Модель данных для переменной
data class Variable(val name: String, var value: Any, var type: String)

class VariableViewModel : ViewModel() {
    // Результат проверки порядка блоков (используется в checkBlockOrder)
    data class BlockOrderResult(
        val isValid: Boolean, val errorMessage: String? = null
    )

    // Флаг для отрисовки соединений между блоками
    val shouldDrawConnections = mutableStateOf(false)

    // Хранилище переменных в "памяти"
    val _variables = mutableStateOf<List<Variable>>(emptyList())
    val variables: List<Variable> get() = _variables.value

    // Список всех блоков в программе
    val _programBlocks = mutableStateOf<List<Block>>(emptyList())
    val programBlocks: List<Block> get() = _programBlocks.value

    // Вывод в консоль
    val consoleOutput = mutableStateOf("Console ready.")

    // Последний слот, в который была произведена вставка (используется для анимации)
    val recentlyInsertedSlot = mutableStateOf<Pair<String, Int>?>(null)

    // Слот, подсвеченный для вставки (используется во время drag'n'drop)
    val highlightedSlot = mutableStateOf<Pair<String, Int>?>(null)


    // Очистка консоли
    fun clearConsole() {
        consoleOutput.value = "Console cleared."
    }

    // Добавить сообщение в консоль
    private fun logToConsole(message: String) {
        consoleOutput.value += "\n$message"
    }

    // Добавление блока в программу и перерисовка
    fun addBlock(block: Block) {
        _programBlocks.value += block
        redrawTrigger.intValue++
    }

    // Очистка всех блоков и переменных с канваса
    fun clearWorkspace() {
        _variables.value = emptyList()
        _programBlocks.value = emptyList()
        shouldDrawConnections.value = false
        BlockPositionTracker.clear()
        BlockSlotTracker.clear()
        highlightedSlot.value = null
        recentlyInsertedSlot.value = null
    }

    // Удаление блока по ID (только верхнеуровневого)
    fun removeBlockById(blockId: String) {
        _programBlocks.value = _programBlocks.value.filterNot { it.id == blockId }
        redrawTrigger.intValue++
    }

    // Поднятие блока в самый верх визуально (перемещение в конец списка)
    fun bringBlockToFront(id: String) {
        val block = findBlockById(id) ?: return
        _programBlocks.value = _programBlocks.value.filterNot { it.id == id } + block
        redrawTrigger.intValue++
    }

    // Исполнение блоков программы
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

                BlockType.VARIABLE_REFERENCE -> {}

                // Остальные блоки пока не реализованы
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

}
