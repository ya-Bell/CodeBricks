package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.VariableViewModel

// Создание одного из математических блоков: Add, Subtract, Multiply, Divide
fun VariableViewModel.declareMathBlock(type: BlockType) {
    if (type !in listOf(
            BlockType.MATH_ADD,
            BlockType.MATH_SUBTRACT,
            BlockType.MATH_MULTIPLY,
            BlockType.MATH_DIVIDE,
            BlockType.MATH_MODULO
        )
    ) return

    val block = Block(
        type = type, inputBlocks = mutableListOf(null, null)
    )

    addBlock(block)
    redrawTrigger.intValue++
}
