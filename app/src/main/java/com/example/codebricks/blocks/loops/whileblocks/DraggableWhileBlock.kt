package com.example.codebricks.blocks.loops.whileblocks

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.codebricks.R
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.ui.theme.BlockLoops
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.updateWhileBlockOperator
import com.example.codebricks.viewmodel.slot.isRecursiveInsertion
import com.example.codebricks.viewmodel.slot.setHighlightedSlot
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import com.example.codebricks.viewmodel.tree.findBlockById
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableWhileBlock(
    id: String,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    inputBlocks: MutableList<Block?> = mutableListOf(null, null),
    viewModel: VariableViewModel
) {
    while (inputBlocks.size < 2) {
        inputBlocks.add(null)
    }

    val currentBlock = viewModel.programBlocks.find { it.id == id }
    val initialOperator = currentBlock?.operator ?: "=="

    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    val selectedOperator = remember { mutableStateOf(initialOperator) }
    val scope = rememberCoroutineScope()
    val animOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var showDeleteIcon by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }
    val expandedOperator = remember { mutableStateOf(false) }

    LaunchedEffect(offset) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }

    LaunchedEffect(selectedOperator.value) {
        if (currentBlock != null) {
            viewModel.updateWhileBlockOperator(id, selectedOperator.value)
        }
    }

    LaunchedEffect(redrawTrigger) {
        layoutCoordinates?.let { coords ->
            val bounds = coords.boundsInWindow()
            BlockPositionTracker.setBlockSize(id, bounds.width, bounds.height)
            // Обновляем позиции слотов
            inputBlocks.forEachIndexed { index, _ ->
                val slotBounds = coords.boundsInWindow()
                BlockSlotTracker.setSlotBounds(id, index, slotBounds)
            }
        }
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .heightIn(min = 50.dp)
            .zIndex(0f)
            .offset {
                val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 50f)
                IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
            }
            .onGloballyPositioned { 
                layoutCoordinates = it
                val bounds = it.boundsInWindow()
                BlockPositionTracker.setBlockSize(id, bounds.width, bounds.height)
                // Обновляем позиции слотов при изменении позиции блока
                inputBlocks.forEachIndexed { index, _ ->
                    BlockSlotTracker.setSlotBounds(id, index, bounds)
                }
            }
            .background(BlockLoops, RoundedCornerShape(12.dp))
            .border(2.dp, TextBlack, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        viewModel.shouldDrawConnections.value = false
                        isPressed = true
                        dragStartTime = System.currentTimeMillis()
                        
                        val currentPosition = BlockPositionTracker.getPosition(id)
                        if (currentPosition != null) {
                            offset = currentPosition
                        }
                        
                        BlockPositionTracker.redrawTrigger.intValue++
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset = offset.plus(Offset(dragAmount.x, dragAmount.y))
                        BlockPositionTracker.updateBlockPosition(id, offset)
                        BlockPositionTracker.redrawTrigger.intValue++

                        val windowCenter = layoutCoordinates?.boundsInWindow()?.center
                        if (windowCenter != null) {
                            val matchedSlot = BlockSlotTracker.getAllSlots()
                                .filter { slot ->
                                    val slotParentBlock = viewModel.findBlockById(slot.blockId)
                                    val draggedBlock = viewModel.findBlockById(id) ?: return@filter false
                                    val isValidTarget = slotParentBlock != null &&
                                        slot.blockId != id &&
                                        !viewModel.isRecursiveInsertion(id, slot.blockId) &&
                                        when (slotParentBlock.type) {
                                            BlockType.WHILE -> false // Cannot insert while block into another while block
                                            BlockType.IF, BlockType.ELSE_IF -> draggedBlock.type in listOf(
                                                BlockType.VARIABLE_REFERENCE,
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
                                            )
                                            else -> false
                                        }
                                    isValidTarget
                                }
                                .map { it.copy(bounds = it.bounds.translate(BlockPositionTracker.canvasOffset)) }
                                .filter { it.bounds.inflate(MAGNETIC_PADDING).contains(windowCenter) }
                                .minByOrNull { it.bounds.center.minus(windowCenter).getDistance() }

                            if (matchedSlot != null) {
                                viewModel.setHighlightedSlot(matchedSlot.blockId, matchedSlot.slotIndex)
                            } else {
                                viewModel.setHighlightedSlot(null, null)
                            }
                        }
                    },
                    onDragEnd = {
                        isPressed = false
                        showDeleteIcon = false
                        val windowCenter = layoutCoordinates?.boundsInWindow()?.center
                        if (windowCenter != null) {
                            scope.launch {
                                viewModel.tryInsertIntoSlot(windowCenter, id)
                            }
                        }
                        viewModel.setHighlightedSlot(null, null)
                    }
                )
            }
    ) {
        if (showDeleteIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { onDelete(id) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.delete_block),
                    modifier = Modifier.size(12.dp),
                    tint = TextBlack
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.while_), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)

            BlockInputSlot(id, 0, inputBlocks.getOrNull(0), viewModel)

            ExposedDropdownMenuBox(
                expanded = expandedOperator.value,
                onExpandedChange = { expandedOperator.value = !expandedOperator.value }
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(40.dp)
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .background(TextWhite, RoundedCornerShape(8.dp))
                        .border(1.dp, TextBlack, RoundedCornerShape(8.dp))
                        .clickable { expandedOperator.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedOperator.value, fontSize = 16.sp, color = TextBlack)
                }

                ExposedDropdownMenu(
                    expanded = expandedOperator.value,
                    onDismissRequest = { expandedOperator.value = false },
                    modifier = Modifier.widthIn(min = 38.dp, max = 140.dp)
                ) {
                    listOf("==","!=",">", "<", ">=", "<=").forEach { operator ->
                        DropdownMenuItem(
                            text = { Text(operator, fontSize = 12.sp) },
                            modifier = Modifier.height(24.dp),
                            onClick = {
                                selectedOperator.value = operator
                                expandedOperator.value = false
                            }
                        )
                    }
                }
            }

            BlockInputSlot(id, 1, inputBlocks.getOrNull(1), viewModel)
        }
    }
} 