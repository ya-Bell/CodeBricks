package com.example.codebricks.blocks.variables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
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
import com.example.codebricks.viewmodel.Variable

@Composable
fun DraggableReferenceBlock(
    id: String,
    variable: Variable,
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
            .requiredSize(100.dp, 32.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFEEEEEE))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offset = Offset(
                        (offset.x + dragAmount.x).coerceIn(0f, containerWidth - 100.dp.toPx()),
                        (offset.y + dragAmount.y).coerceIn(0f, containerHeight - 32.dp.toPx())
                    )
                    BlockPositionTracker.updateBlockPosition(id, offset)
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = variable.name,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}

