package com.example.codebricks.blocks.variables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.limitPosition
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
    inputBlocks: List<Block> = emptyList(),
    viewModel: VariableViewModel
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    val targetVar = inputBlocks.getOrNull(0)?.value as? Variable
    val valueVar = inputBlocks.getOrNull(1)?.value as? Variable
    val variableType = targetVar?.type ?: "string"

    val inputText = remember(id) { mutableStateOf("") }
    /* val showApply = remember { mutableStateOf(false) } */
    val isEditing = remember { mutableStateOf(false) }
    val isError = remember { mutableStateOf(false) }

    var showDeleteIcon by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableStateOf<Long>(0L) }
    var isPressed by remember { mutableStateOf(false) }

    val redrawTrigger = BlockPositionTracker.redrawTrigger.value

    LaunchedEffect(valueVar?.name, isEditing.value){
        if (valueVar != null && !isEditing.value && inputText.value.isNotEmpty()) {
            inputText.value = ""
        }
    }
    LaunchedEffect(redrawTrigger, valueVar) {
    }

    Box(
        modifier = Modifier
            .offset {
                val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 44f)
                IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
            }
            .height(44.dp)
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
                    }
                )
            }
    ) {
        if (showDeleteIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clickable {
                        onDelete(id)
                    }
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
                .padding(horizontal = 8.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Set", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(end = 4.dp))

            val expanded = remember { mutableStateOf(false) }
            val selectedVar = remember { mutableStateOf(targetVar?.name ?: "") }

            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = !expanded.value }
            ) {
                Box(
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .widthIn(min = 48.dp, max = 300.dp)
                        .height(24.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { expanded.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedVar.value,
                        fontSize = 11.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )                }

                ExposedDropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    viewModel.variables.forEach { variable ->
                        DropdownMenuItem(
                            text = { Text(variable.name) },
                            onClick = {
                                selectedVar.value = variable.name
                                viewModel.updateSetBlockTarget(id, variable)
                                expanded.value = false
                            }
                        )
                    }
                }
            }

            Text(" to ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

            val isHighlighted = viewModel.highlightedSlot.value == (id to 1)
            val valueBlock = inputBlocks.getOrNull(1)
            val blockId = valueBlock?.id

            val blockWidth = blockId?.let { BlockPositionTracker.getBlockWidth(it) }
            val textLength = if (isEditing.value) inputText.value.length else valueVar?.name?.length ?: 1

            val targetWidth: Dp = when {
                blockWidth != null && valueVar != null && !isEditing.value -> {
                    with(LocalDensity.current) { blockWidth.toDp() }.coerceIn(32.dp, 240.dp)
                }
                else -> (textLength * 7 + 20).dp.coerceIn(32.dp, 240.dp)
            }

            val animatedWidth by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = tween(200)
            )


            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(animatedWidth)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .onGloballyPositioned {
                        val globalBounds = it.boundsInWindow()
                        BlockSlotTracker.setSlotBounds(id, 1, globalBounds)
                    }
                    .border(
                        2.dp,
                        if (isError.value) Color.Red else if (isHighlighted) Color(0xFF4CAF50) else Color.Black,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (valueVar != null && !isEditing.value) {
                    Text(
                        text = valueVar.name,
                        fontSize = 11.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .clickable {
                                inputText.value = valueVar.name
                                isEditing.value = true
                            }
                            .padding(horizontal = 8.dp)
                    )
                } else {
                    when (variableType) {
                        "bool" -> {
                            val boolOptions = listOf("true", "false")
                            val selectedBool = remember { mutableStateOf("true") }
                            val boolExpanded = remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = boolExpanded.value,
                                onExpandedChange = { boolExpanded.value = !boolExpanded.value }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                                        .width(80.dp)
                                        .height(24.dp)
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                                        .clickable { boolExpanded.value = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(selectedBool.value, fontSize = 11.sp, color = Color.Black)
                                }

                                ExposedDropdownMenu(
                                    expanded = boolExpanded.value,
                                    onDismissRequest = { boolExpanded.value = false }
                                ) {
                                    boolOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedBool.value = option
                                                boolExpanded.value = false
                                                viewModel.updateSetBlockValue(id, option)
                                                isEditing.value = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            BasicTextField(
                                value = inputText.value,
                                onValueChange = {
                                    inputText.value = it
                                    isEditing.value = true
                                    /* showApply.value = it.isNotBlank() */
                                    isError.value = when (variableType) {
                                        "int" -> it.any { c -> !c.isDigit() }
                                        "double" -> it.replace(",", ".").toDoubleOrNull() == null
                                        else -> false
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (!isError.value) {
                                            val cleanedValue = if (variableType == "double") {
                                                inputText.value.replace(",", ".").toDoubleOrNull()?.toString() ?: "0.0"
                                            } else inputText.value

                                            viewModel.updateSetBlockValue(id, cleanedValue)
                                            isEditing.value = false
                                        }
                                    }
                                ),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

