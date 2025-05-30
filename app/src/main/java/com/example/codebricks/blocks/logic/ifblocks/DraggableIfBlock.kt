package com.example.codebricks.blocks.logic.ifblocks

import android.R.attr.id
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
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
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.updateIfBlockOperator
import com.example.codebricks.viewmodel.slot.isRecursiveInsertion
import com.example.codebricks.viewmodel.slot.setHighlightedSlot
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.removeBlockRecursively
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.zIndex
import com.example.codebricks.viewmodel.slot.setRecentlyInsertedSlot
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableIfBlock(
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
            viewModel.updateIfBlockOperator(id, selectedOperator.value)
        }
    }
    LaunchedEffect(redrawTrigger) {
        layoutCoordinates?.let { coords ->
            val bounds = coords.boundsInWindow()
            BlockPositionTracker.setBlockSize(id, bounds.width, bounds.height)
            // Слоты регистрируются в BlockInputSlot через onGloballyPositioned
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
            .onGloballyPositioned { layoutCoordinates = it }
            .background(Color(0xFF81C784), RoundedCornerShape(12.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        viewModel.shouldDrawConnections.value = false
                        isPressed = true
                        dragStartTime = System.currentTimeMillis()
                        
                        // Запоминаем текущую позицию для плавного перемещения
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
                                            BlockType.VARIABLE_SET -> draggedBlock.type in listOf(
                                                BlockType.VARIABLE_REFERENCE,
                                                BlockType.MATH_ADD,
                                                BlockType.MATH_SUBTRACT,
                                                BlockType.MATH_MULTIPLY,
                                                BlockType.MATH_DIVIDE
                                            )
                                            else -> false
                                        }
                                    isValidTarget
                                }
                                .map { it.copy(bounds = it.bounds.translate(BlockPositionTracker.canvasOffset)) }
                                .filter { it.bounds.inflate(MAGNETIC_PADDING).contains(windowCenter) }
                                .minByOrNull { it.bounds.width * it.bounds.height }

                            if (matchedSlot != null) {
                                viewModel.setHighlightedSlot(matchedSlot.blockId, matchedSlot.slotIndex)
                            } else {
                                viewModel.setHighlightedSlot(null, null)
                            }
                        }

                        if (System.currentTimeMillis() - dragStartTime >= 2500) {
                            showDeleteIcon = true
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
                    contentDescription = "Delete Block",
                    modifier = Modifier.size(12.dp),
                    tint = Color.Black
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .horizontalScroll(rememberScrollState()),
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
                        .width(40.dp)
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
                    listOf("==", "!=", ">", "<", ">=", "<=").forEach { operator ->
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
    val isRecentlyInserted = viewModel.recentlyInsertedSlot.value == (parentId to slotIndex)
    var showInput by remember { mutableStateOf(block == null) }
    var inputValue by remember { mutableStateOf("") }
    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .wrapContentWidth()
            .heightIn(min = 32.dp)
            .defaultMinSize(minWidth = 30.dp)
            .zIndex(1f)
            .onGloballyPositioned {
                val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
                BlockSlotTracker.setSlotBounds(parentId, slotIndex, bounds)
                BlockPositionTracker.redrawTrigger.intValue++
            }
            .border(
                width = if (isRecentlyInserted) 2.dp else 1.dp,
                color = when {
                    isRecentlyInserted -> Color(0xFF4CAF50)
                    isHighlighted -> Color(0xFF2196F3)
                    else -> Color.Gray
                },
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isHighlighted) Color(0xFFE3F2FD) else Color.White,
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
                        BlockType.VARIABLE_REFERENCE,
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
                            Text("...", color = Color.LightGray, fontSize = 12.sp)
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
                        color = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputValue.isNotEmpty()) {
                                val variable = Variable(name = inputValue, value = inputValue, type = "string")
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