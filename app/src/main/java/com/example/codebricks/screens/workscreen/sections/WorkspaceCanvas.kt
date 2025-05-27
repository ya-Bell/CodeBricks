package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.control.DraggableControlBlock
import com.example.codebricks.blocks.math.DraggableMathBlock
import com.example.codebricks.blocks.print.DraggablePrintBlock
import com.example.codebricks.blocks.variables.DraggableChangeVariableBlock
import com.example.codebricks.blocks.variables.DraggableDeclareBlock
import com.example.codebricks.blocks.variables.DraggableReferenceBlock
import com.example.codebricks.blocks.variables.DraggableSetVariableBlock
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel

@SuppressLint("UnusedTransitionTargetStateParameter", "ConfigurationScreenWidthHeight")
@Composable
fun WorkspaceCanvas(
    viewModel: VariableViewModel, onSizeChanged: (Float, Float) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val adaptiveHeight = screenHeight.value * 0.42f


    val canvasSize = remember { mutableStateOf(IntSize(0, 0)) }
    val contentSize = 3000f // холст 4000x4000
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoomCount by remember { mutableIntStateOf(0) }
    val maxZoom = 3
    val zoomFactor = 1.25f
    val redrawTrigger = BlockPositionTracker.redrawTrigger.value


    // функции управления зумом
    fun zoomIn() {
        if (zoomCount < maxZoom) {
            scale *= zoomFactor
            zoomCount++
            offset = Offset(
                x = offset.x.coerceIn(
                    (canvasSize.value.width - contentSize * scale).coerceAtMost(0f), 0f
                ), y = offset.y.coerceIn(
                    (canvasSize.value.height - contentSize * scale).coerceAtMost(
                        0f
                    ), 0f
                )
            )
        }
    }

    fun zoomOut() {
        if (zoomCount > 0) {
            scale /= zoomFactor
            zoomCount--
            offset = Offset(
                x = offset.x.coerceIn(
                    (canvasSize.value.width - contentSize * scale).coerceAtMost(0f), 0f
                ), y = offset.y.coerceIn(
                    (canvasSize.value.height - contentSize * scale).coerceAtMost(
                        0f
                    ), 0f
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(adaptiveHeight.dp)
            .background(Color(0xFFF0F0F0))
            .border(1.dp, Color.Gray)
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val scaledW = contentSize * scale
                    val scaledH = contentSize * scale
                    val minX = (canvasSize.value.width - scaledW).coerceAtMost(0f)
                    val minY = (canvasSize.value.height - scaledH).coerceAtMost(0f)
                    val newOffset = offset + dragAmount
                    offset = Offset(
                        x = newOffset.x.coerceIn(minX, 0f), y = newOffset.y.coerceIn(minY, 0f)
                    )
                    change.consume()
                }
            }
            .onSizeChanged {
                canvasSize.value = it
                onSizeChanged(it.width.toFloat(), it.height.toFloat())
            }) {
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .width(with(density) { contentSize.toDp() })
                .height(with(density) { contentSize.toDp() })
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .onGloballyPositioned {
                    // фиксируем scale и offset
                    BlockPositionTracker.canvasScale = scale
                    BlockPositionTracker.canvasOffset = offset
                }) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // красная рамка
                drawRect(
                    color = Color.Red,
                    topLeft = Offset.Zero,
                    size = Size(contentSize, contentSize),
                    style = Stroke(width = 4f)
                )
                // сетка
                val step = 40f * scale
                var x = 0f
                while (x <= contentSize) {
                    drawLine(Color.LightGray, Offset(x, 0f), Offset(x, contentSize), 1f)
                    x += step
                }
                var y = 0f
                while (y <= contentSize) {
                    drawLine(Color.LightGray, Offset(0f, y), Offset(contentSize, y), 1f)
                    y += step
                }
            }
            // блоки
            val controlBlocks = viewModel.programBlocks.filter {
                it.type == BlockType.CONTROL_START || it.type == BlockType.CONTROL_STOP
            }
            val printBlocks = viewModel.programBlocks.filter { it.type == BlockType.IO_PRINT }
            val setVariableBlocks =
                viewModel.programBlocks.filter { it.type == BlockType.VARIABLE_SET }
            val changeVariableBlocks =
                viewModel.programBlocks.filter { it.type == BlockType.VARIABLE_CHANGE }

            controlBlocks.forEach { block ->
                DraggableControlBlock(
                    id = block.id,
                    type = block.type.name,
                    containerWidth = contentSize,
                    containerHeight = contentSize,
                    onDelete = { blockId -> viewModel.removeBlockById(blockId) })
            }

            printBlocks.forEach { block ->
                val variable = block.inputBlocks.firstOrNull()?.value as? Variable
                DraggablePrintBlock(
                    id = block.id,
                    variable = variable,
                    containerWidth = contentSize,
                    containerHeight = contentSize,
                    onDelete = { blockId -> viewModel.removeBlockById(blockId) })
            }

            setVariableBlocks.forEach { block ->
                DraggableSetVariableBlock(
                    id = block.id,
                    containerWidth = contentSize,
                    containerHeight = contentSize,
                    onDelete = { blockId -> viewModel.removeBlockById(blockId) },
                    inputBlocks = block.inputBlocks,
                    viewModel = viewModel
                )
            }

            changeVariableBlocks.forEach { block ->
                val variable = block.inputBlocks.getOrNull(0)?.value as? Variable
                DraggableChangeVariableBlock(
                    id = block.id,
                    variable = variable,
                    changeSign = block.changeSign,
                    changeAmount = block.changeAmount,
                    containerWidth = contentSize,
                    containerHeight = contentSize,
                    onDelete = { blockId -> viewModel.removeBlockById(blockId) },
                    viewModel = viewModel
                )
            }

            val declareBlocks = viewModel.programBlocks.filter {
                it.type == BlockType.VARIABLE_DECLARE
            }
            val referenceBlocks = viewModel.programBlocks.filter { block ->
                block.type == BlockType.VARIABLE_REFERENCE && viewModel.findBlockContaining(block.id) == null // ❗ не вложен
            }


            declareBlocks.forEach { block ->
                val variable = block.value as? Variable
                if (variable != null) {
                    DraggableDeclareBlock(
                        id = block.id,
                        variable = variable,
                        containerWidth = contentSize,
                        containerHeight = contentSize,
                        onDelete = { blockId -> viewModel.removeBlockById(blockId) })
                }
            }

            val mathBlocks = viewModel.programBlocks.filter {
                it.type in listOf(
                    BlockType.MATH_ADD,
                    BlockType.MATH_SUBTRACT,
                    BlockType.MATH_MULTIPLY,
                    BlockType.MATH_DIVIDE
                ) && viewModel.findBlockContaining(it.id) == null
            }

            mathBlocks.forEach { block ->
                DraggableMathBlock(
                    id = block.id,
                    type = block.type,
                    inputBlocks = block.inputBlocks,
                    containerWidth = contentSize,
                    containerHeight = contentSize,
                    onDelete = { viewModel.removeBlockById(block.id) },
                    viewModel = viewModel
                )
            }

            referenceBlocks.forEach { block ->
                val variable = block.value as? Variable
                if (variable != null) {
                    DraggableReferenceBlock(
                        id = block.id, variable = variable, viewModel = viewModel
                    )
                }
            }
            // соединения
            if (viewModel.shouldDrawConnections.value) {
                Canvas(modifier = Modifier.fillMaxSize()) {
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

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .background(
                    Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "X = %.0f; Y = %.0f".format(offset.x, offset.y),
                fontSize = 12.sp,
                color = Color.Black
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { zoomIn() },
                enabled = zoomCount < maxZoom,
                modifier = Modifier.size(20.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Zoom in",
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = { zoomOut() },
                enabled = zoomCount > 0,
                modifier = Modifier.size(20.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_remove_24),
                    contentDescription = "Zoom out",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(
    showBackground = true, widthDp = 360, heightDp = 330, name = "WorkspaceCanvas Preview"
)
@Composable
fun WorkspaceCanvasPreview() {
    val mockViewModel = VariableViewModel()
    WorkspaceCanvas(
        viewModel = mockViewModel, onSizeChanged = { _, _ -> })
}
