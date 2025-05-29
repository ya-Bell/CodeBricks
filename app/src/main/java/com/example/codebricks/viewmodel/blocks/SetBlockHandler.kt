package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger

// Создание пустого блока Set(variable, value)
fun VariableViewModel.declareEmptySetVariableBlock() {
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

// Обновление переменной, в которую производится установка значения
fun VariableViewModel.updateSetBlockTarget(blockId: String, variable: Variable) {
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


// Обновление значения, которое присваивается переменной в блоке Set
fun VariableViewModel.updateSetBlockValue(blockId: String, rawValue: String) {
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
