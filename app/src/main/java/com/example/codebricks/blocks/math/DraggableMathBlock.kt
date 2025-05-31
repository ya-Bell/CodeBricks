package com.example.codebricks.blocks.math

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.codebricks.R
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.variables.varreference.DraggableReferenceBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker.MAGNETIC_PADDING
import com.example.codebricks.ui.theme.BlockHighlighted
import com.example.codebricks.ui.theme.BlockMath
import com.example.codebricks.ui.theme.BlockNormal
import com.example.codebricks.ui.theme.BlockSlotHighlighted
import com.example.codebricks.ui.theme.BlockSuccess
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextGray
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.slot.isRecursiveInsertion
import com.example.codebricks.viewmodel.slot.setHighlightedSlot
import com.example.codebricks.viewmodel.slot.tryInsertIntoSlot
import com.example.codebricks.viewmodel.tree.collectDescendantIds
import com.example.codebricks.viewmodel.tree.findBlockById
import com.example.codebricks.viewmodel.tree.findBlockContaining
import com.example.codebricks.viewmodel.tree.removeBlockFromParent
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
        BlockType.MATH_MODULO -> "%"
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
            val correctedOffset =
                layoutCoordinates!!.windowToLocal(windowOffset - BlockPositionTracker.canvasOffset)
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
            .border(2.dp, TextBlack, RoundedCornerShape(12.dp))
            .background(BlockMath)
            .zIndex(if (isBeingDragged) 100f else if (isInserted) 0f else 1f)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    viewModel.shouldDrawConnections.value = false
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
                    viewModel.shouldDrawConnections.value = false
                    isPressed = false
                    showDeleteIcon = false
                    isBeingDragged = false
                })
            }
            .pointerInput(id, isInserted) {
                detectDragGestures(
                    onDragStart = {
                        viewModel.shouldDrawConnections.value = false
                        isBeingDragged = true
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
                                // Если блока нет в programBlocks, добавляем его
                                val block = Block(
                                    id = id,
                                    type = type,
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
                            BlockPositionTracker.updateBlockPosition(
                                id,
                                canvasOffset ?: Offset.Zero
                            )
                            BlockPositionTracker.redrawTrigger.intValue++
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            animOffset.snapTo(animOffset.value + dragAmount)
                        }
                        BlockPositionTracker.updateBlockPosition(id, animOffset.value)

                        val draggedBlock = viewModel.programBlocks.find { it.id == id }
                            ?: return@detectDragGestures
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
                                        !viewModel.collectDescendantIds(draggedBlock)
                                            .contains(slot.blockId) &&
                                        !viewModel.isRecursiveInsertion(id, slot.blockId) &&
                                        slotParentBlock.type in listOf(
                                    BlockType.VARIABLE_SET,
                                    BlockType.MATH_ADD,
                                    BlockType.MATH_SUBTRACT,
                                    BlockType.MATH_MULTIPLY,
                                    BlockType.MATH_DIVIDE,
                                    BlockType.MATH_MODULO,
                                    BlockType.IF,
                                    BlockType.ELSE_IF,
                                    BlockType.WHILE,
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
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable { onDelete(id) }
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.delete_block),
                modifier = Modifier.size(12.dp),
                tint = TextBlack
            )
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
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
                    color = TextWhite,
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
                (previousBlock.value?.value as? Variable)?.name?.toDoubleOrNull() != null
            ) {
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
            .zIndex(1f)
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
        if (block != null) {
            when (block.type) {
                BlockType.MATH_ADD,
                BlockType.MATH_SUBTRACT,
                BlockType.MATH_MULTIPLY,
                BlockType.MATH_DIVIDE,
                BlockType.MATH_MODULO -> {
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
                            onDelete = { id ->
                                viewModel.removeBlockById(id)
                            },
                            viewModel = viewModel
                        )
                    } else {
                        Text(stringResource(R.string.unknown_value), fontSize = 12.sp)
                    }
                }

                else -> {
                    Text(
                        block.value?.toString() ?: stringResource(R.string.unknown_value),
                        fontSize = 12.sp
                    )
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
                textStyle = TextStyle(fontSize = 12.sp, color = TextBlack),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(IntrinsicSize.Min)
                    .focusable()
            )
        }
    }
}
