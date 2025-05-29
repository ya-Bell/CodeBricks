package com.example.codebricks.blocks.variables

//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.blocks.math.DraggableMathBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
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

    val block = viewModel.findBlockById(id) ?: return
    val targetVar = block.inputBlocks.getOrNull(0)?.value as? Variable
    val valueBlock = block.inputBlocks.getOrNull(1)

    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    val valueVar = inputBlocks.getOrNull(1)?.value as? Variable
    val variableType = targetVar?.type ?: "string"

    val inputText = remember(id) { mutableStateOf("") }
    val isEditing = remember { mutableStateOf(false) }
    val isError = remember { mutableStateOf(false) }

    var showDeleteIcon by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }

    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue

    val layoutCoordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(valueVar?.name, isEditing.value) {
        if (valueVar != null && !isEditing.value && inputText.value.isNotEmpty()) {
            inputText.value = ""
        }
    }
    LaunchedEffect(redrawTrigger, valueVar) {}

    LaunchedEffect(redrawTrigger) {
        layoutCoordinates.value?.let {
            val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
            BlockSlotTracker.setSlotBounds(id, 1, bounds)
        }
    }

    Box(modifier = Modifier
        .wrapContentWidth()
        .height(40.dp)
        .offset {
            val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 44f)
            IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
        }
        .widthIn(max = 500.dp)
        .clip(RoundedCornerShape(12.dp))
        .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
        .background(Color(0xFFFB8C00))
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
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = false
                    showDeleteIcon = false
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
                    contentDescription = "Delete Block",
                    modifier = Modifier.size(12.dp),
                    tint = Color.Black
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
                "Set",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(end = 4.dp)
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
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { expanded.value = true }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedVar.value,
                        fontSize = 11.sp,
                        color = Color.Black,
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


            Text(" to ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

            val isHighlighted = viewModel.highlightedSlot.value == (id to 1)
            val valueBlock = inputBlocks.getOrNull(1)

            val hasBlock = valueBlock != null
            val isBlockEmpty = valueBlock == null

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .wrapContentWidth()
                    .defaultMinSize(minWidth = 28.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .onGloballyPositioned {
                        layoutCoordinates.value = it
                        val bounds = it.boundsInWindow().translate(BlockPositionTracker.canvasOffset)
                        BlockSlotTracker.setSlotBounds(id, 1, bounds)

                    }
                    .border(
                        1.dp,
                        if (isError.value) Color.Red else if (isHighlighted) Color(0xFF4CAF50) else Color.Black,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                when {
                    hasBlock -> {
                        if (valueBlock != null) {
                            when (valueBlock.type) {
                                BlockType.VARIABLE_REFERENCE -> {
                                    val variable = valueBlock.value as? Variable
                                    if (variable != null) {
                                        DraggableReferenceBlock(
                                            id = valueBlock.id,
                                            variable = variable,
                                            viewModel = viewModel
                                        )
                                    }
                                }

                                BlockType.MATH_ADD, BlockType.MATH_SUBTRACT,
                                BlockType.MATH_MULTIPLY, BlockType.MATH_DIVIDE -> {
                                    DraggableMathBlock(
                                        id = valueBlock.id,
                                        type = valueBlock.type,
                                        inputBlocks = valueBlock.inputBlocks,
                                        containerWidth = 0f,
                                        containerHeight = 0f,
                                        onDelete = {
                                            viewModel.removeBlockRecursively(valueBlock.id)
                                            if (block.inputBlocks.size > 1) block.inputBlocks[1] = null
                                        },
                                        viewModel = viewModel
                                    )
                                }

                                else -> {
                                    Text(text = "", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    isBlockEmpty -> {
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
                                    if (!isError.value) {
                                        viewModel.updateSetBlockValue(id, inputText.value)
                                        isEditing.value = false
                                    }
                                }
                            ),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                            modifier = Modifier.widthIn(10.dp, 32.dp),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 9.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    innerTextField()
                                }
                            }
                        )
                    }

                    else -> {
                        Text("...", fontSize = 12.sp, color = Color.LightGray)
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


