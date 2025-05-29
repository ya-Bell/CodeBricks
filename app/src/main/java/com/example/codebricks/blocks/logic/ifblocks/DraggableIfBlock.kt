package com.example.codebricks.blocks.logic.ifblocks

import android.R.attr.id
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.blocks.math.DraggableMathBlock
import com.example.codebricks.blocks.variables.varreference.DraggableReferenceBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.updateIfBlockOperator
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableIfBlock(
    id: String,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    inputBlocks: MutableList<Block?> = mutableListOf(),
    viewModel: VariableViewModel
) {
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
            viewModel.updateIfBlockOperator(id, selectedOperator.value)
        }
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .heightIn(min = 50.dp)
            .offset {

                val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 50f)
                IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
            }
            .background(Color(0xFF81C784), RoundedCornerShape(12.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (!isPressed) {
                        isPressed = true
                        dragStartTime = System.currentTimeMillis()
                    }

                    offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)

                    BlockPositionTracker.updateBlockPosition(id, offset)

                    change.consume()

                    if (System.currentTimeMillis() - dragStartTime >= 2500) {
                        showDeleteIcon = true
                    }

                }
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
                    contentDescription = "Delete Block",
                    modifier = Modifier.size(12.dp),
                    tint = Color.Black
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("if", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

            BlockInputSlot(id, 0, inputBlocks.getOrNull(0), viewModel)

            ExposedDropdownMenuBox(
                expanded = expandedOperator.value,
                onExpandedChange = { expandedOperator.value = !expandedOperator.value }
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(20.dp)
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clickable { expandedOperator.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedOperator.value, fontSize = 16.sp, color = Color.Black)
                }

                ExposedDropdownMenu(
                    expanded = expandedOperator.value,
                    onDismissRequest = { expandedOperator.value = false },
                    modifier = Modifier.widthIn(min = 38.dp, max = 140.dp)
                ) {
                    listOf("==", ">", "<", ">=", "<=").forEach { operator ->
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

@Composable
fun BlockInputSlot(
    parentId: String,
    slotIndex: Int,
    block: Block?,
    viewModel: VariableViewModel
) {
    val isHighlighted = viewModel.highlightedSlot.value == (parentId to slotIndex)

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .wrapContentWidth()
            .heightIn(min = 32.dp)
            .defaultMinSize(minWidth = 30.dp)
            .onGloballyPositioned {
                val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
                BlockSlotTracker.setSlotBounds(parentId, slotIndex, bounds)
            }
            .border(
                1.dp,
                if (isHighlighted) Color.Green else Color.Gray,
                RoundedCornerShape(8.dp)
            )
            .background(Color.White, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            block == null -> Text("...", color = Color.LightGray, fontSize = 12.sp)
            block.type == BlockType.VARIABLE_REFERENCE -> {
                val variable = block.value as? Variable
                if (variable != null) {
                    DraggableReferenceBlock(
                        id = block.id,
                        variable = variable,
                        viewModel = viewModel
                    )
                } else {
                    Text("?", fontSize = 12.sp)
                }
            }
            block.type == BlockType.MATH_ADD || block.type == BlockType.MATH_SUBTRACT || block.type == BlockType.MATH_MULTIPLY || block.type == BlockType.MATH_DIVIDE -> {
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
            else -> Text(block.value?.toString() ?: "?", fontSize = 12.sp)
        }

        Modifier.pointerInput(id) {
            detectDragGestures { change, dragAmount ->
                change.consume()

                viewModel.tryInsertIntoSlot(change.position, id.toString())
            }
        }
    }
}