package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.VariableViewModel

fun VariableViewModel.declareIfBlock(operator: String = "==", variable: String = "score") {
    val block = Block(
        type = BlockType.IF,
        operator = operator,
        variable = variable,
        inputBlocks = mutableListOf(
            Block(type = BlockType.VARIABLE_REFERENCE),
            Block(type = BlockType.VARIABLE_REFERENCE)
        )
    )

    addBlock(block)
    redrawTrigger.intValue++
}

fun VariableViewModel.declareElseIfBlock(operator: String = "==", variable: String = "score") {
    val block = Block(
        type = BlockType.ELSE_IF,
        operator = operator,
        variable = variable,
        inputBlocks = mutableListOf(
            Block(type = BlockType.VARIABLE_REFERENCE),
            Block(type = BlockType.VARIABLE_REFERENCE)
        )
    )

    addBlock(block)
    redrawTrigger.intValue++
}

fun VariableViewModel.declareElseBlock() {
    val block = Block(
        type = BlockType.ELSE,
        inputBlocks = mutableListOf()
    )

    addBlock(block)
    redrawTrigger.intValue++
}

fun VariableViewModel.declareEndIfBlock() {
    val block = Block(
        type = BlockType.END_IF,
        inputBlocks = mutableListOf()
    )

    addBlock(block)
    redrawTrigger.intValue++
}

fun VariableViewModel.updateIfBlockOperator(blockId: String, operator: String) {
    _programBlocks.value = _programBlocks.value.map { block ->
        if (block.id == blockId && block.type == BlockType.IF) {
            block.copy(operator = operator)
        } else {
            block
        }
    }
}

fun VariableViewModel.updateElseIfBlockOperator(blockId: String, operator: String) {
    _programBlocks.value = _programBlocks.value.map { block ->
        if (block.id == blockId && block.type == BlockType.ELSE_IF) {
            block.copy(operator = operator)
        } else {
            block
        }
    }
}