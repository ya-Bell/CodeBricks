package com.example.codebricks.viewmodel.blocks


import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger

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
        inputBlocks = referenceBlock?.let { mutableListOf(it) } ?: mutableListOf()).apply {
        changeSign = "+"
        changeAmount = 0
    }

    redrawTrigger.intValue++
    addBlock(changeBlock)
}

// Обновление переменной, которую изменяет блок VARIABLE_CHANGE
fun VariableViewModel.updateChangeBlockVariable(blockId: String, variable: Variable) {
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

// Обновление знака изменения ("+" или "-") в блоке VARIABLE_CHANGE
fun VariableViewModel.updateChangeBlockSign(blockId: String, sign: String) {
    _programBlocks.value = _programBlocks.value.map { block ->
        if (block.id == blockId && block.type == BlockType.VARIABLE_CHANGE) {
            block.changeSign = sign
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
