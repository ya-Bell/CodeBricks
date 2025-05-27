package com.example.codebricks.blocks.math

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.variables.DraggableReferenceBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableMathBlock(
    id: String,
    type: BlockType,
    inputBlocks: List<Block>,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    viewModel: VariableViewModel
) {
    val isInserted = viewModel.findBlockContaining(id) != null

    val scope = rememberCoroutineScope()
    val animOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val localOffset = remember { mutableStateOf(Offset.Zero) }
    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var showDeleteIcon by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    val symbol = when (type) {
        BlockType.MATH_ADD -> "+"
        BlockType.MATH_SUBTRACT -> "-"
        BlockType.MATH_MULTIPLY -> "×"
        BlockType.MATH_DIVIDE -> "÷"
        else -> "?"
    }

    val blockModifier = if (isInserted) {
        Modifier
            .wrapContentWidth()
            .heightIn(min = 40.dp)
    } else {
        Modifier
            .wrapContentWidth()
            .height(40.dp)
    }

    Box(
        modifier = blockModifier
            .offset {
                val offsetToUse = if (isInserted) localOffset.value else animOffset.value
                IntOffset(offsetToUse.x.roundToInt(), offsetToUse.y.roundToInt())
            }
            .onGloballyPositioned { coords ->
                layoutCoordinates = coords
                BlockPositionTracker.setBlockSize(
                    id, coords.size.width.toFloat(), coords.size.height.toFloat()
                )

                if (viewModel.findBlockContaining(id) != null) {
                    val parent = viewModel.findBlockContaining(id)
                    val slot = BlockSlotTracker.getAllSlots().find {
                        it.blockId == parent?.id && parent.inputBlocks.getOrNull(it.slotIndex)?.id == id
                    }
                    val windowOffset = slot?.bounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
                    localOffset.value = coords.windowToLocal(windowOffset)
                }
            }

            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFF4FC3F7))
            .then(if (!isInserted) Modifier.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (!isPressed) {
                        isPressed = true
                        dragStartTime = System.currentTimeMillis()
                    }
                    scope.launch {
                        animOffset.snapTo(animOffset.value + dragAmount)
                    }
                    BlockPositionTracker.updateBlockPosition(id, animOffset.value)
                    change.consume()
                    if (System.currentTimeMillis() - dragStartTime >= 2500) {
                        showDeleteIcon = true
                    }
                }
            } else Modifier)
            .then(if (!isInserted) Modifier.pointerInput(id) {
                detectDragGestures(onDragStart = {
                    viewModel.removeReferenceFromParent(id)
                }, onDrag = { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        animOffset.snapTo(animOffset.value + dragAmount)
                    }
                    val coords = layoutCoordinates ?: return@detectDragGestures
                    val blockW = coords.size.width.toFloat()
                    val blockH = coords.size.height.toFloat()
                    val center = animOffset.value + Offset(blockW / 2f, blockH / 2f)
                    val windowCenter = coords.localToWindow(center)

                    val matchedSlot = BlockSlotTracker.getAllSlots().find {
                        it.bounds.inflate(20f).contains(windowCenter)
                    }

                    if (matchedSlot != null) {
                        viewModel.setHighlightedSlot(
                            matchedSlot.blockId, matchedSlot.slotIndex
                        )
                    } else {
                        viewModel.setHighlightedSlot(null, null)
                    }
                }, onDragEnd = {
                    viewModel.setHighlightedSlot(null, null)
                    val coords = layoutCoordinates ?: return@detectDragGestures
                    val blockW = coords.size.width.toFloat()
                    val blockH = coords.size.height.toFloat()
                    val localCenter = animOffset.value + Offset(blockW / 2f, blockH / 2f)
                    val windowCenter = coords.localToWindow(localCenter)

                    val matchedSlot = BlockSlotTracker.getAllSlots().find {
                        it.bounds.inflate(20f).contains(windowCenter)
                    }

                    if (matchedSlot != null) {
                        val slotCenter = Offset(
                            (matchedSlot.bounds.left + matchedSlot.bounds.right) / 2f,
                            (matchedSlot.bounds.top + matchedSlot.bounds.bottom) / 2f
                        )
                        val snappedLocal = coords.windowToLocal(slotCenter) - Offset(
                            blockW / 2f, blockH / 2f
                        )
                        scope.launch {
                            viewModel.tryInsertIntoSlot(slotCenter, id)
                            viewModel.setRecentlyInsertedSlot(
                                matchedSlot.blockId, matchedSlot.slotIndex
                            )
                            animOffset.snapTo(snappedLocal)
                        }
                    }
                })
            } else Modifier)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    isPressed = false
                    showDeleteIcon = false
                })
            }, contentAlignment = Alignment.Center
    ) {
        if (showDeleteIcon) {
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable { onDelete(id) }
                .padding(4.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(12.dp),
                    tint = Color.Black
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            MathInputSlot(id, 0, inputBlocks.getOrNull(0), viewModel)

            Box(
                modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center
            ) {
                Text(
                    symbol,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 6.dp)
                )
            }

            MathInputSlot(id, 1, inputBlocks.getOrNull(1), viewModel)
        }
    }
}

