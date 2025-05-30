package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.VariableViewModel

fun VariableViewModel.declareWhileBlock(operator: String = "==", variable: String = "score") {
    val block = Block(
        type = BlockType.WHILE,
        operator = operator,
        variable = variable,
        inputBlocks = mutableListOf(null, null)
    )

    addBlock(block)
    redrawTrigger.intValue++
}

fun VariableViewModel.declareWhileEndBlock() {
    val block = Block(
        type = BlockType.WHILE_END,
        inputBlocks = mutableListOf()
    )

    addBlock(block)
    redrawTrigger.intValue++
}

fun VariableViewModel.updateWhileBlockOperator(blockId: String, operator: String) {
    _programBlocks.value = _programBlocks.value.map { block ->
        if (block.id == blockId && block.type == BlockType.WHILE) {
            block.copy(operator = operator)
        } else {
            block
        }
    }
}