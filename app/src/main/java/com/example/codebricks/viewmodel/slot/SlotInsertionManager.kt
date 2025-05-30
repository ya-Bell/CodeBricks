package com.example.codebricks.viewmodel.slot

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.findBlockRecursivelyById
import com.example.codebricks.viewmodel.tree.removeBlockRecursively
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Вставляет блок в слот, если попадает в магнитную зону
fun VariableViewModel.tryInsertIntoSlot(position: Offset, blockId: String) {
    val draggedBlock = programBlocks.find { it.id == blockId } ?: return

    // Получаем все подходящие слоты в точке вставки
    val matchingSlots = BlockSlotTracker.getAllSlots()
        .filter { slot ->
            val slotParentBlock = findBlockById(slot.blockId)
            val isValidTarget = slotParentBlock != null &&
                !isRecursiveInsertion(blockId, slot.blockId) &&
                slot.blockId != blockId &&
                when (slotParentBlock.type) {
                    BlockType.IF, BlockType.ELSE_IF -> draggedBlock.type in listOf(
                        BlockType.VARIABLE_REFERENCE,
                        BlockType.MATH_ADD,
                        BlockType.MATH_SUBTRACT,
                        BlockType.MATH_MULTIPLY,
                        BlockType.MATH_DIVIDE,
                        BlockType.MATH_MODULO,
                        BlockType.COMPARISON_EQUAL,
                        BlockType.COMPARISON_GREATER,
                        BlockType.COMPARISON_LESS,
                        BlockType.LOGIC_AND,
                        BlockType.LOGIC_OR,
                        BlockType.LOGIC_NOT
                    )
                    BlockType.VARIABLE_SET -> draggedBlock.type in listOf(
                        BlockType.VARIABLE_REFERENCE,
                        BlockType.MATH_ADD,
                        BlockType.MATH_SUBTRACT,
                        BlockType.MATH_MULTIPLY,
                        BlockType.MATH_DIVIDE,
                        BlockType.MATH_MODULO
                    )
                    BlockType.MATH_ADD,
                    BlockType.MATH_SUBTRACT,
                    BlockType.MATH_MULTIPLY,
                    BlockType.MATH_DIVIDE,
                    BlockType.MATH_MODULO-> draggedBlock.type in listOf(
                        BlockType.VARIABLE_REFERENCE,
                        BlockType.MATH_ADD,
                        BlockType.MATH_SUBTRACT,
                        BlockType.MATH_MULTIPLY,
                        BlockType.MATH_DIVIDE,
                        BlockType.MATH_MODULO
                    )
                    BlockType.COMPARISON_EQUAL,
                    BlockType.COMPARISON_GREATER,
                    BlockType.COMPARISON_LESS -> draggedBlock.type in listOf(
                        BlockType.VARIABLE_REFERENCE,
                        BlockType.MATH_ADD,
                        BlockType.MATH_SUBTRACT,
                        BlockType.MATH_MULTIPLY,
                        BlockType.MATH_DIVIDE,
                        BlockType.MATH_MODULO
                    )
                    BlockType.LOGIC_AND,
                    BlockType.LOGIC_OR -> draggedBlock.type in listOf(
                        BlockType.COMPARISON_EQUAL,
                        BlockType.COMPARISON_GREATER,
                        BlockType.COMPARISON_LESS,
                        BlockType.LOGIC_AND,
                        BlockType.LOGIC_OR,
                        BlockType.LOGIC_NOT
                    )
                    BlockType.LOGIC_NOT -> draggedBlock.type in listOf(
                        BlockType.COMPARISON_EQUAL,
                        BlockType.COMPARISON_GREATER,
                        BlockType.COMPARISON_LESS,
                        BlockType.LOGIC_AND,
                        BlockType.LOGIC_OR,
                        BlockType.LOGIC_NOT
                    )
                    else -> false
                } &&
                // Проверяем, что целевой блок не является частью перетаскиваемого блока
                !isBlockInDraggedTree(slot.blockId, blockId)
            
            isValidTarget && slot.bounds.inflate(MAGNETIC_PADDING).contains(position)
        }
    
    if (matchingSlots.isEmpty()) return

    // Находим самый верхний блок среди всех подходящих слотов
    val topSlot = findTopMostSlot(matchingSlots)
    if (topSlot == null) return

    // Найти целевой блок
    val targetBlock = findBlockRecursivelyById(topSlot.blockId, programBlocks) ?: return
    val slotIndex = topSlot.slotIndex

    // Удаляем все копии блока в дереве
    removeAllInstancesOfBlock(programBlocks, blockId)

    // Подготавливаем слот для вставки
    while (targetBlock.inputBlocks.size <= slotIndex) {
        targetBlock.inputBlocks.add(null)
    }

    // Очищаем старый блок из слота, если он там был
    val oldBlock = targetBlock.inputBlocks[slotIndex]
    if (oldBlock != null) {
        removeBlockRecursively(oldBlock.id)
    }

    // Вставляем блок в слот
    targetBlock.inputBlocks[slotIndex] = draggedBlock

    // Обновляем список блоков
    val updatedBlocks = _programBlocks.value.toMutableList()
    updatedBlocks.removeAll { it.id == blockId }
    _programBlocks.value = updatedBlocks

    println("✅ Inserted into $slotIndex of ${targetBlock.type} (${targetBlock.id})")
    redrawTrigger.intValue++
}

