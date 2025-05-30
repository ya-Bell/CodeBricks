package com.example.codebricks.blocks.print

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.blocks.common.limitPosition
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.viewmodel.Variable
import kotlin.math.roundToInt

@Composable
fun DraggablePrintBlock(
    id: String,
    variable: Variable?,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }

    Box(modifier = Modifier
        .offset {
            val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 44f)
            IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
        }
        .requiredSize(140.dp, 40.dp)
        .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0xFFE57373))
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                if (!isPressed) {
                    isPressed = true
                }

                offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                BlockPositionTracker.updateBlockPosition(id, offset)
                change.consume()
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = false
                })
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
                tint = Color.Black
            )
        }

        val displayText = variable?.let { "print(${it.name})" } ?: "print(?)"
        Text(
            text = displayText,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}
