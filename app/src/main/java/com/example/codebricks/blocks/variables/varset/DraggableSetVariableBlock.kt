package com.example.codebricks.blocks.variables.varset


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
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
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.ui.theme.BlockHighlighted
import com.example.codebricks.ui.theme.BlockNormal
import com.example.codebricks.ui.theme.BlockSuccess
import com.example.codebricks.ui.theme.BlockVariables
import com.example.codebricks.ui.theme.IconRed
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.declareVariable
import com.example.codebricks.viewmodel.blocks.updateSetBlockTarget
import com.example.codebricks.viewmodel.slot.isRecursiveInsertion
import com.example.codebricks.viewmodel.slot.setHighlightedSlot
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.findBlockContaining
import com.example.codebricks.viewmodel.tree.removeBlockFromParent
import com.example.codebricks.viewmodel.tree.removeBlockRecursively
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableSetVariableBlock(
    id: String,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    inputBlocks: MutableList<Block?> = mutableListOf(),
    viewModel: VariableViewModel
) {
    val scope = rememberCoroutineScope()
    val block = viewModel.findBlockById(id) ?: return
    val targetVar = block.inputBlocks.getOrNull(0)?.value as? Variable
    val valueBlock = block.inputBlocks.getOrNull(1)

    val isInserted = viewModel.findBlockContaining(id) != null
    var isBeingDragged by remember { mutableStateOf(false) }
    val animOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val localOffset = remember { mutableStateOf(Offset.Zero) }

    val valueVar = inputBlocks.getOrNull(1)?.value as? Variable
    val variableType = targetVar?.type ?: "string"

    val inputText = remember(id) { mutableStateOf("") }
    val isEditing = remember { mutableStateOf(false) }
    val isError = remember { mutableStateOf(false) }

    var showDeleteIcon by remember { mutableStateOf(true) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }

    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(valueVar?.name, isEditing.value) {
        if (valueVar != null && !isEditing.value && inputText.value.isNotEmpty()) {
            inputText.value = ""
        }
    }

    LaunchedEffect(redrawTrigger) {
        layoutCoordinates?.let {
            val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
            BlockSlotTracker.setSlotBounds(id, 1, bounds)
        }
    }

    LaunchedEffect(id, isInserted, redrawTrigger) {
        if (isInserted && layoutCoordinates != null) {
            val parent = viewModel.findBlockContaining(id)
            val slot = BlockSlotTracker.getAllSlots().find {
                it.blockId == parent?.id && parent.inputBlocks.getOrNull(it.slotIndex)?.id == id
            }
            val windowOffset = slot?.bounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
            val correctedOffset = layoutCoordinates!!.windowToLocal(windowOffset - BlockPositionTracker.canvasOffset)
            if (!correctedOffset.x.isNaN() && !correctedOffset.y.isNaN() && correctedOffset != localOffset.value) {
                localOffset.value = correctedOffset
            }
        }
    }

    Box(modifier = Modifier
        .wrapContentWidth()
        .height(40.dp)
        .then(
            if (isInserted) Modifier else Modifier.offset {
                IntOffset(animOffset.value.x.roundToInt(), animOffset.value.y.roundToInt())
            }
        )
        .onGloballyPositioned { coords ->
            layoutCoordinates = coords
            BlockPositionTracker.setBlockSize(
                id, coords.size.width.toFloat(), coords.size.height.toFloat()
            )

            if (isInserted) {
                val parent = viewModel.findBlockContaining(id)
                val slot = BlockSlotTracker.getAllSlots().find {
                    it.blockId == parent?.id && parent.inputBlocks.getOrNull(it.slotIndex)?.id == id
                }
                val windowOffset = slot?.bounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
                val local = coords.windowToLocal(windowOffset)
                localOffset.value = local
            }
        }
        .clip(RoundedCornerShape(12.dp))
        .border(2.dp, TextBlack, RoundedCornerShape(12.dp))
        .background(BlockVariables)
        .zIndex(if (isBeingDragged) 100f else if (isInserted) 0f else 1f)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    viewModel.shouldDrawConnections.value = false
                    isBeingDragged = true
                    dragStartTime = System.currentTimeMillis()
                    viewModel.findBlockContaining(id)?.id?.let { viewModel.bringBlockToFront(it) }
                    if (isInserted) {
                        val coords = layoutCoordinates
                        val windowPos = coords?.boundsInWindow()?.topLeft
                        val canvasOffset = windowPos?.plus(BlockPositionTracker.canvasOffset)

                        scope.launch {
                            canvasOffset?.let { animOffset.snapTo(it) }
                        }

                        // Убеждаемся, что блок есть в programBlocks
                        val currentBlock = viewModel.findBlockById(id)
                        if (currentBlock == null) {
                            val block = Block(
                                id = id,
                                type = BlockType.VARIABLE_SET,
                                inputBlocks = inputBlocks.toMutableList()
                            )
                            viewModel.addBlock(block)
                        }

                        // Сохраняем все вложенные блоки и их состояния
                        val nestedBlocks = mutableListOf<Block>()
                        currentBlock?.inputBlocks?.filterNotNull()?.forEach { inputBlock ->
                            // Рекурсивно копируем вложенные блоки
                            fun copyBlockWithChildren(block: Block): Block {
                                val newBlock = Block(
                                    id = block.id,
                                    type = block.type,
                                    inputBlocks = block.inputBlocks.map { child ->
                                        child?.let { copyBlockWithChildren(it) }
                                    }.toMutableList(),
                                    value = block.value
                                )
                                return newBlock
                            }
                            nestedBlocks.add(copyBlockWithChildren(inputBlock))
                        }

                        // Удаляем блок из родителя, но НЕ из programBlocks
                        viewModel.removeBlockFromParent(id)

                        // Восстанавливаем вложенные блоки
                        currentBlock?.inputBlocks?.clear()
                        nestedBlocks.forEach { nestedBlock ->
                            currentBlock?.inputBlocks?.add(nestedBlock)
                        }

                        // Обновляем позицию блока
                        BlockPositionTracker.updateBlockPosition(id, canvasOffset ?: Offset.Zero)
                        BlockPositionTracker.redrawTrigger.intValue++
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        animOffset.snapTo(animOffset.value + dragAmount)
                    }
                    BlockPositionTracker.updateBlockPosition(id, animOffset.value)
                    // Проверяем возможные слоты для вставки
                    val coords = layoutCoordinates ?: return@detectDragGestures
                    val windowCenter = coords.boundsInWindow().center

                    val matchedSlot = BlockSlotTracker.getAllSlots()
                        .filter { slot ->
                            val slotParentBlock = viewModel.findBlockById(slot.blockId)
                            val isValidTarget = slotParentBlock != null &&
                                slot.blockId != id &&
                                !viewModel.isRecursiveInsertion(id, slot.blockId) &&
                                slotParentBlock.type !in listOf(
                                    BlockType.IF,
                                    BlockType.ELSE_IF,
                                    BlockType.COMPARISON_EQUAL,
                                    BlockType.COMPARISON_GREATER,
                                    BlockType.COMPARISON_LESS,
                                    BlockType.LOGIC_AND,
                                    BlockType.LOGIC_OR,
                                    BlockType.LOGIC_NOT
                                )
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
                },
                onDragEnd = {
                    isBeingDragged = false
                    scope.launch {
                        viewModel.tryInsertIntoSlot(layoutCoordinates?.boundsInWindow()?.center ?: Offset.Zero, id)
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = false
                })
        }) {
        if (showDeleteIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clickable {
                        onDelete(id)
                    }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.delete_icon_description),
                    modifier = Modifier.size(12.dp),
                    tint = TextBlack
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.set),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )

            val expanded = remember { mutableStateOf(false) }
            val selectedVar = remember { mutableStateOf(targetVar?.name ?: "") }

            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = !expanded.value }) {
                Box(
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryEditable, enabled = true
                        )
                        .widthIn(min = 48.dp, max = 300.dp)
                        .height(24.dp)
                        .background(TextWhite, RoundedCornerShape(4.dp))
                        .border(1.dp, TextBlack, RoundedCornerShape(4.dp))
                        .clickable { expanded.value = true }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedVar.value,
                        fontSize = 11.sp,
                        color = TextBlack,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                ExposedDropdownMenu(
                    expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                    viewModel.variables.forEach { variable ->
                        DropdownMenuItem(text = { Text(variable.name) }, onClick = {
                            selectedVar.value = variable.name
                            viewModel.updateSetBlockTarget(id, variable)
                            expanded.value = false
                        })
                    }
                }
            }


            Text(
                text = stringResource(id = R.string.to),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            val isHighlighted = viewModel.highlightedSlot.value == (id to 1)
            val isRecentlyInserted = viewModel.recentlyInsertedSlot.value == (id to 1)
            val valueBlock = inputBlocks.getOrNull(1)

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .wrapContentWidth()
                    .defaultMinSize(minWidth = 28.dp)
                    .background(
                        color = if (isHighlighted) BlockHighlighted else BlockNormal,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onGloballyPositioned {
                        layoutCoordinates = it
                        val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
                        BlockSlotTracker.setSlotBounds(id, 1, bounds)
                    }
                    .border(
                        width = if (isRecentlyInserted) 2.dp else 1.dp,
                        color = when {
                            isRecentlyInserted -> BlockSuccess
                            isHighlighted -> BlockHighlighted
                            isError.value -> IconRed
                            else -> TextBlack
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    valueBlock != null -> {
                        when (valueBlock.type) {
                            BlockType.VARIABLE_REFERENCE -> {
                                val variable = valueBlock.value as? Variable
                                if (variable != null) {
                                    DraggableReferenceBlock(
                                        id = valueBlock.id,
                                        variable = variable,
                                        viewModel = viewModel,
                                        onDelete = {
                                            viewModel.removeBlockRecursively(valueBlock.id)
                                            if (block.inputBlocks.size > 1) block.inputBlocks[1] = null
                                        }
                                    )
                                }
                            }

                            BlockType.MATH_ADD, BlockType.MATH_SUBTRACT,
                            BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE,BlockType.MATH_MODULO -> {
                                DraggableMathBlock(
                                    id = valueBlock.id,
                                    type = valueBlock.type,
                                    inputBlocks = valueBlock.inputBlocks,
                                    containerWidth = 0f,
                                    containerHeight = 0f,
                                    onDelete = {
                                        // Только очищаем слот, не удаляем блок полностью
                                        if (block.inputBlocks.size > 1) block.inputBlocks[1] = null
                                        BlockPositionTracker.redrawTrigger.intValue++
                                    },
                                    viewModel = viewModel
                                )
                            }

                            else -> {
                                Text(text = "", fontSize = 12.sp)
                            }
                        }
                    }

                    else -> {
                        BasicTextField(
                            value = inputText.value,
                            onValueChange = {
                                val newValue = it.filter { char -> char.isDigit() || char == '.' }
                                inputText.value = newValue
                                isEditing.value = true
                                isError.value = false
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (!isError.value && inputText.value.isNotEmpty()) {
                                        val value = inputText.value
                                        val number = value.toDoubleOrNull() ?: 0.0
                                        val fakeVar = Variable(name = value, value = number, type = "double")
                                        val newBlock = Block(type = BlockType.VARIABLE_REFERENCE, value = fakeVar)
                                        viewModel.addBlock(newBlock)

                                        // Вставляем новый блок
                                        if (block.inputBlocks.size <= 1) {
                                            block.inputBlocks.add(null)
                                        }
                                        val oldBlock = block.inputBlocks[1]
                                        if (oldBlock != null) {
                                            viewModel.removeBlockRecursively(oldBlock.id)
                                        }
                                        block.inputBlocks[1] = newBlock
                                        BlockPositionTracker.redrawTrigger.intValue++

                                        inputText.value = ""
                                        isEditing.value = false
                                    }
                                }
                            ),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, color = if (isError.value) IconRed else TextBlack),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(IntrinsicSize.Min)
                                .focusable()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DraggableSetVariableBlockPreview() {
    val mockViewModel = remember { VariableViewModel() }

    val variableX = Variable(name = "x", value = 0, type = "int")
    val variableY = Variable(name = "y", value = 123, type = "int")

    mockViewModel.declareVariable(variableX.name, variableX.value, variableX.type)
    mockViewModel.declareVariable(variableY.name, variableY.value, variableY.type)
    val targetBlock = Block(
        type = BlockType.VARIABLE_REFERENCE, value = variableX
    )
    val valueBlock = Block(
        type = BlockType.VARIABLE_REFERENCE, value = variableY
    )
    val setBlock = Block(
        type = BlockType.VARIABLE_SET, inputBlocks = mutableListOf(targetBlock, valueBlock)
    )

    DraggableSetVariableBlock(
        id = setBlock.id, containerWidth = 1000f, containerHeight = 1000f, onDelete = {},
//        inputBlocks = setBlock.inputBlocks,
        viewModel = mockViewModel
    )
}


