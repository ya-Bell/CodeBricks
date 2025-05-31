package com.example.codebricks.blocks.write

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.ui.theme.BlockIO
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import kotlin.math.roundToInt

@Composable
fun DraggableWriteBlock(
    id: String,
    variable: Variable?,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    viewModel: VariableViewModel
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var isPressed by remember { mutableStateOf(false) }
    var isBeingDragged by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }

    Box(modifier = Modifier
        .offset {
            val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 44f)
            IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
        }
        .defaultMinSize(minWidth = 100.dp)
        .wrapContentWidth()
        .height(40.dp)
        .border(2.dp, TextBlack, RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .background(BlockIO)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    viewModel.shouldDrawConnections.value = false
                    isBeingDragged = true
                    isPressed = true
                },
                onDrag = { change, dragAmount ->
                    offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                    BlockPositionTracker.updateBlockPosition(id, offset)
                    change.consume()
                },
                onDragEnd = {
                    isBeingDragged = false
                    isPressed = false
                }
            )
        }) {
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
                contentDescription = stringResource(id = R.string.delete_icon_description),
                modifier = Modifier.size(12.dp),
                tint = TextBlack
            )
        }

        val displayText = variable?.let { "write (${it.name})" } ?: "write(?)"
        Text(
            text = displayText,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(5.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TextWhite
        )
    }
} 