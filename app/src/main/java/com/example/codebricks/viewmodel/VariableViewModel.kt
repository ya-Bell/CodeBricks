package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.blocks.findUsedReferenceBlocks
import com.example.codebricks.viewmodel.tree.findBlockById
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val conditionStack: MutableList<Boolean> = mutableListOf(),
        val whileLoopStack: MutableList<String> = mutableListOf(),
        var skipUntilWhileEnd: Boolean = false,
        // Храним последовательность блоков для каждого цикла
        val whileLoopSequence: MutableMap<String, MutableList<String>> = mutableMapOf(),
        var isCollectingSequence: Boolean = false,
        var currentWhileId: String? = null,
        // Добавляем счетчик итераций для каждого цикла
        val whileLoopIterations: MutableMap<String, Int> = mutableMapOf(),
        // Максимальное количество итераций
        val MAX_ITERATIONS: Int = 1000,
        // Добавляем время начала выполнения цикла
        var currentLoopStartTime: Long = 0,
        // Максимальное время выполнения одной итерации (в миллисекундах)
        val MAX_ITERATION_TIME: Long = 1000,
        // Флаг для остановки выполнения
        var shouldStop: Boolean = false
    )

    private val executionState = ExecutionState()
    private var continueExecutionCallback: (() -> Unit)? = null

    // Добавляем корутину для выполнения программы
    private var executionJob: kotlinx.coroutines.Job? = null

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
            whileLoopStack.clear()
            whileLoopSequence.clear()
            whileLoopIterations.clear()
            skipUntilWhileEnd = false
            isCollectingSequence = false
            currentWhileId = null
            currentLoopStartTime = 0
            shouldStop = false
        }

        // Запускаем выполнение в отдельном потоке
        executionJob = GlobalScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            try {
                // Запускаем сторожевой таймер в отдельной корутине
                val watchdogJob = launch {
                    while (isActive) {
                        if (executionState.whileLoopStack.isNotEmpty()) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - executionState.currentLoopStartTime > executionState.MAX_ITERATION_TIME) {
                                withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    logToConsole("⚠️ Warning: Maximum iteration time exceeded (${executionState.MAX_ITERATION_TIME}ms). Loop stopped.", isError = true)
                                }
                                executionState.shouldStop = true
                                break
                            }
                        }
                        kotlinx.coroutines.delay(100) // Проверяем каждые 100мс
                    }
                }

                fun executeBlock(block: Block): Boolean {
                    if (executionState.shouldStop) {
                        return false
                    }

                    when (block.type) {
                        BlockType.CONTROL_START -> {
                            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                logToConsole("🟢 Program started")
                            }
                        }
                        BlockType.CONTROL_STOP -> {
                            if (!executionState.skipUntilEndIf) {
                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                    logToConsole("🔴 Program stopped")
                                }
                                return false
                            }
                        }
                        BlockType.IF -> {
                            executionState.wasConditionMet = evaluateCondition(block, declaredVariables)
                            executionState.conditionStack.add(executionState.wasConditionMet)
                            executionState.skipUntilEndIf = !executionState.wasConditionMet
                            if (executionState.wasConditionMet) {
                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                    logToConsole("✅ IF condition is true")
                                }
                            } else {
                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                    logToConsole("❌ IF condition is false")
                                }
                            }
                        }
                        BlockType.ELSE_IF -> {
                            if (!executionState.wasConditionMet) {
                                executionState.wasConditionMet = evaluateCondition(block, declaredVariables)
                                executionState.skipUntilEndIf = !executionState.wasConditionMet
                                if (executionState.wasConditionMet) {
                                    GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                        logToConsole("✅ ELSE IF condition is true")
                                    }
                                } else {
                                    GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                        logToConsole("❌ ELSE IF condition is false")
                                    }
                                }
                            } else {
                                executionState.skipUntilEndIf = true
                            }
                        }
                        BlockType.ELSE -> {
                            if (!executionState.wasConditionMet) {
                                executionState.skipUntilEndIf = false
                                executionState.wasConditionMet = true
                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                    logToConsole("✅ ELSE block executed")
                                }
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
                        BlockType.WHILE -> {
                            val condition = evaluateCondition(block, declaredVariables)
                            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                logToConsole("🔄 Checking WHILE condition: $condition")
                            }
                            if (condition) {
                                val iterations = executionState.whileLoopIterations.getOrDefault(block.id, 0)
                                if (iterations >= executionState.MAX_ITERATIONS) {
                                    GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                        logToConsole("⚠️ Warning: Maximum number of iterations reached (${executionState.MAX_ITERATIONS}). Loop stopped.", isError = true)
                                    }
                                    executionState.skipUntilWhileEnd = true
                                    return true
                                }
                                
                                executionState.whileLoopStack.add(block.id)
                                executionState.whileLoopIterations[block.id] = iterations + 1
                                executionState.isCollectingSequence = true
                                executionState.currentWhileId = block.id
                                executionState.whileLoopSequence[block.id] = mutableListOf()
                                executionState.currentLoopStartTime = System.currentTimeMillis()
                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                    logToConsole("✅ WHILE condition is true, entering loop (iteration ${iterations + 1})")
                                }
                            } else {
                                executionState.skipUntilWhileEnd = true
                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                    logToConsole("❌ WHILE condition is false, skipping loop")
                                }
                            }
                        }
                        BlockType.WHILE_END -> {
                            if (executionState.whileLoopStack.isNotEmpty()) {
                                val whileBlockId = executionState.whileLoopStack.last()
                                val whileBlock = findBlockById(whileBlockId)
                                if (whileBlock != null) {
                                    if (executionState.isCollectingSequence) {
                                        executionState.isCollectingSequence = false
                                        executionState.currentWhileId = null
                                    }
                                    
                                    val condition = evaluateCondition(whileBlock, declaredVariables)
                                    GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                        logToConsole("🔄 Checking WHILE condition again: $condition")
                                    }
                                    if (condition) {
                                        val iterations = executionState.whileLoopIterations.getOrDefault(whileBlockId, 0)
                                        if (iterations >= executionState.MAX_ITERATIONS) {
                                            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("⚠️ Warning: Maximum number of iterations reached (${executionState.MAX_ITERATIONS}). Loop stopped.", isError = true)
                                            }
                                            executionState.whileLoopStack.removeAt(executionState.whileLoopStack.lastIndex)
                                            executionState.whileLoopSequence.remove(whileBlockId)
                                            executionState.whileLoopIterations.remove(whileBlockId)
                                            return true
                                        }

                                        executionState.currentLoopStartTime = System.currentTimeMillis()
                                        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                            logToConsole("🔄 WHILE condition still true, repeating loop (iteration ${iterations + 1})")
                                        }
                                        
                                        val sequence = executionState.whileLoopSequence[whileBlockId] ?: mutableListOf()
                                        for (blockId in sequence) {
                                            executeBlock(findBlockById(blockId) ?: return true)
                                        }
                                        executionState.currentBlockId = whileBlock.nextBlockId
                                        return true
                                    } else {
                                        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                            logToConsole("✅ WHILE loop completed after ${executionState.whileLoopIterations[whileBlockId]} iterations")
                                        }
                                        executionState.whileLoopStack.removeAt(executionState.whileLoopStack.lastIndex)
                                        executionState.whileLoopSequence.remove(whileBlockId)
                                        executionState.whileLoopIterations.remove(whileBlockId)
                                        executionState.currentBlockId = block.nextBlockId
                                    }
                                }
                            }
                            executionState.skipUntilWhileEnd = false
                        }
                        else -> {
                            if (!executionState.skipUntilEndIf && !executionState.skipUntilWhileEnd) {
                                if (executionState.isCollectingSequence && executionState.currentWhileId != null) {
                                    executionState.whileLoopSequence[executionState.currentWhileId]?.add(block.id)
                                }
                                
                                when (block.type) {
                                    BlockType.VARIABLE_DECLARE -> {
                                        (block.value as? Variable)?.let { variable ->
                                            declaredVariables[variable.name] = variable.copy()
                                            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("✅ Declared ${variable.name} = ${variable.value}")
                                            }
                                        }
                                    }
                                    BlockType.VARIABLE_SET -> {
                                        val targetVar = block.inputBlocks.getOrNull(0)?.value as? Variable
                                        val valueBlock = block.inputBlocks.getOrNull(1)

                                        if (targetVar == null) {
                                            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("❌ Error: No variable selected in Set block")
                                            }
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
                                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                    logToConsole("✅ Set ${memoryVar.name} = ${memoryVar.value}")
                                                }
                                            } ?: GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("❌ Error: Variable '${targetVar.name}' not declared")
                                            }
                                        }
                                    }
                                    BlockType.IO_PRINT -> {
                                        val variable = block.inputBlocks.firstOrNull()?.value as? Variable
                                        if (variable != null) {
                                            declaredVariables[variable.name]?.let { target ->
                                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                    logToConsole("📤 Output: ${target.name} = ${target.value}", true)
                                                }
                                            } ?: GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("❌ Error: Variable '${variable.name}' not declared", true)
                                            }
                                        } else {
                                            val text = block.value as? String
                                            if (text != null) {
                                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                    logToConsole("📤 Output: $text", true)
                                                }
                                            }
                                        }
                                    }
                                    BlockType.IO_WRITE -> {
                                        (block.value as? Variable)?.let { variable ->
                                            declaredVariables[variable.name]?.let { target ->
                                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                    logToConsole("📥 Input: Enter value for ${target.name}:", true)
                                                    waitingForInput = true
                                                    waitingVariable = variable
                                                    programExecution = continueExecutionCallback
                                                    return@launch
                                                }
                                            } ?: GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("❌ Error: Variable '${variable.name}' not declared", true)
                                            }
                                        }
                                    }
                                    BlockType.MATH_ADD, BlockType.MATH_SUBTRACT, BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE,BlockType.MATH_MODULO -> {
                                        val result = MathExpressionEvaluator.evaluate(block)
                                        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                            logToConsole("🧮 Math expression result: $result")
                                        }
                                    }
                                    BlockType.VARIABLE_CHANGE -> {
                                        val targetVar = block.inputBlocks.getOrNull(0)?.value as? Variable
                                        if (targetVar == null) {
                                            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("❌ Error: No variable selected in Change block")
                                            }
                                        } else {
                                            declaredVariables[targetVar.name]?.let { memoryVar ->
                                                val currentValue = when (memoryVar.value) {
                                                    is Int -> memoryVar.value as Int
                                                    is Double -> (memoryVar.value as Double).toInt()
                                                    else -> 0
                                                }
                                                val changeAmount = block.changeAmount
                                                val newValue = when (block.changeSign) {
                                                    "+" -> currentValue + changeAmount
                                                    "-" -> currentValue - changeAmount
                                                    "*" -> currentValue * changeAmount
                                                    "/" -> if (changeAmount != 0) currentValue / changeAmount else currentValue
                                                    "%" -> if (changeAmount != 0) currentValue % changeAmount else currentValue
                                                    else -> currentValue
                                                }
                                                memoryVar.value = when (memoryVar.type) {
                                                    "int" -> newValue
                                                    "double" -> newValue.toDouble()
                                                    else -> newValue
                                                }
                                                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                    logToConsole("✅ Changed ${memoryVar.name} by ${block.changeSign}$changeAmount to ${memoryVar.value}")
                                                }
                                            } ?: GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                logToConsole("❌ Error: Variable '${targetVar.name}' not declared")
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                    return true
                }

                var shouldContinue = true
                while (shouldContinue && executionState.currentBlockId != null && !waitingForInput) {
                    val currentBlock = blocksById[executionState.currentBlockId]
                    if (currentBlock != null) {
                        shouldContinue = executeBlock(currentBlock)
                        if (shouldContinue && executionState.currentBlockId == currentBlock.id) {
                            executionState.currentBlockId = currentBlock.nextBlockId
                        }
                    } else {
                        shouldContinue = false
                    }
                }

                watchdogJob.cancel() // Останавливаем сторожевой таймер

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (!waitingForInput) {
                        onFinish()
                    }
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    logToConsole("❌ Error: ${e.message}", isError = true)
                    onFinish()
                }
            }
        }
    }

    // Добавляем метод для остановки выполнения
    fun stopExecution() {
        executionState.shouldStop = true
        executionJob?.cancel()
        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            logToConsole("🛑 Program execution stopped", isError = true)
        }
    }

    // Обработка пользовательского ввода в консоли
    fun processUserInput(input: String) {
        consoleOutput.value += "\n> $input"
        
        if (waitingForInput && waitingVariable != null) {
            val variable = waitingVariable!!
            val declaredVar = declaredVariables[variable.name]
            
            if (declaredVar != null) {
                val processedInput = input.replace(",", ".")
                
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
                        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            logToConsole("✅ Input accepted: ${declaredVar.name} = ${declaredVar.value}", true)
                        }

                        waitingForInput = false
                        waitingVariable = null
                        
                        programExecution?.invoke()
                    }
                    conversionResult.isFailure -> {
                        val errorMessage = conversionResult.exceptionOrNull()?.message ?: "Invalid format"
                        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            logToConsole("❌ Error: $errorMessage", isError = true)
                            logToConsole("❌ Variable '${declaredVar.name}' expects type: ${declaredVar.type}", isError = true)
                            logToConsole("📥 Please enter the value again:", isError = true)
                        }
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
            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                logToConsole("❌ Error: Condition operator not specified")
            }
            return false
        }

        val leftBlock = block.inputBlocks.getOrNull(0)
        val rightBlock = block.inputBlocks.getOrNull(1)

        if (leftBlock == null || rightBlock == null) {
            GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                logToConsole("❌ Error: Condition operands not set")
            }
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
            BlockType.MATH_DIVIDE,
            BlockType.MATH_MODULO -> {
                MathExpressionEvaluator.evaluate(leftBlock)
            }
            else -> {
                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    logToConsole("❌ Error: Unsupported left operand type: ${leftBlock.type}")
                }
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
            BlockType.MATH_DIVIDE,
            BlockType.MATH_MODULO -> {
                MathExpressionEvaluator.evaluate(rightBlock)
            }
            else -> rightBlock.value // Добавляем прямое значение для числовых констант
        }

        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            logToConsole("🔍 Debug: Comparing left($leftValue: ${leftValue?.javaClass?.simpleName}) ${block.operator} right($rightValue: ${rightValue?.javaClass?.simpleName})")
        }

        // Числовое сравнение (если не строки и не boolean)
        val leftNum = when (leftValue) {
            is Number -> leftValue.toDouble()
            is String -> leftValue.toDoubleOrNull() ?: run {
                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    logToConsole("❌ Error: Left operand cannot be converted to number: $leftValue")
                }
                return false
            }
            else -> {
                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    logToConsole("❌ Error: Left operand is not a number or string: $leftValue")
                }
                return false
            }
        }

        val rightNum = when (rightValue) {
            is Number -> rightValue.toDouble()
            is String -> rightValue.toString().toDoubleOrNull() ?: run {
                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    logToConsole("❌ Error: Right operand cannot be converted to number: $rightValue")
                }
                return false
            }
            else -> {
                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    logToConsole("❌ Error: Right operand is not a number or string: $rightValue")
                }
                return false
            }
        }

        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            logToConsole("🔍 Debug: Converted to numbers - left: $leftNum, right: $rightNum")
        }

        val result = when (block.operator) {
            "==" -> leftNum == rightNum
            "!=" -> leftNum != rightNum
            "<" -> leftNum < rightNum
            ">" -> leftNum > rightNum
            "<=" -> leftNum <= rightNum
            ">=" -> leftNum >= rightNum
            else -> {
                GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    logToConsole("❌ Error: Unsupported operator: ${block.operator}")
                }
                false
            }
        }

        GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            logToConsole("🔍 Debug: Comparison result: $result")
        }
        return result
    }
}
