package com.example.codebricks.viewmodel

import androidx.compose.runtime.Composable
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.control.DraggableControlBlock
import com.example.codebricks.blocks.math.DraggableMathBlock
import com.example.codebricks.blocks.print.DraggablePrintBlock
import com.example.codebricks.blocks.variables.varchangeby.DraggableChangeVariableBlock
import com.example.codebricks.blocks.variables.vardeclare.DraggableDeclareBlock
import com.example.codebricks.blocks.variables.varreference.DraggableReferenceBlock
import com.example.codebricks.blocks.variables.varset.DraggableSetVariableBlock

@Composable
fun RenderBlockTree(
    block: Block,
    viewModel: VariableViewModel,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit
) {

    when (block.type) {
        BlockType.MATH_ADD, BlockType.MATH_SUBTRACT, BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE -> {
            DraggableMathBlock(
                id = block.id,
                type = block.type,
                inputBlocks = block.inputBlocks,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete,
                viewModel = viewModel
            )
        }
        BlockType.VARIABLE_SET -> {
            DraggableSetVariableBlock(
                id = block.id,
                inputBlocks = block.inputBlocks,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete,
                viewModel = viewModel
            )
        }
        BlockType.VARIABLE_CHANGE -> {
            val variable = block.inputBlocks.getOrNull(0)?.value as? Variable
            DraggableChangeVariableBlock(
                id = block.id,
                variable = variable,
                changeSign = block.changeSign,
                changeAmount = block.changeAmount,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete,
                viewModel = viewModel
            )
        }
        BlockType.IO_PRINT -> {
            val variable = block.inputBlocks.firstOrNull()?.value as? Variable
            DraggablePrintBlock(
                id = block.id,
                variable = variable,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete
            )
        }
        BlockType.CONTROL_START, BlockType.CONTROL_STOP -> {
            DraggableControlBlock(
                id = block.id,
                type = block.type.name,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete
            )
        }
        BlockType.VARIABLE_DECLARE -> {
            val variable = block.value as? Variable
            if (variable != null) {
                DraggableDeclareBlock(
                    id = block.id,
                    variable = variable,
                    containerWidth = containerWidth,
                    containerHeight = containerHeight,
                    onDelete = onDelete
                )
            }
        }
        BlockType.VARIABLE_REFERENCE -> {
            val variable = block.value as? Variable
            if (variable != null) {
                DraggableReferenceBlock(
                    id = block.id,
                    variable = variable,
                    viewModel = viewModel
                )
            }
        }
        else -> {}
    }

    block.inputBlocks.forEach { child ->
        if (child != null) {
            RenderBlockTree(child, viewModel, containerWidth, containerHeight, onDelete)
        }
    }
}