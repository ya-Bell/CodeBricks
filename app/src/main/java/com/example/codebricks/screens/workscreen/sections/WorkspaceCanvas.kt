package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.control.DraggableControlBlock
import com.example.codebricks.blocks.print.DraggablePrintBlock
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.blocks.variables.DraggableItem

@Composable
fun WorkspaceCanvas(
    viewModel: VariableViewModel,
    onSizeChanged: (Float, Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val gestureModifier = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(0.5f, 2.5f)
            offset += pan
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(Color.White)
            .border(1.dp, Color.Gray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(8.dp)
            .onSizeChanged { size ->
                onSizeChanged(size.width.toFloat(), size.height.toFloat())
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .then(gestureModifier)
    ) {
        val controlBlocks = viewModel.programBlocks.filter {
            it.type == BlockType.CONTROL_START || it.type == BlockType.CONTROL_STOP
        }
        val printBlocks = viewModel.programBlocks.filter { it.type == BlockType.IO_PRINT }
        val variableBlocks = viewModel.variables

        controlBlocks.forEach { block ->
            DraggableControlBlock(
                type = block.type.name,
                containerWidth = offset.x,
                containerHeight = offset.y
            )
        }

        printBlocks.forEach { block ->
            val variable = block.inputBlocks.firstOrNull()?.value as? Variable
            DraggablePrintBlock(
                variable = variable,
                containerWidth = offset.x,
                containerHeight = offset.y
            )
        }

        variableBlocks.forEach { variable ->
            DraggableItem(
                variable = variable,
                containerWidth = offset.x,
                containerHeight = offset.y
            )
        }
    }
}



@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun WorkspaceCanvasPreview() {
    val mockViewModel = VariableViewModel().apply {
        declareVariable("score", 42, "int")
        declareControlBlock("Start")
        declareControlBlock("Stop")
    }

    WorkspaceCanvas(
        viewModel = mockViewModel,
        onSizeChanged = { _, _ -> }
    )
}