// Находит и очищает все копии блока в дереве
fun VariableViewModel.removeAllInstancesOfBlock(blocks: List<Block>, targetId: String) {
    val processedBlocks = mutableSetOf<String>()
    
    fun removeRecursively(block: Block) {
        if (!processedBlocks.add(block.id)) return
        
        // Очищаем слоты текущего блока
        for (i in block.inputBlocks.indices) {
            val inputBlock = block.inputBlocks[i]
            if (inputBlock?.id == targetId) {
                block.inputBlocks[i] = null
            } else if (inputBlock != null) {
                removeRecursively(inputBlock)
            }
        }
        
        // Проверяем детей
        block.children.forEach { child ->
            removeRecursively(child)
        }
    }
    
    blocks.forEach { block ->
        removeRecursively(block)
    }
}

// Находит самый верхний слот среди перекрывающихся слотов
private fun VariableViewModel.findTopMostSlot(slots: List<BlockSlotTracker.SlotInfo>): BlockSlotTracker.SlotInfo? {
    if (slots.isEmpty()) return null
    
    // Сортируем слоты по времени создания блоков (более новые блоки находятся выше)
    return slots.maxByOrNull { slot ->
        val block = findBlockRecursivelyById(slot.blockId, programBlocks)
        block?.creationTime ?: 0L
    }
}

// Вспомогательная функция для поиска блока в дереве
private fun isBlockInTree(root: Block, searchId: String): Boolean {
    if (root.id == searchId) return true
    return root.inputBlocks.any { it != null && isBlockInTree(it, searchId) }
}

// Находит родительский блок для заданного blockId
private fun VariableViewModel.findParentBlock(blockId: String): Block? {
    return programBlocks.find { block ->
        block.inputBlocks.any { it?.id == blockId } ||
        block.inputBlocks.any { input -> input != null && isBlockInTree(input, blockId) }
    }
}

// Улучшенная проверка на рекурсивную вставку
fun VariableViewModel.isRecursiveInsertion(childId: String, targetId: String): Boolean {
    if (childId == targetId) return true

    // Находим все блоки в дереве перетаскиваемого блока
    fun collectIds(blockId: String, collected: MutableSet<String>) {
        collected.add(blockId)
        findBlockById(blockId)?.let { block ->
            block.inputBlocks.forEach { input ->
                if (input != null) {
                    collectIds(input.id, collected)
                }
            }
        }
    }

    // Находим все блоки в целевом дереве
    fun collectTargetIds(blockId: String, collected: MutableSet<String>) {
        collected.add(blockId)
        findBlockById(blockId)?.let { block ->
            block.inputBlocks.forEach { input ->
                if (input != null) {
                    collectTargetIds(input.id, collected)
                }
            }
        }
    }

    val childTreeIds = mutableSetOf<String>()
    val targetTreeIds = mutableSetOf<String>()
    
    collectIds(childId, childTreeIds)
    collectTargetIds(targetId, targetTreeIds)

    // Проверяем пересечение деревьев
    return childTreeIds.intersect(targetTreeIds).isNotEmpty()
}

// Заменяет блок в указанном слоте на новый
fun VariableViewModel.replaceSlotBlock(parentId: String, slotIndex: Int, newBlock: Block) {
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

// Устанавливает визуально подсвеченный слот
fun VariableViewModel.setHighlightedSlot(blockId: String?, slotIndex: Int?) {
    println("🎯 Setting highlighted slot: $blockId to $slotIndex (previous: ${highlightedSlot.value})")
    highlightedSlot.value = if (blockId != null && slotIndex != null) blockId to slotIndex else null
    // Вызываем перерисовку для обновления подсветки
    BlockPositionTracker.redrawTrigger.intValue++
}

// Отмечает слот, в который только что вставили блок (для анимации/эффекта)
fun VariableViewModel.setRecentlyInsertedSlot(blockId: String, slotIndex: Int) {
    recentlyInsertedSlot.value = blockId to slotIndex
    viewModelScope.launch {
        delay(300)
        recentlyInsertedSlot.value = null
    }
}

// Проверяет, является ли блок частью дерева перетаскиваемого блока
private fun VariableViewModel.isBlockInDraggedTree(blockId: String, draggedBlockId: String): Boolean {
    val draggedBlock = findBlockById(draggedBlockId) ?: return false
    
    fun checkRecursively(block: Block): Boolean {
        if (block.id == blockId) return true
        return block.inputBlocks.any { it != null && checkRecursively(it) }
    }
    
    return checkRecursively(draggedBlock)
}