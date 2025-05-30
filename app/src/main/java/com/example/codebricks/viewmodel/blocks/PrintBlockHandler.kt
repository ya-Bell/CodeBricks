package com.example.codebricks.viewmodel.blocks

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger

// Создание блока Print(variable), который выводит значение переменной в консоль
fun VariableViewModel.declarePrintBlock(variable: Variable) {
    val referenceBlock = Block(
        type = BlockType.VARIABLE_REFERENCE, value = variable
    )

    val printBlock = Block(
        type = BlockType.IO_PRINT, inputBlocks = mutableListOf(referenceBlock)
    )

    addBlock(printBlock)
    redrawTrigger.intValue++
}

// Создание блока Print(text), который выводит текст в консоль
fun VariableViewModel.declarePrintTextBlock(text: String) {
    val printBlock = Block(
        type = BlockType.IO_PRINT,
        value = text // Используем value для хранения текста
    )

    addBlock(printBlock)
    redrawTrigger.intValue++
}
