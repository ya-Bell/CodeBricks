package com.example.codebricks.blocks.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker

@Composable
fun DraggableControlBlock(
    id: String,
    type: String,
    containerWidth: Float,
    containerHeight: Float
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    LaunchedEffect(Unit) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .requiredSize(140.dp, 40.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (type == "Start") Color(0xFF4CAF50) else Color(0xFFf44336))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offset = Offset(
                        (offset.x + dragAmount.x).coerceIn(0f, containerWidth - 140.dp.toPx()),
                        (offset.y + dragAmount.y).coerceIn(0f, containerHeight - 40.dp.toPx())
                    )
                    BlockPositionTracker.updateBlockPosition(id, offset)
                    change.consume()
                }
            }
    ) {
        Text(
            text = type,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White
        )
    }

}