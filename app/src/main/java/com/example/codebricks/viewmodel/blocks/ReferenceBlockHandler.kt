package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.tree.findBlockContaining

// Добавление блока-ссылки на переменную напрямую на канвас
fun VariableViewModel.addReferenceBlock(variable: Variable) {
    println("Added insert block: ${variable.name}")
    val block = Block(
        type = BlockType.VARIABLE_REFERENCE, value = variable
    )
    redrawTrigger.intValue++
    _programBlocks.value += block
}

// Удаляет оторванные ссылки на переменные, не подключённые к дереву
fun VariableViewModel.cleanupUnattachedReferenceBlocks() {
    val attachedIds = mutableSetOf<String>()

    fun collect(block: Block) {
        if (attachedIds.add(block.id)) {
            block.inputBlocks.filterNotNull().forEach(::collect)
            block.children.forEach(::collect)
        }
    }

    programBlocks.filter { findBlockContaining(it.id) == null }.forEach(::collect)

    val before = _programBlocks.value.size
    _programBlocks.value = _programBlocks.value.filter { it.id in attachedIds }
    val after = _programBlocks.value.size

    println("Cleaned: removed ${before - after} detached blocks")
}
