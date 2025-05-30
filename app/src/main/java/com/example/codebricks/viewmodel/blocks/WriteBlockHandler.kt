package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger

fun VariableViewModel.declareWriteBlock(variable: Variable) {
    val block = Block(
        type = BlockType.IO_WRITE,
        value = variable
    )

    addBlock(block)
    redrawTrigger.intValue++
} 