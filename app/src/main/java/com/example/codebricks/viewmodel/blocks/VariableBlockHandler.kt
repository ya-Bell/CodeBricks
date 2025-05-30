package com.example.codebricks.viewmodel.blocks


import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel


// Создание новой переменной и добавление блока объявления в программу
fun VariableViewModel.declareVariable(name: String, value: Any, type: String) {
    val newVariable = Variable(name, value, type)
    _variables.value += newVariable

    val block = Block(
        type = BlockType.VARIABLE_DECLARE, value = newVariable
    )
    redrawTrigger.intValue++
    addBlock(block)
}

// Проверка, была ли уже объявлена переменная с таким именем
fun VariableViewModel.isVariableAlreadyDeclared(name: String): Boolean {
    return variables.any { it.name == name }
}

// Создание пустого блока изменения переменной (VARIABLE_CHANGE)
fun VariableViewModel.declareEmptyChangeVariableBlock() {
    val firstVar = variables.firstOrNull()
    val referenceBlock = firstVar?.let {
        Block(type = BlockType.VARIABLE_REFERENCE, value = it)
    }

    val changeBlock = Block(
        type = BlockType.VARIABLE_CHANGE,
        inputBlocks = referenceBlock?.let { mutableListOf(it) } ?: mutableListOf()
    ).apply {
        changeSign = "+"
        changeAmount = 0
    }

    // Добавляем только change блок, reference блок уже включен в него
    addBlock(changeBlock)
}

// Обновление переменной, которую изменяет блок VARIABLE_CHANGE
// Функция для поиска всех используемых reference блоков
fun VariableViewModel.findUsedReferenceBlocks(blocks: List<Block>): Set<String> {
    val usedReferenceIds = mutableSetOf<String>()

    fun traverse(block: Block) {
        block.inputBlocks.forEach { inputBlock ->
            if (inputBlock != null) {
                if (inputBlock.type == BlockType.VARIABLE_REFERENCE) {
                    usedReferenceIds.add(inputBlock.id)
                }
                traverse(inputBlock)
            }
        }
    }

    blocks.forEach { traverse(it) }
    return usedReferenceIds
}

// Обновленная функция updateChangeBlockVariable
fun VariableViewModel.updateChangeBlockVariable(blockId: String, variable: Variable) {
    val oldReferenceIds = mutableSetOf<String>()

    // Сначала найдем ID старого reference блока
    _programBlocks.value.find { it.id == blockId }?.inputBlocks?.firstOrNull()?.let {
        if (it.type == BlockType.VARIABLE_REFERENCE) {
            oldReferenceIds.add(it.id)
        }
    }

    // Обновляем change блок с новым reference блоком
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

    // Находим все используемые reference блоки
    val usedReferenceIds = findUsedReferenceBlocks(_programBlocks.value)

    // Удаляем неиспользуемые reference блоки
    _programBlocks.value = _programBlocks.value.filterNot { block ->
        block.type == BlockType.VARIABLE_REFERENCE && block.id !in usedReferenceIds
    }

    redrawTrigger.intValue++
}

// Обновление знака изменения в блоке VARIABLE_CHANGE
fun VariableViewModel.updateChangeBlockSign(blockId: String, operation: String) {
    _programBlocks.value = _programBlocks.value.map { block ->
        if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
            block.changeSign = operation
        }
        block
    }
    redrawTrigger.intValue++
}

// Обновление величины изменения в блоке VARIABLE_CHANGE
fun VariableViewModel.updateChangeBlockAmount(blockId: String, amount: Int) {
    _programBlocks.value = _programBlocks.value.map { block ->
        if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
            block.changeAmount = amount
        }
        block
    }
    redrawTrigger.intValue++
}