@Composable
fun MathInputSlot(
    parentId: String, slotIndex: Int, block: Block?, viewModel: VariableViewModel
) {
    val isHighlighted = viewModel.highlightedSlot.value == (parentId to slotIndex)

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .wrapContentWidth()
            .heightIn(min = 32.dp)
            .defaultMinSize(minWidth = 30.dp)
            .onGloballyPositioned {
                val bounds = it.boundsInWindow()
                BlockSlotTracker.setSlotBounds(parentId, slotIndex, bounds)
            }
            .border(
                1.dp, when {
                    isHighlighted -> Color.Green
                    else -> Color.Gray
                }, RoundedCornerShape(8.dp)
            )
            .background(Color.White, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        if (block != null) {
            when (block.type) {
                BlockType.MATH_ADD, BlockType.MATH_SUBTRACT, BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE -> {
                    DraggableMathBlock(
                        id = block.id,
                        type = block.type,
                        inputBlocks = block.inputBlocks,
                        containerWidth = 0f,
                        containerHeight = 0f,
                        onDelete = { viewModel.removeBlockById(block.id) },
                        viewModel = viewModel
                    )
                }

                BlockType.VARIABLE_REFERENCE -> {
                    val variable = block.value as? Variable
                    if (variable != null) {
                        DraggableReferenceBlock(
                            id = block.id, variable = variable, viewModel = viewModel
                        )
                    } else {
                        Text(text = "?", fontSize = 12.sp)
                    }
                }

                else -> {
                    //            modifier = Modifier.padding(horizontal = 6.dp)
                    Text(text = block.value?.toString() ?: "?", fontSize = 12.sp)
                }
            }
        } else {
            Text("...", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}


@Composable
fun MathBlockPreview() {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .heightIn(min = 40.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFF4FC3F7), RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp), contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .defaultMinSize(minWidth = 45.dp)
                    .heightIn(min = 32.dp)
                    .padding(horizontal = 8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Box(
                modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center
            ) {
                Text(
                    "+",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .defaultMinSize(minWidth = 45.dp)
                    .heightIn(min = 32.dp)
                    .padding(horizontal = 8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MathBlockPreview_Show() {
    MathBlockPreview()
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun EmptyDraggableMathBlockPreview() {
    val viewModel = VariableViewModel()

    val emptyBlock = Block(
        type = BlockType.MATH_ADD, inputBlocks = mutableListOf(
            Block(type = BlockType.VARIABLE_REFERENCE), Block(type = BlockType.VARIABLE_REFERENCE)
        )
    )

    DraggableMathBlock(
        id = emptyBlock.id,
        type = BlockType.MATH_ADD,
        inputBlocks = emptyBlock.inputBlocks,
        containerWidth = 400f,
        containerHeight = 400f,
        onDelete = {},
        viewModel = viewModel
    )
}