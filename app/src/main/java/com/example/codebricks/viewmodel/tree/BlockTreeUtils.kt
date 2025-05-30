package com.example.codebricks.viewmodel.tree

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger

// Удаляет блок и всех его потомков из программы
fun VariableViewModel.removeBlockRecursively(blockId: String) {
    val toRemove = collectDescendantIds(findBlockById(blockId) ?: return)
    _programBlocks.value = _programBlocks.value.filterNot { it.id in toRemove }
    redrawTrigger.intValue++
}

// Рекурсивно удаляет блок из inputBlocks и children всех блоков
fun VariableViewModel.removeBlockFromParent(childId: String) {
    // Сначала находим блок, который нужно удалить
    val blockToRemove = findBlockById(childId)
    if (blockToRemove == null) return

    if (!_programBlocks.value.any { it.id == childId }) {
        _programBlocks.value = _programBlocks.value + blockToRemove
    }

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
