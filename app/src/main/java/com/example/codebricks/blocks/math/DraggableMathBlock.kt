package com.example.codebricks.blocks.math

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.variables.varreference.DraggableReferenceBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.slot.isRecursiveInsertion
import com.example.codebricks.viewmodel.slot.setHighlightedSlot
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import com.example.codebricks.viewmodel.tree.collectDescendantIds
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.findBlockContaining
import com.example.codebricks.viewmodel.tree.removeBlockRecursively
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableMathBlock(
    id: String,
    type: BlockType,
    inputBlocks: MutableList<Block?>,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    viewModel: VariableViewModel
) {
    val scrollState = rememberScrollState()
    val isInserted = viewModel.findBlockContaining(id) != null
    val scope = rememberCoroutineScope()
    val animOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val localOffset = remember { mutableStateOf(Offset.Zero) }
    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var showDeleteIcon by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }
    var isBeingDragged by remember { mutableStateOf(false) }
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

    Box(
        modifier = blockModifier
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
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFF4FC3F7))
            .zIndex(if (isBeingDragged) 100f else if (isInserted) 0f else 1f)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (!isPressed) {
                        isPressed = true
                        dragStartTime = System.currentTimeMillis()
                        isBeingDragged = true
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
            }
            .pointerInput(id) {
                detectTapGestures(onPress = {
                    isPressed = false
                    showDeleteIcon = false
                    isBeingDragged = false
                })
            }
            .pointerInput(id, isInserted) {
                detectDragGestures(
                    onDragStart = {
                        isBeingDragged = true
                        viewModel.findBlockContaining(id)?.id?.let { viewModel.bringBlockToFront(it) }
                        if (isInserted) {
                            val coords = layoutCoordinates
                            val windowPos = coords?.boundsInWindow()?.topLeft
                            val canvasOffset = windowPos?.plus(BlockPositionTracker.canvasOffset)

                            scope.launch {
                                canvasOffset?.let { animOffset.snapTo(it) }
                            }

                            // Находим все копии блока и удаляем их
                            fun removeAllCopies(blockId: String) {
                                viewModel.programBlocks.forEach { block ->
                                    for (i in block.inputBlocks.indices) {
                                        if (block.inputBlocks[i]?.id == blockId) {
                                            block.inputBlocks[i] = null
                                        }
                                    }
                                }
                            }

                            // Удаляем все копии блока
                            removeAllCopies(id)

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

                        val draggedBlock = viewModel.programBlocks.find { it.id == id } ?: return@detectDragGestures
                        val treeIds = viewModel.collectDescendantIds(draggedBlock) + setOf(id)

                        val coords = layoutCoordinates ?: return@detectDragGestures
                        val windowCenter = coords.boundsInWindow().center

                        val matchedSlot = BlockSlotTracker.getAllSlots()
                            .filter { slot ->
                                // Проверяем, что:
                                // 1. Слот не принадлежит блоку из нашего дерева
                                // 2. Родительский блок слота не находится в нашем дереве
                                // 3. Блок не пытается вставить сам в себя
                                // 4. Родительский блок поддерживает вставку
                                // 5. Нет циклических зависимостей
                                val slotParentBlock = viewModel.findBlockById(slot.blockId)
                                val isValidTarget = slot.blockId !in treeIds &&
                                        slotParentBlock != null &&
                                        !treeIds.contains(slotParentBlock.id) &&
                                        !viewModel.collectDescendantIds(draggedBlock).contains(slot.blockId) &&
                                        !viewModel.isRecursiveInsertion(id, slot.blockId) &&
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
                        viewModel.setHighlightedSlot(null, null)

                        val coords = layoutCoordinates ?: return@detectDragGestures
                        val windowCenter = coords.boundsInWindow().center

                        scope.launch {
                            viewModel.tryInsertIntoSlot(windowCenter, id)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (showDeleteIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { onDelete(id) }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(12.dp),
                    tint = Color.Black
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 8.dp)
                .widthIn(max = 2000.dp)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MathInputSlot(id, 0, inputBlocks.getOrNull(0), viewModel)

            Box(
                modifier = Modifier.width(20.dp),
                contentAlignment = Alignment.Center
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
    parentId: String,
    slotIndex: Int,
    block: Block?,
    viewModel: VariableViewModel
) {
    val isHighlighted = viewModel.highlightedSlot.value == (parentId to slotIndex)
    val isRecentlyInserted = viewModel.recentlyInsertedSlot.value == (parentId to slotIndex)

    val inputText = remember { mutableStateOf("") }
    val isEditing = remember { mutableStateOf(false) }
    val previousBlock = remember { mutableStateOf<Block?>(null) }

    // Очистка текстового ввода и удаление предыдущего блока, если пришёл новый блок
    LaunchedEffect(block?.id) {
        if (block != null) {
            // Если был предыдущий блок с числом, удаляем его только если это новый блок
            if (previousBlock.value != null &&
                previousBlock.value?.id != block.id &&
                previousBlock.value?.type == BlockType.VARIABLE_REFERENCE &&
                (previousBlock.value?.value as? Variable)?.name?.toDoubleOrNull() != null) {
                viewModel.removeBlockRecursively(previousBlock.value!!.id)
            }
            previousBlock.value = block
            inputText.value = ""
            isEditing.value = false
        }
    }

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
        if (block != null) {
            when (block.type) {
                BlockType.MATH_ADD,
                BlockType.MATH_SUBTRACT,
                BlockType.MATH_MULTIPLY,
                BlockType.MATH_DIVIDE -> {
                    if (!viewModel.programBlocks.any { it.id == block.id }) {
                        viewModel.addBlock(block)
                    }
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
                            id = block.id,
                            variable = variable,
                            viewModel = viewModel
                        )
                    } else {
                        Text("?", fontSize = 12.sp)
                    }
                }

                else -> {
                    Text(block.value?.toString() ?: "?", fontSize = 12.sp)
                }
            }
        } else {
            BasicTextField(
                value = inputText.value,
                onValueChange = {
                    inputText.value = it.filter { c -> c.isDigit() || c == '.' }
                    isEditing.value = true
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (inputText.value.isNotEmpty()) {
                        val value = inputText.value
                        val number = value.toDoubleOrNull() ?: 0.0
                        val fakeVar = Variable(name = value, value = number, type = "double")
                        val newBlock = Block(type = BlockType.VARIABLE_REFERENCE, value = fakeVar)
                        viewModel.addBlock(newBlock)

                        // Находим родительский блок
                        val parentBlock = viewModel.findBlockById(parentId)
                        if (parentBlock != null) {
                            // Расширяем список inputBlocks если нужно
                            while (parentBlock.inputBlocks.size <= slotIndex) {
                                parentBlock.inputBlocks.add(null)
                            }
                            // Сохраняем старый блок
                            val oldBlock = parentBlock.inputBlocks[slotIndex]
                            if (oldBlock != null) {
                                viewModel.removeBlockRecursively(oldBlock.id)
                            }
                            // Вставляем новый блок
                            parentBlock.inputBlocks[slotIndex] = newBlock
                            previousBlock.value = newBlock
                            BlockPositionTracker.redrawTrigger.intValue++
                        }

                        inputText.value = ""
                        isEditing.value = false
                    }
                }),
                singleLine = true,
                textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .widthIn(min = 32.dp)
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MathBlockPreview() {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(40.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFF4FC3F7), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(32.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("...", color = Color.LightGray, fontSize = 12.sp)
            }

            Text(
                "+",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(32.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("...", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun NestedMathBlockPreview() {
    val viewModel = VariableViewModel()

    val innerBlock = Block(
        type = BlockType.MATH_ADD,
        inputBlocks = mutableListOf(
            Block(type = BlockType.VARIABLE_REFERENCE, value = Variable("a", 5, "int")),
            Block(type = BlockType.VARIABLE_REFERENCE, value = Variable("b", 3, "int"))
        )
    )

    val outerBlock = Block(
        type = BlockType.MATH_MULTIPLY,
        inputBlocks = mutableListOf(
            innerBlock,
            Block(type = BlockType.VARIABLE_REFERENCE, value = Variable("c", 2, "int"))
        )
    )

    DraggableMathBlock(
        id = outerBlock.id,
        type = outerBlock.type,
        inputBlocks = outerBlock.inputBlocks,
        containerWidth = 400f,
        containerHeight = 400f,
        onDelete = {},
        viewModel = viewModel
    )
}