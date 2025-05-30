package com.example.codebricks.blocks.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.viewmodel.VariableViewModel
import kotlin.math.roundToInt

@Composable
fun DraggableControlBlock(
    id: String,
    type: String,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    viewModel: VariableViewModel
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var isPressed by remember { mutableStateOf(false) }
    var isBeingDragged by remember { mutableStateOf(false) }

    LaunchedEffect(offset) {
        val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 44f)
        BlockPositionTracker.updateBlockPosition(id, newOffset)
    }

    Box(
        modifier = Modifier
            .offset {
                val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 44f)
                IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
            }
            .requiredSize(140.dp, 40.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3F51B5))
            .zIndex(if (isBeingDragged) 100f else 1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        viewModel.shouldDrawConnections.value = false
                        isBeingDragged = true
                        isPressed = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset = Offset(
                            offset.x + dragAmount.x,
                            offset.y + dragAmount.y
                        )
                    },
                    onDragEnd = {
                        isBeingDragged = false
                    }
                )
            }
    ) {
        // Крестик для удаления блока сразу
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clickable {
                    onDelete(id) // Удаление блока
                }
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Delete Block",
                modifier = Modifier.size(12.dp),
                tint = Color.Black
            )
        }

        // Отображение текста
        Text(
            text = if (type == "CONTROL_START") "Start" else "Stop",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

