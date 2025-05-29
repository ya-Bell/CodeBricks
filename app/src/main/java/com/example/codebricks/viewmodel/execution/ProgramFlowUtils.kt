package com.example.codebricks.viewmodel.execution

import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.VariableViewModel.BlockOrderResult

// Устанавливает nextBlockId между блоками в зависимости от их Y-положения
fun VariableViewModel.linkBlocksByPosition() {
    val positionedBlocks = programBlocks.mapNotNull { block ->
        val position = BlockPositionTracker.getRawPosition(block.id)
        position?.let { block to it }
    }

    // Сортируем блоки по Y-координате, используя необработанные позиции
    val sortedBlocks = positionedBlocks.sortedBy { (_, position) ->
        position.y
    }

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

// Проверка порядка и корректности блоков: Start первый, переменные объявлены до использования
fun VariableViewModel.checkBlockOrder(): BlockOrderResult {
    val declared = mutableSetOf<String>()

    val orderedBlocks = programBlocks.mapNotNull { block ->
        val position = BlockPositionTracker.getRawPosition(block.id)
        position?.let { block to it }
    }.sortedBy { (_, position) ->
        position.y
    }.map { it.first }

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
