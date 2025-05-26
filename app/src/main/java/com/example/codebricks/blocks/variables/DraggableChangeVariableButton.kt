package com.example.codebricks.blocks.variables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableChangeVariableBlock(
    id: String,
    variable: Variable?,
    changeSign: String,
    changeAmount: Int,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    viewModel: VariableViewModel
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var showDeleteIcon by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableStateOf<Long>(0L) }
    var isPressed by remember { mutableStateOf(false) }

    val expandedVar = remember { mutableStateOf(false) }
    val selectedVar = remember { mutableStateOf(variable?.name ?: "") }

    val expandedSign = remember { mutableStateOf(false) }
    val selectedSign = remember { mutableStateOf(changeSign) }

    val inputText = remember(id) { mutableStateOf("") }
    val isEditing = remember { mutableStateOf(false) }
    val isError = remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(variable) {
        selectedVar.value = variable?.name ?: ""
    }
    LaunchedEffect(changeSign) {
        selectedSign.value = changeSign
    }
    LaunchedEffect(changeAmount) {
        if (!isEditing.value && inputText.value.isEmpty()) {

        }
    }

    LaunchedEffect(id, offset) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }

    Box(
        modifier = Modifier
            .offset {
                val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 44f)
                IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
            }
            .height(44.dp)
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
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center

    ) {
        if (showDeleteIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = "Change",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 4.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedVar.value,
                onExpandedChange = { expandedVar.value = !expandedVar.value },
                modifier = Modifier
            ) {
                Box(
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .widthIn(min = 48.dp, max = 300.dp)
                        .height(24.dp)
                        .height(24.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { expandedVar.value = true }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = selectedVar.value, fontSize = 11.sp, color = Color.Black)
                }
                ExposedDropdownMenu(
                    expanded = expandedVar.value,
                    onDismissRequest = { expandedVar.value = false },
                    modifier = Modifier.widthIn(min = 32.dp, max = 140.dp)
                ) {
                    viewModel.variables.forEach { variable ->
                        DropdownMenuItem(
                            text = { Text(variable.name, fontSize = 11.sp) },
                            modifier = Modifier.height(24.dp),
                            onClick = {
                                selectedVar.value = variable.name
                                viewModel.updateChangeBlockVariable(id, variable)
                                expandedVar.value = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "by",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedSign.value,
                onExpandedChange = { expandedSign.value = !expandedSign.value }
            ) {
                Box(
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .width(24.dp)
                        .height(24.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { expandedSign.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedSign.value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                ExposedDropdownMenu(
                    expanded = expandedSign.value,
                    onDismissRequest = { expandedSign.value = false },
                    modifier = Modifier.width(28.dp)
                ) {
                    listOf("+", "-").forEach { sign ->
                        DropdownMenuItem(
                            text = { Text(sign, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                            modifier = Modifier.height(20.dp),
                            onClick = {
                                selectedSign.value = sign
                                viewModel.updateChangeBlockSign(id, sign)
                                expandedSign.value = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            val targetWidth = (inputText.value.length * 8 + 20).dp.coerceIn(32.dp, 240.dp)
            val animatedWidth by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = tween(200)
            )

            BasicTextField(
                value = inputText.value,
                onValueChange = { newValue ->
                    inputText.value = newValue
                    isEditing.value = true
                    isError.value = newValue.any { c -> !c.isDigit() }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val parsed = inputText.value.toIntOrNull() ?: 0
                        viewModel.updateChangeBlockAmount(id, parsed)
                        isEditing.value = false
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    color = if (isError.value) Color.Red else Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .width(animatedWidth)
                    .height(24.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        color = if (isError.value) Color.Red else Color.Black,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 2.dp)
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}