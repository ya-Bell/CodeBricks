package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.blocks.findUsedReferenceBlocks
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

    // Флаг режима отладки
    val isDebugMode = mutableStateOf(false)

    // Последний слот, в который была произведена вставка (используется для анимации)
    val recentlyInsertedSlot = mutableStateOf<Pair<String, Int>?>(null)

    // Слот, подсвеченный для вставки (используется во время drag'n'drop)
    val highlightedSlot = mutableStateOf<Pair<String, Int>?>(null)

    var skipNextBlock = false



    //ОТКЛАДКА
    fun logAllProgramBlocks() {
        println("📦 Current program blocks:")
        programBlocks.forEachIndexed { index, block ->
            println("$index. ${block.type} (${block.id})")
            block.inputBlocks.forEachIndexed { i, input ->
                println("   ↳ input[$i]: ${input?.type} (${input?.id})")
            }
        }
    }

    //ОТКЛАДКА


    // Очистка консоли
    fun clearConsole() {
        consoleOutput.value = "Console cleared."
    }

    // Добавить сообщение в консоль
    private fun logToConsole(message: String) {
        // Если режим отладки выключен, показываем только вывод print и инициализацию
        if (!isDebugMode.value && !message.startsWith("📤") && !message.startsWith("🟢") && !message.startsWith("🔴") && !message.startsWith("Console")) {
            return
        }
        consoleOutput.value += "\n$message"
    }

    // Добавление блока в программу и перерисовка
    fun addBlock(block: Block) {
        _programBlocks.value += block
        logAllProgramBlocks()
        // Вызываем перерисовку только один раз после всех изменений
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

        // После удаления блока, очищаем неиспользуемые reference блоки
        val usedReferenceIds = findUsedReferenceBlocks(_programBlocks.value)
        _programBlocks.value = _programBlocks.value.filterNot { block ->
            block.type == BlockType.VARIABLE_REFERENCE && block.id !in usedReferenceIds
        }

        logAllProgramBlocks()
        redrawTrigger.intValue++
    }

    // Перемещает блок на передний план
    fun bringBlockToFront(blockId: String) {
        val block = findBlockById(blockId) ?: return
        val updatedBlocks = _programBlocks.value.toMutableList()
        updatedBlocks.removeAll { it.id == blockId }
        updatedBlocks.add(block)
        _programBlocks.value = updatedBlocks
    }

    fun evaluateCondition(block: Block, declaredVariables: Map<String, Variable>): Boolean {
        if (block.operator.isBlank()) {
            logToConsole("❌ Condition operator not specified")
            return false
        }

        val leftBlock = block.inputBlocks.getOrNull(0)
        val rightBlock = block.inputBlocks.getOrNull(1)

        if (leftBlock == null || rightBlock == null) {
            logToConsole("❌ Condition operands not set")
            return false
        }

        // Вычисляем значения левой и правой части с учетом вложенных математических блоков
        val leftValue = when (leftBlock.type) {
            BlockType.VARIABLE_REFERENCE -> {
                val variable = leftBlock.value as? Variable
                (declaredVariables[variable?.name]?.value ?: variable?.value)
                    ?.toString()?.toDoubleOrNull() ?: run {
                    logToConsole("❌ Left operand is not a number")
                    return false
                }
            }
            BlockType.MATH_ADD,
            BlockType.MATH_SUBTRACT,
            BlockType.MATH_MULTIPLY,
            BlockType.MATH_DIVIDE -> {
                MathExpressionEvaluator.evaluate(leftBlock)
            }
            else -> {
                logToConsole("❌ Unsupported left operand type: ${leftBlock.type}")
                return false
            }
        }

        val rightValue = when (rightBlock.type) {
            BlockType.VARIABLE_REFERENCE -> {
                val variable = rightBlock.value as? Variable
                (declaredVariables[variable?.name]?.value ?: variable?.value)
                    ?.toString()?.toDoubleOrNull() ?: run {
                    logToConsole("❌ Right operand is not a number")
                    return false
                }
            }
            BlockType.MATH_ADD,
            BlockType.MATH_SUBTRACT,
            BlockType.MATH_MULTIPLY,
            BlockType.MATH_DIVIDE -> {
                MathExpressionEvaluator.evaluate(rightBlock)
            }
            else -> {
                logToConsole("❌ Unsupported right operand type: ${rightBlock.type}")
                return false
            }
        }

        println("Evaluating condition: $leftValue ${block.operator} $rightValue")

        return when (block.operator) {
            "==" -> leftValue == rightValue
            "!=" -> leftValue != rightValue
            ">"  -> leftValue > rightValue
            "<"  -> leftValue < rightValue
            ">=" -> leftValue >= rightValue
            "<=" -> leftValue <= rightValue
            else -> {
                logToConsole("❌ Unsupported operator: ${block.operator}")
                false
            }
        }
    }

    // Исполнение блоков программы
    fun executeProgram(onFinish: () -> Unit) {
        val blocksById = programBlocks.associateBy { it.id }
        val declaredVariables = mutableMapOf<String, Variable>()
        var current = programBlocks.find { it.type == BlockType.CONTROL_START }
        
        // Стек для отслеживания условных блоков
        val conditionStack = mutableListOf<Boolean>()
        var skipUntilEndIf = false
        var wasConditionMet = false // Флаг для отслеживания выполненного условия в цепочке if-else

        while (current != null) {
            when (current.type) {
                BlockType.CONTROL_START -> logToConsole("🟢 Program started")
                BlockType.CONTROL_STOP -> logToConsole("🔴 Program stopped")

                BlockType.IF -> {
                    wasConditionMet = evaluateCondition(current, declaredVariables)
                    conditionStack.add(wasConditionMet)
                    skipUntilEndIf = !wasConditionMet
                    if (wasConditionMet) {
                        logToConsole("✅ IF condition is true")
                        logToConsole("⏭️ Next ELSE IF blocks will be skipped")
                    } else {
                        logToConsole("❌ IF condition is false")
                        logToConsole("➡️ Moving to next condition")
                    }
                }

                BlockType.ELSE_IF -> {
                    if (!wasConditionMet) {
                        // Проверяем условие ELSE IF только если предыдущие условия не выполнились
                        wasConditionMet = evaluateCondition(current, declaredVariables)
                        skipUntilEndIf = !wasConditionMet
                        if (wasConditionMet) {
                            logToConsole("✅ ELSE IF condition is true")
                            logToConsole("⏭️ Next ELSE IF/ELSE blocks will be skipped")
                        } else {
                            logToConsole("❌ ELSE IF condition is false")
                            logToConsole("➡️ Moving to next condition")
                        }
                    } else {
                        // Если предыдущее условие выполнилось, пропускаем этот блок
                        skipUntilEndIf = true
                        logToConsole("⏭️ Skipping ELSE IF block (previous condition was true)")
                    }
                }

                BlockType.ELSE -> {
                    if (!wasConditionMet) {
                        // Выполняем ELSE только если ни одно из предыдущих условий не выполнилось
                        skipUntilEndIf = false
                        wasConditionMet = true
                        logToConsole("✅ ELSE block executed")
                    } else {
                        // Если предыдущее условие выполнилось, пропускаем этот блок
                        skipUntilEndIf = true
                        logToConsole("⏭️ Skipping ELSE block (previous condition was true)")
                    }
                }

                BlockType.END_IF -> {
                    if (conditionStack.isNotEmpty()) {
                        conditionStack.removeAt(conditionStack.lastIndex)
                    }
                    skipUntilEndIf = false
                    wasConditionMet = false
                    logToConsole("✅ END IF reached - condition chain completed")
                }

                else -> {
                    if (!skipUntilEndIf) {
                        when (current.type) {
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

                            else -> {}
                        }
                    }
                }
            }
            current = current.nextBlockId?.let { blocksById[it] }
        }
        onFinish()
    }

}
