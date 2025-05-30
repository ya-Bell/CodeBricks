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

    // Флаги для обработки ввода
    private var waitingForInput = false
    private var waitingVariable: Variable? = null
    private var programExecution: (() -> Unit)? = null

    // Map для хранения объявленных переменных
    private val declaredVariables = mutableMapOf<String, Variable>()

    // Очистка консоли
    fun clearConsole() {
        consoleOutput.value = "Console cleared."
    }

    // Добавить сообщение в консоль
    private fun logToConsole(message: String, isPrintOutput: Boolean = false, isError: Boolean = false) {
        // Если режим отладки выключен, показываем только:
        // - вывод print
        // - инициализацию и остановку программы
        // - сообщения об ошибках ввода
        if (!isDebugMode.value && !isPrintOutput && !isError && 
            !message.startsWith("🟢") && !message.startsWith("🔴") && 
            !message.startsWith("Console")) {
            return
        }
        consoleOutput.value += "\n$message"
    }

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

    // Состояние выполнения программы
    private data class ExecutionState(
        var currentBlockId: String? = null,
        var skipUntilEndIf: Boolean = false,
        var wasConditionMet: Boolean = false,
        val conditionStack: MutableList<Boolean> = mutableListOf()
    )

    private val executionState = ExecutionState()
    private var continueExecutionCallback: (() -> Unit)? = null

    // Исполнение блоков программы
    fun executeProgram(onFinish: () -> Unit) {
        val blocksById = programBlocks.associateBy { it.id }
        declaredVariables.clear()
        
        // Инициализация состояния
        executionState.apply {
            currentBlockId = programBlocks.find { it.type == BlockType.CONTROL_START }?.id
            skipUntilEndIf = false
            wasConditionMet = false
            conditionStack.clear()
        }

        fun executeBlock(block: Block): Boolean {
            when (block.type) {
                BlockType.CONTROL_START -> {
                    logToConsole("🟢 Program started")
                }
                BlockType.CONTROL_STOP -> {
                    if (!executionState.skipUntilEndIf) {
                        logToConsole("🔴 Program stopped")
                        return false
                    }
                }
                BlockType.IF -> {
                    executionState.wasConditionMet = evaluateCondition(block, declaredVariables)
                    executionState.conditionStack.add(executionState.wasConditionMet)
                    executionState.skipUntilEndIf = !executionState.wasConditionMet
                    if (executionState.wasConditionMet) {
                        logToConsole("✅ IF condition is true")
                    } else {
                        logToConsole("❌ IF condition is false")
                    }
                }
                BlockType.ELSE_IF -> {
                    if (!executionState.wasConditionMet) {
                        executionState.wasConditionMet = evaluateCondition(block, declaredVariables)
                        executionState.skipUntilEndIf = !executionState.wasConditionMet
                        if (executionState.wasConditionMet) {
                            logToConsole("✅ ELSE IF condition is true")
                        } else {
                            logToConsole("❌ ELSE IF condition is false")
                        }
                    } else {
                        executionState.skipUntilEndIf = true
                    }
                }
                BlockType.ELSE -> {
                    if (!executionState.wasConditionMet) {
                        executionState.skipUntilEndIf = false
                        executionState.wasConditionMet = true
                        logToConsole("✅ ELSE block executed")
                    } else {
                        executionState.skipUntilEndIf = true
                    }
                }
                BlockType.END_IF -> {
                    if (executionState.conditionStack.isNotEmpty()) {
                        executionState.conditionStack.removeAt(executionState.conditionStack.lastIndex)
                    }
                    executionState.skipUntilEndIf = false
                    executionState.wasConditionMet = false
                }
                else -> {
                    if (!executionState.skipUntilEndIf) {
                        when (block.type) {
                            BlockType.VARIABLE_DECLARE -> {
                                (block.value as? Variable)?.let { variable ->
                                    declaredVariables[variable.name] = variable.copy()
                                    logToConsole("✅ Declared ${variable.name} = ${variable.value}")
                                }
                            }
                            BlockType.VARIABLE_SET -> {
                                val targetVar = block.inputBlocks.getOrNull(0)?.value as? Variable
                                val valueBlock = block.inputBlocks.getOrNull(1)

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
                                        logToConsole("✅ Set ${memoryVar.name} = ${memoryVar.value}")
                                    } ?: logToConsole("❌ Error: variable '${targetVar.name}' not declared")
                                }
                            }
                            BlockType.IO_PRINT -> {
                                val variable = block.inputBlocks.firstOrNull()?.value as? Variable
                                if (variable != null) {
                                    declaredVariables[variable.name]?.let { target ->
                                        logToConsole("📤 Output: ${target.name} = ${target.value}", true)
                                    } ?: logToConsole("❌ Error: variable '${variable.name}' not declared", true)
                                } else {
                                    val text = block.value as? String
                                    if (text != null) {
                                        logToConsole("📤 Output: $text", true)
                                    }
                                }
                            }
                            BlockType.IO_WRITE -> {
                                (block.value as? Variable)?.let { variable ->
                                    declaredVariables[variable.name]?.let { target ->
                                        logToConsole("📥 Input: Enter value for ${target.name}:", true)
                                        waitingForInput = true
                                        waitingVariable = variable
                                        programExecution = continueExecutionCallback
                                        return false
                                    } ?: logToConsole("❌ Error: variable '${variable.name}' not declared", true)
                                }
                            }
                            BlockType.MATH_ADD, BlockType.MATH_SUBTRACT, BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE -> {
                                val result = MathExpressionEvaluator.evaluate(block)
                                logToConsole("🧮 Result of math expression: $result")
                            }
                            else -> {}
                        }
                    }
                }
            }
            return true
        }

        fun continueExecution() {
            var shouldContinue = true
            while (shouldContinue && executionState.currentBlockId != null && !waitingForInput) {
                val currentBlock = blocksById[executionState.currentBlockId]
                if (currentBlock != null) {
                    shouldContinue = executeBlock(currentBlock)
                    executionState.currentBlockId = currentBlock.nextBlockId
                } else {
                    shouldContinue = false
                }
            }
            
            if (!waitingForInput) {
                onFinish()
            }
        }

        // Сохраняем callback для продолжения выполнения
        continueExecutionCallback = ::continueExecution
        continueExecution()
    }

    // Обработка пользовательского ввода в консоли
    fun processUserInput(input: String) {
        consoleOutput.value += "\n> $input"
        
        if (waitingForInput && waitingVariable != null) {
            val variable = waitingVariable!!
            val declaredVar = declaredVariables[variable.name]
            
            if (declaredVar != null) {
                // Заменяем запятую на точку для чисел
                val processedInput = input.replace(",", ".")
                
                // Пытаемся преобразовать значение в соответствии с типом переменной
                val conversionResult = when (declaredVar.type) {
                    "int" -> {
                        processedInput.toIntOrNull()?.let { 
                            Result.success(it as Any)
                        } ?: Result.failure(Exception("Expected an integer number"))
                    }
                    "double" -> {
                        processedInput.toDoubleOrNull()?.let {
                            Result.success(it as Any)
                        } ?: Result.failure(Exception("Expected a decimal number"))
                    }
                    "bool" -> {
                        when (processedInput.lowercase()) {
                            "true", "1" -> Result.success(true as Any)
                            "false", "0" -> Result.success(false as Any)
                            else -> Result.failure(Exception("Expected a boolean value (true/false or 1/0)"))
                        }
                    }
                    else -> Result.success(processedInput as Any)
                }

                when {
                    conversionResult.isSuccess -> {
                        declaredVar.value = conversionResult.getOrNull()!!
                        logToConsole("✅ Input accepted: ${declaredVar.name} = ${declaredVar.value}", true)
                        logToConsole("-------------------", true)
                        
                        // Сбрасываем флаги ожидания
                        waitingForInput = false
                        waitingVariable = null
                        
                        // Продолжаем выполнение программы
                        programExecution?.invoke()
                    }
                    conversionResult.isFailure -> {
                        val errorMessage = conversionResult.exceptionOrNull()?.message ?: "Invalid format"
                        logToConsole("❌ Error: $errorMessage", isError = true)
                        logToConsole("❌ Variable '${declaredVar.name}' expects type: ${declaredVar.type}", isError = true)
                        logToConsole("📥 Please enter the value again:", isError = true)
                    }
                }
            }
        }
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
        skipNextBlock = false
        waitingForInput = false
        waitingVariable = null
        programExecution = null
        declaredVariables.clear()
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
                declaredVariables[variable?.name]?.value ?: variable?.value
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
                declaredVariables[variable?.name]?.value ?: variable?.value
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

        // Стринговое сравнение
        if (leftValue is String || rightValue is String) {
            val leftStr = leftValue.toString()
            val rightStr = rightValue.toString()
            
            val result = when (block.operator) {
                "==" -> leftStr == rightStr
                "!=" -> leftStr != rightStr
                "<" -> leftStr < rightStr
                ">" -> leftStr > rightStr
                "<=" -> leftStr <= rightStr
                ">=" -> leftStr >= rightStr
                else -> {
                    logToConsole("❌ Unsupported operator for strings: ${block.operator}")
                    return false
                }
            }
            return result
        }

        // Автоматическое поддеражание типов если есть возможность
        val leftNum = when (leftValue) {
            is Number -> leftValue.toDouble()
            is String -> leftValue.toDoubleOrNull() ?: run {
                logToConsole("❌ Left operand cannot be converted to number: $leftValue")
                return false
            }
            else -> {
                logToConsole("❌ Left operand is not a number or string: $leftValue")
                return false
            }
        }

        val rightNum = when (rightValue) {
            is Number -> rightValue.toDouble()
            is String -> rightValue.toDoubleOrNull() ?: run {
                logToConsole("❌ Right operand cannot be converted to number: $rightValue")
                return false
            }
            else -> {
                logToConsole("❌ Right operand is not a number or string: $rightValue")
                return false
            }
        }

        return when (block.operator) {
            "==" -> leftNum == rightNum
            "!=" -> leftNum != rightNum
            "<" -> leftNum < rightNum
            ">" -> leftNum > rightNum
            "<=" -> leftNum <= rightNum
            ">=" -> leftNum >= rightNum
            else -> {
                logToConsole("❌ Unsupported operator: ${block.operator}")
                false
            }
        }
    }
}
