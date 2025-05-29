package com.example.codebricks.viewmodel

import androidx.compose.runtime.Composable
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.control.DraggableControlBlock
import com.example.codebricks.blocks.logic.DraggableElseIfBlock
import com.example.codebricks.blocks.logic.DraggableEndIfBlock
import com.example.codebricks.blocks.logic.elseblocks.DraggableElseBlock
import com.example.codebricks.blocks.logic.ifblocks.DraggableIfBlock
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
    // Проверяем, используется ли блок как input в других блоках
    fun isUsedAsInput(blockId: String): Boolean {
        return viewModel.programBlocks.any { parentBlock ->
            if (parentBlock.id == blockId) return@any false
            // Проверяем не только inputBlocks, но и все дерево блоков
            fun checkInTree(block: Block): Boolean {
                if (block.id == blockId) return true
                return block.inputBlocks.any { it?.let { checkInTree(it) } ?: false }
            }
            parentBlock.inputBlocks.any { it?.let { checkInTree(it) } ?: false }
        }
    }

    // Не рендерим блок, если он используется как input в другом блоке
    // или если это SET блок, который уже отрендерен
    if (isUsedAsInput(block.id) || 
        (block.type == BlockType.VARIABLE_SET && 
         viewModel.programBlocks.any { it.id != block.id && it.type == BlockType.VARIABLE_SET && 
                                     it.inputBlocks.any { input -> input?.id == block.id } })) {
        return
    }

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
                onDelete = onDelete,
                viewModel = viewModel
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
        BlockType.IF -> {
            DraggableIfBlock(
                id = block.id,
                inputBlocks = block.inputBlocks,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete,
                viewModel = viewModel
            )
        }

        BlockType.ELSE_IF -> {
            DraggableElseIfBlock(
                id = block.id,
                inputBlocks = block.inputBlocks,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete,
                viewModel = viewModel
            )
        }

        BlockType.ELSE -> {
            DraggableElseBlock(
                id = block.id,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete
            )
        }

        BlockType.END_IF -> {
            DraggableEndIfBlock(
                id = block.id,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                onDelete = onDelete
            )
        }
        else -> {}
    }

    block.inputBlocks.forEach { child ->
        if (child != null) {
            RenderBlockTree(child, viewModel, containerWidth, containerHeight, onDelete)
        }
    }
}