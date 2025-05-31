package com.example.codebricks.blocks.logic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.blocks.math.DraggableMathBlock
import com.example.codebricks.blocks.variables.varreference.DraggableReferenceBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.ui.theme.BlockHighlighted
import com.example.codebricks.ui.theme.BlockLogic
import com.example.codebricks.ui.theme.BlockNormal
import com.example.codebricks.ui.theme.BlockSlotHighlighted
import com.example.codebricks.ui.theme.BlockSuccess
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextGray
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.updateElseIfBlockOperator
import com.example.codebricks.viewmodel.slot.isRecursiveInsertion
import com.example.codebricks.viewmodel.slot.setHighlightedSlot
import com.example.codebricks.viewmodel.slot.setRecentlyInsertedSlot
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.removeBlockRecursively
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableElseIfBlock(
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
    var showDeleteIcon by remember { mutableStateOf(true) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }
    val expandedOperator = remember { mutableStateOf(false) }

    LaunchedEffect(offset) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }
    LaunchedEffect(selectedOperator.value) {
        if (currentBlock != null) {
            viewModel.updateElseIfBlockOperator(id, selectedOperator.value)
        }
    }
    LaunchedEffect(redrawTrigger) {
        layoutCoordinates?.let { coords ->
            inputBlocks.forEachIndexed { index, _ ->
                val bounds = coords.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
                BlockSlotTracker.setSlotBounds(id, index, bounds)
            }
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
            .background(BlockLogic, RoundedCornerShape(12.dp))
            .border(2.dp, TextBlack, RoundedCornerShape(12.dp))
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
                        offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                        BlockPositionTracker.updateBlockPosition(id, offset)

                        // Проверяем возможность вставки во время перетаскивания
                        val windowCenter = layoutCoordinates?.boundsInWindow()?.center
                        if (windowCenter != null) {
                            val matchedSlot = BlockSlotTracker.getAllSlots()
                                .filter { slot ->
                                    val slotParentBlock = viewModel.findBlockById(slot.blockId)
                                    val draggedBlock =
                                        viewModel.findBlockById(id) ?: return@filter false
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
                                                    BlockType.MATH_MODULO,
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
                                                    BlockType.MATH_DIVIDE,
                                                    BlockType.MATH_MODULO
                                                )

                                                else -> false
                                            }
                                    isValidTarget
                                }
                                .map { it.copy(bounds = it.bounds.translate(BlockPositionTracker.canvasOffset)) }
                                .filter {
                                    it.bounds.inflate(MAGNETIC_PADDING).contains(windowCenter)
                                }
                                .minByOrNull { it.bounds.width * it.bounds.height }

                            if (matchedSlot != null) {
                                viewModel.setHighlightedSlot(
                                    matchedSlot.blockId,
                                    matchedSlot.slotIndex
                                )
                            } else {
                                viewModel.setHighlightedSlot(null, null)
                            }
                        }
                    },
                    onDragEnd = {
                        isPressed = false
                        val windowCenter = layoutCoordinates?.boundsInWindow()?.center
                        if (windowCenter != null) {
                            scope.launch {
                                viewModel.tryInsertIntoSlot(windowCenter, id)
                            }
                        }
                    }
                )
            }
    ) {
        if (showDeleteIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
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
            Text(
                stringResource(R.string.else_if),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            BlockInputsSlot(id, 0, inputBlocks.getOrNull(0), viewModel)

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
                    val operators = listOf(
                        stringResource(id = R.string.operator_equals),
                        stringResource(id = R.string.operator_not_equals),
                        stringResource(id = R.string.operator_greater),
                        stringResource(id = R.string.operator_less),
                        stringResource(id = R.string.operator_greater_equals),
                        stringResource(id = R.string.operator_less_equals)
                    )
                    operators.forEach { operator ->
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

            BlockInputsSlot(id, 1, inputBlocks.getOrNull(1), viewModel)
        }
    }
}

@Composable
fun BlockInputsSlot(
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
            .onGloballyPositioned {
                val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
                BlockSlotTracker.setSlotBounds(parentId, slotIndex, bounds)
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
                        BlockType.MATH_MODULO,
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