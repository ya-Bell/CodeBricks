package com.example.codebricks.blocks.loops.whileblocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.codebricks.R
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.math.DraggableMathBlock
import com.example.codebricks.blocks.variables.varreference.DraggableReferenceBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.ui.theme.BlockHighlighted
import com.example.codebricks.ui.theme.BlockNormal
import com.example.codebricks.ui.theme.BlockSlotHighlighted
import com.example.codebricks.ui.theme.BlockSuccess
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextGray
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.slot.setRecentlyInsertedSlot
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.removeBlockRecursively

@Composable
fun BlockInputSlot(
    parentId: String,
    slotIndex: Int,
    block: Block?,
    viewModel: VariableViewModel
) {
    val isHighlighted = viewModel.highlightedSlot.value == (parentId to slotIndex)
    val isRecentlyInserted = viewModel.recentlyInsertedSlot.value == (parentId to slotIndex)
    var showInput by remember { mutableStateOf(block == null) }
    var inputValue by remember { mutableStateOf("") }
    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    var layoutCoordinates by remember {
        mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(
            null
        )
    }

    LaunchedEffect(redrawTrigger) {
        layoutCoordinates?.let {
            val bounds = it.boundsInWindow()
            BlockSlotTracker.setSlotBounds(parentId, slotIndex, bounds)
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .wrapContentWidth()
            .heightIn(min = 32.dp)
            .defaultMinSize(minWidth = 30.dp)
            .zIndex(1f)
            .onGloballyPositioned {
                layoutCoordinates = it
                val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
                BlockSlotTracker.setSlotBounds(parentId, slotIndex, bounds)
                BlockPositionTracker.redrawTrigger.intValue++
            }
            .border(
                width = if (isRecentlyInserted) 2.dp else 1.dp,
                color = when {
                    isRecentlyInserted -> BlockSuccess
                    isHighlighted -> BlockHighlighted
                    else -> TextGray
                },
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isHighlighted) BlockSlotHighlighted else BlockNormal,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            block != null -> {
                when (block.type) {
                    BlockType.VARIABLE_REFERENCE -> {
                        val variable = block.value as? Variable
                        if (variable != null) {
                            DraggableReferenceBlock(
                                id = block.id,
                                variable = variable,
                                viewModel = viewModel,
                                onDelete = {
                                    if (variable.name.toDoubleOrNull() != null) {
                                        viewModel.removeBlockRecursively(block.id)
                                    }
                                    viewModel.findBlockById(parentId)?.let { parentBlock ->
                                        if (parentBlock.inputBlocks.size > slotIndex) {
                                            parentBlock.inputBlocks[slotIndex] = null
                                        }
                                    }
                                    BlockPositionTracker.redrawTrigger.intValue++
                                    showInput = true
                                }
                            )
                        }
                    }

                    in listOf(
                        BlockType.MATH_ADD,
                        BlockType.MATH_SUBTRACT,
                        BlockType.MATH_MULTIPLY,
                        BlockType.MATH_DIVIDE,
                        BlockType.COMPARISON_EQUAL,
                        BlockType.COMPARISON_GREATER,
                        BlockType.COMPARISON_LESS,
                        BlockType.LOGIC_AND,
                        BlockType.LOGIC_OR,
                        BlockType.LOGIC_NOT
                    ) -> {
                        DraggableMathBlock(
                            id = block.id,
                            type = block.type,
                            inputBlocks = block.inputBlocks,
                            containerWidth = 0f,
                            containerHeight = 0f,
                            onDelete = {
                                viewModel.findBlockById(parentId)?.let { parentBlock ->
                                    if (parentBlock.inputBlocks.size > slotIndex) {
                                        parentBlock.inputBlocks[slotIndex] = null
                                    }
                                }
                                BlockPositionTracker.redrawTrigger.intValue++
                                showInput = true
                            },
                            viewModel = viewModel
                        )
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .width(32.dp)
                                .height(24.dp)
                                .clickable { showInput = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.points),
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            else -> {
                BasicTextField(
                    value = inputValue,
                    onValueChange = { value ->
                        inputValue = value
                    },
                    textStyle = TextStyle(
                        fontSize = 12.sp,
                        color = TextBlack
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputValue.isNotEmpty()) {
                                val variable =
                                    Variable(name = inputValue, value = inputValue, type = "string")
                                val newBlock = Block(
                                    type = BlockType.VARIABLE_REFERENCE,
                                    value = variable
                                )
                                viewModel.findBlockById(parentId)?.let { parentBlock ->
                                    if (parentBlock.inputBlocks.size > slotIndex) {
                                        parentBlock.inputBlocks[slotIndex] = newBlock
                                    } else {
                                        parentBlock.inputBlocks.add(newBlock)
                                    }
                                    viewModel.setRecentlyInsertedSlot(parentId, slotIndex)
                                    BlockPositionTracker.redrawTrigger.intValue++
                                }
                            }
                            showInput = false
                            inputValue = ""
                        }
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(IntrinsicSize.Min)
                        .focusRequester(focusRequester)
                        .focusable()
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
} 