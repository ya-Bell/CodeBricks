package com.example.codebricks.blocks.loops.whileendblocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.codebricks.ui.theme.BlockLoops
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel
import kotlin.math.roundToInt

@Composable
fun DraggableWhileEndBlock(
    id: String,
    containerWidth: Float,
    containerHeight: Float,
    onDelete: (String) -> Unit,
    viewModel: VariableViewModel
) {
    val redrawTrigger = BlockPositionTracker.redrawTrigger.intValue

    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var showDeleteIcon by remember { mutableStateOf(true) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }


    LaunchedEffect(offset) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .heightIn(min = 30.dp)
            .offset {
                val newOffset = limitPosition(offset, containerWidth, containerHeight, 300f, 50f)
                IntOffset(newOffset.x.roundToInt(), newOffset.y.roundToInt())
            }
            .background(BlockLoops, RoundedCornerShape(12.dp))
            .border(2.dp, TextBlack, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        viewModel.shouldDrawConnections.value = false
                        isPressed = true
                        dragStartTime = System.currentTimeMillis()
                    },
                    onDrag = { change, dragAmount ->
                        offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                        BlockPositionTracker.updateBlockPosition(id, offset)
                        change.consume()

                    },
                    onDragEnd = {
                        isPressed = false
                    }
                )
            }
    ) {
        if (showDeleteIcon) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .clickable { onDelete(id) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.delete_block),
                    modifier = Modifier.size(12.dp),
                    tint = TextBlack
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.loops_while_end),
                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite
            )

        }
    }
} 