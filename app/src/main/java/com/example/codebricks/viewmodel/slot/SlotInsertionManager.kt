package com.example.codebricks.viewmodel.slot

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.tree.findBlockRecursivelyById
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.removeBlockRecursively
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Вставляет блок в слот, если попадает в магнитную зону
fun VariableViewModel.tryInsertIntoSlot(position: Offset, blockId: String) {
    val draggedBlock = programBlocks.find { it.id == blockId } ?: return

    val matchedSlot = BlockSlotTracker.getAllSlots()
        .filter { it.bounds.inflate(MAGNETIC_PADDING).contains(position) }
        .minByOrNull { it.bounds.width * it.bounds.height } ?: return

    if (blockId == matchedSlot.blockId) {
        println("⚠️ Warning: inserting into self")
        return
    }

    // найти любой блок (на любой глубине) по matchedSlot.blockId
    val targetBlock = findBlockRecursivelyById(matchedSlot.blockId, programBlocks) ?: return

    val slotIndex = matchedSlot.slotIndex

    if (targetBlock.type !in listOf(
            BlockType.VARIABLE_SET,
            BlockType.MATH_ADD,
            BlockType.MATH_SUBTRACT,
            BlockType.MATH_MULTIPLY,
            BlockType.MATH_DIVIDE
        )
    ) {
        println("🚫 Target block doesn't support insertion")
        return
    }

    if (isRecursiveInsertion(blockId, targetBlock.id)) {
        println("🚫 Recursive insertion detected")
        return
    }

    while (targetBlock.inputBlocks.size <= slotIndex) {
        targetBlock.inputBlocks.add(null)
    }

    targetBlock.inputBlocks[slotIndex] = draggedBlock
    _programBlocks.value = _programBlocks.value.filterNot { it.id == draggedBlock.id }
    println("✅ Inserted into $slotIndex of ${targetBlock.type} (${targetBlock.id})")

    _programBlocks.value = _programBlocks.value.toList()
    redrawTrigger.intValue++
}

// Проверка на рекурсивную вставку (вставляешь блок внутрь самого себя)
fun VariableViewModel.isRecursiveInsertion(childId: String, targetId: String): Boolean {
    if (childId == targetId) return true

    val draggedBlock = findBlockRecursivelyById(childId, programBlocks) ?: return false

    fun containsRecursively(block: Block): Boolean {
        if (block.id == targetId) return true
        return block.inputBlocks.any { it != null && containsRecursively(it) } ||
                block.children.any { containsRecursively(it) }
    }

    return containsRecursively(draggedBlock)
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
    highlightedSlot.value =
        if (blockId != null && slotIndex != null) blockId to slotIndex else null
}

// Отмечает слот, в который только что вставили блок (для анимации/эффекта)
fun VariableViewModel.setRecentlyInsertedSlot(blockId: String, slotIndex: Int) {
    recentlyInsertedSlot.value = blockId to slotIndex
    viewModelScope.launch {
        delay(300)
        recentlyInsertedSlot.value = null
    }
}