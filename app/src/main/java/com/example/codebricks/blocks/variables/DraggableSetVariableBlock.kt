package com.example.codebricks.blocks.variables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.blocks.common.Block
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.Variable
import androidx.compose.material3.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import com.example.codebricks.viewmodel.VariableViewModel
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.example.codebricks.blocks.common.BlockType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableSetVariableBlock(
    id: String,
    containerWidth: Float,
    containerHeight: Float,
    inputBlocks: List<Block> = emptyList(),
    viewModel: VariableViewModel
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    val targetVar = inputBlocks.getOrNull(0)?.value as? Variable
    val valueVar = inputBlocks.getOrNull(1)?.value as? Variable

    val variableType = targetVar?.type ?: "string"
    val valueText = remember { mutableStateOf("") }

    LaunchedEffect(offset) {
        BlockPositionTracker.updateBlockPosition(id, offset)
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .requiredSize(300.dp, 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFFFB8C00))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offset = Offset(
                        (offset.x + dragAmount.x).coerceIn(0f, containerWidth - 160.dp.toPx()),
                        (offset.y + dragAmount.y).coerceIn(0f, containerHeight - 44.dp.toPx())
                    )
                    BlockPositionTracker.updateBlockPosition(id, offset)
                    change.consume()
                }
            }
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(end = 4.dp)
            )

            val expanded = remember { mutableStateOf(false) }
            val selectedVar = remember { mutableStateOf(targetVar?.name ?: "") }

            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = !expanded.value }
            ) {
                Box(
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .width(64.dp)
                        .height(24.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { expanded.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedVar.value,
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                }

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
            Text(
                text = " to ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            val slotBoundsKey = id to 1

            val isHighlighted = viewModel.highlightedSlot.value == (id to 1)

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .onGloballyPositioned { coords ->
                        val globalBounds = coords.boundsInParent()
                        BlockSlotTracker.setSlotBounds(id, 1, globalBounds)
                    }
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            android.view.MotionEvent.ACTION_HOVER_ENTER,
                            android.view.MotionEvent.ACTION_HOVER_MOVE -> {
                                viewModel.onDragOverSlot(id, 1)
                            }

                            android.view.MotionEvent.ACTION_HOVER_EXIT -> {
                                viewModel.onDragExitSlot()
                            }
                        }
                        false
                    }
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(
                        width = 2.dp,
                        color = if (isHighlighted) Color(0xFF4CAF50) else Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (valueVar != null) {
                    Text(text = valueVar.name, fontSize = 11.sp, color = Color.Black)
                } else {
                    when (variableType) {
                        "bool" -> {
                            val selected = remember { mutableStateOf("true") }

                            Text(
                                text = selected.value,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        selected.value =
                                            if (selected.value == "true") "false" else "true"
                                        viewModel.updateSetBlockValue(id, selected.value)
                                    }
                                    .padding(4.dp),
                                color = Color.Black
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Red, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("DROP HERE", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
//@Preview(showBackground = true)
//@Composable
//fun DraggableSetVariableBlockPreview() {
//    val mockVariable = Variable(name = "score", value = 10, type = "int")
//    val block = Block(
//        id = "preview-set-block",
//        type = BlockType.VARIABLE_SET,
//        inputBlocks = mutableListOf(
//            Block(type = BlockType.VARIABLE_REFERENCE, value = mockVariable)
//        )
//    )
//
//    val mockViewModel = VariableViewModel().apply {
//        declareVariable("score", 10, "int")
//        declareVariable("lives", 3, "int")
//    }
//
//    DraggableSetVariableBlock(
//        id = block.id,
//        containerWidth = 400f,
//        containerHeight = 300f,
//        inputBlocks = block.inputBlocks,
//        viewModel = mockViewModel
//    )
//}


