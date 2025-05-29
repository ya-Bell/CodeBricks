package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger

// Создание управляющего блока Start или Stop
fun VariableViewModel.declareControlBlock(type: String) {
    val blockType = when (type) {
        "Start" -> BlockType.CONTROL_START
        "Stop" -> BlockType.CONTROL_STOP
        else -> BlockType.CONTROL_START
    }

    val controlBlock = Block(
        type = blockType
    )
    redrawTrigger.intValue++
    addBlock(controlBlock)
}
