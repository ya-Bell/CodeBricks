package com.example.codebricks.blocks.variables.varreference

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.slot.isRecursiveInsertion
import com.example.codebricks.viewmodel.slot.setHighlightedSlot
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.findBlockContaining
import com.example.codebricks.viewmodel.tree.removeBlockFromParent
import com.example.codebricks.viewmodel.tree.removeBlockRecursively
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableReferenceBlock(
    id: String,
    variable: Variable,
    viewModel: VariableViewModel,
    onDelete: (String) -> Unit = {}
) {
    val currentId = remember { mutableStateOf(id) }
    val scope = rememberCoroutineScope()
    val animOffset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue
    val localOffset = remember { mutableStateOf(Offset.Zero) }

    val isInserted by remember(currentId.value, viewModel.programBlocks) {
        mutableStateOf(viewModel.findBlockContaining(currentId.value) != null)
    }

    // Проверяем, является ли это числовым блоком
    val isNumericBlock = remember(variable.name) {
        variable.name.toDoubleOrNull() != null
    }

    val blockModifier = if (isInserted) {
        Modifier.wrapContentWidth().heightIn(min = 32.dp)
    } else {
        Modifier.wrapContentWidth().height(32.dp)
    }

    LaunchedEffect(currentId.value, isInserted, redrawTrigger) {
        if (isInserted && layoutCoordinates != null) {
            val parent = viewModel.findBlockContaining(currentId.value)
            val slot = BlockSlotTracker.getAllSlots().find {
                it.blockId == parent?.id && parent.inputBlocks.getOrNull(it.slotIndex)?.id == currentId.value
            }
            val windowOffset = slot?.bounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
            val correctedOffset = layoutCoordinates!!.windowToLocal(windowOffset - BlockPositionTracker.canvasOffset)
            if (!correctedOffset.x.isNaN() && !correctedOffset.y.isNaN() && correctedOffset != localOffset.value) {
                localOffset.value = correctedOffset
            }
        }
    }

    Box(
        modifier = blockModifier
            .then(
                if (isInserted) Modifier else Modifier.offset {
                    IntOffset(animOffset.value.x.roundToInt(), animOffset.value.y.roundToInt())
                }
            )
            .onGloballyPositioned { coords ->
                layoutCoordinates = coords
                BlockPositionTracker.setBlockSize(currentId.value, coords.size.width.toFloat(), coords.size.height.toFloat())

                if (isInserted) {
                    val parent = viewModel.findBlockContaining(currentId.value)
                    val slot = BlockSlotTracker.getAllSlots().find {
                        it.blockId == parent?.id && parent.inputBlocks.getOrNull(it.slotIndex)?.id == currentId.value
                    }
                    val windowOffset = slot?.bounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
                    localOffset.value = coords.windowToLocal(windowOffset)
                }
            }
            .clip(RoundedCornerShape(8.dp))
            .border(width = if (isInserted) 0.dp else 2.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
            .background(Color(0xFFEEEEEE))
            .zIndex(if (isInserted) 0f else 1f)
            .pointerInput(currentId.value, isInserted) {
                detectDragGestures(
                    onDragStart = {
                        viewModel.shouldDrawConnections.value = false
                        viewModel.findBlockContaining(id)?.id?.let { viewModel.bringBlockToFront(it) }
                        if (isInserted) {
                            val original = viewModel.findBlockById(currentId.value)
                            val parentBlock = viewModel.findBlockContaining(currentId.value)
                            
                            // Если это числовой блок, удаляем его при начале перетаскивания
                            if (isNumericBlock) {
                                viewModel.removeBlockRecursively(currentId.value)
                                if (parentBlock != null) {
                                    val slotIndex = parentBlock.inputBlocks.indexOfFirst { it?.id == currentId.value }
                                    if (slotIndex != -1) {
                                        parentBlock.inputBlocks[slotIndex] = null
                                    }
                                }
                                return@detectDragGestures
                            }

                            viewModel.removeBlockFromParent(currentId.value)
                            
                            if (parentBlock != null) {
                                val slotIndex = parentBlock.inputBlocks.indexOfFirst { it?.id == currentId.value }
                                if (slotIndex != -1) {
                                    parentBlock.inputBlocks[slotIndex] = null
                                }
                            }
                            
                            if (original != null) {
                                layoutCoordinates?.boundsInWindow()?.topLeft?.let { windowPos ->
                                    val canvasOffset = windowPos + BlockPositionTracker.canvasOffset
                                    scope.launch {
                                        animOffset.snapTo(canvasOffset)
                                        BlockPositionTracker.updateBlockPosition(original.id, canvasOffset)
                                        BlockPositionTracker.redrawTrigger.intValue++
                                    }
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (isNumericBlock && isInserted) return@detectDragGestures

                        change.consume()
                        scope.launch {
                            animOffset.snapTo(animOffset.value + dragAmount)
                        }
                        val coords = layoutCoordinates ?: return@detectDragGestures
                        val windowCenter = coords.boundsInWindow().center

                        val matchedSlot = BlockSlotTracker.getAllSlots()
                            .filter { slot ->
                                val slotParentBlock = viewModel.findBlockById(slot.blockId)
                                slotParentBlock != null &&
                                slot.blockId != currentId.value &&
                                !viewModel.isRecursiveInsertion(currentId.value, slot.blockId) &&
                                slotParentBlock.type in listOf(
                                    BlockType.VARIABLE_SET,
                                    BlockType.MATH_ADD,
                                    BlockType.MATH_SUBTRACT,
                                    BlockType.MATH_MULTIPLY,
                                    BlockType.MATH_DIVIDE,
                                    BlockType.IF,
                                    BlockType.ELSE_IF,
                                    BlockType.COMPARISON_EQUAL,
                                    BlockType.COMPARISON_GREATER,
                                    BlockType.COMPARISON_LESS,
                                    BlockType.LOGIC_AND,
                                    BlockType.LOGIC_OR,
                                    BlockType.LOGIC_NOT
                                )
                            }
                            .map { it.copy(bounds = it.bounds.translate(BlockPositionTracker.canvasOffset)) }
                            .filter { it.bounds.inflate(MAGNETIC_PADDING).contains(windowCenter) }
                            .minByOrNull { it.bounds.width * it.bounds.height }

                        if (matchedSlot != null) {
                            viewModel.setHighlightedSlot(matchedSlot.blockId, matchedSlot.slotIndex)
                        } else {
                            viewModel.setHighlightedSlot(null, null)
                        }
                    },
                    onDragEnd = {
                        if (isNumericBlock && isInserted) return@detectDragGestures

                        viewModel.setHighlightedSlot(null, null)
                        val coords = layoutCoordinates ?: return@detectDragGestures
                        val windowCenter = coords.boundsInWindow().center

                        scope.launch {
                            viewModel.tryInsertIntoSlot(windowCenter, currentId.value)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = variable.name,
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DraggableReferenceBlockPreview() {
    val mockViewModel = remember { VariableViewModel() }
    val variable = Variable(name = "e", value = 10, type = "int")

    DraggableReferenceBlock(
        id = "ref-preview-id", variable = variable, viewModel = mockViewModel
    )
}
