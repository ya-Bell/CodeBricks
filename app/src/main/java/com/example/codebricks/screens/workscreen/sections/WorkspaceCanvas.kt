package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.control.DraggableControlBlock
import com.example.codebricks.blocks.print.DraggablePrintBlock
import com.example.codebricks.blocks.variables.DraggableDeclareBlock
import com.example.codebricks.blocks.variables.DraggableReferenceBlock
import com.example.codebricks.blocks.variables.DraggableSetVariableBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel



@Composable
fun WorkspaceCanvas(
    viewModel: VariableViewModel,
    onSizeChanged: (Float, Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var containerWidth by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }

    val redrawTrigger = BlockPositionTracker.redrawTrigger.value

    val gestureModifier = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(0.5f, 2.5f)
            offset += pan
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .background(Color.White)
            .border(1.dp, Color.Gray)
            .onSizeChanged { size ->
                containerWidth = size.width.toFloat()
                containerHeight = size.height.toFloat()
                onSizeChanged(containerWidth, containerHeight)
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .then(gestureModifier),
    ) {
        val controlBlocks = viewModel.programBlocks.filter {
            it.type == BlockType.CONTROL_START || it.type == BlockType.CONTROL_STOP
        }
        val printBlocks = viewModel.programBlocks.filter { it.type == BlockType.IO_PRINT }

        val setVariableBlocks = viewModel.programBlocks.filter { it.type == BlockType.VARIABLE_SET }

        controlBlocks.forEach { block ->
            DraggableControlBlock(
                id = block.id,
                type = block.type.name,
                containerWidth = containerWidth,
                containerHeight = containerHeight
            )
        }

        printBlocks.forEach { block ->
            val variable = block.inputBlocks.firstOrNull()?.value as? Variable
            DraggablePrintBlock(
                id = block.id,
                variable = variable,
                containerWidth = containerWidth,
                containerHeight = containerHeight
            )
        }
        setVariableBlocks.forEach { block ->
            val variable = block.value as? Variable
            DraggableSetVariableBlock(
                id = block.id,
                variable = variable,
                containerWidth = containerWidth,
                containerHeight = containerHeight)
        }


        val declareBlocks = viewModel.programBlocks.filter {
            it.type == BlockType.VARIABLE_DECLARE
        }
        val referenceBlocks = viewModel.programBlocks.filter {
            it.type == BlockType.VARIABLE_REFERENCE
        }
        declareBlocks.forEach { block ->
            val variable = block.value as? Variable
            if (variable != null) {
                DraggableDeclareBlock(
                    id = block.id,
                    variable = variable,
                    containerWidth = containerWidth,
                    containerHeight = containerHeight
                )
            }
        }

        referenceBlocks.forEach { block ->
            val variable = block.value as? Variable
            if (variable != null) {
                DraggableReferenceBlock(
                    id = block.id,
                    variable = variable,
                    containerWidth = containerWidth,
                    containerHeight = containerHeight
                )
            }
        }

        if (viewModel.shouldDrawConnections.value) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                //  redrawTrigger, чтобы Canvas знал об изменении
                redrawTrigger

                for (block in viewModel.programBlocks) {
                    val from = BlockPositionTracker.getPosition(block.id)
                    val to = block.nextBlockId?.let { BlockPositionTracker.getPosition(it) }

                    if (from != null && to != null) {
                        val fromPoint = from + Offset(140.dp.toPx() / 2, 40.dp.toPx() / 2)
                        val toPoint = to + Offset(140.dp.toPx() / 2, 40.dp.toPx() / 2)

                        drawLine(
                            color = Color.Black,
                            start = fromPoint,
                            end = toPoint,
                            strokeWidth = 4f
                        )
                    }
                }
            }
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