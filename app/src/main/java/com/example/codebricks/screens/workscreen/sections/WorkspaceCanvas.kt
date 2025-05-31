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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.ui.theme.BackgroundCanvas
import com.example.codebricks.ui.theme.GridLine
import com.example.codebricks.ui.theme.IconRed
import com.example.codebricks.ui.theme.OverlayWhite
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextGray
import com.example.codebricks.viewmodel.RenderBlockTree
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.tree.findBlockContaining

@SuppressLint("UnusedTransitionTargetStateParameter", "ConfigurationScreenWidthHeight")
@Composable
fun WorkspaceCanvas(
    viewModel: VariableViewModel, onSizeChanged: (Float, Float) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val adaptiveHeight = screenHeight.value * 0.44f
    val canvasSize = remember { mutableStateOf(IntSize(0, 0)) }
    val contentSize = 3000f // холст 4000x4000
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoomCount by remember { mutableIntStateOf(0) }
    val maxZoom = 3
    val zoomFactor = 1.25f
    val redrawTrigger = BlockPositionTracker.redrawTrigger.value

    // Обновляем BlockPositionTracker при изменении масштаба или смещения
    LaunchedEffect(scale, offset) {
        BlockPositionTracker.canvasScale = scale
        BlockPositionTracker.canvasOffset = offset
        BlockPositionTracker.redrawTrigger.intValue++
    }

    // функции управления зумом
    fun zoomIn() {
        if (zoomCount < maxZoom) {
            scale *= zoomFactor
            zoomCount++
            offset = Offset(
                x = offset.x.coerceIn(
                    (canvasSize.value.width - contentSize * scale).coerceAtMost(0f), 0f
                ),
                y = offset.y.coerceIn(
                    (canvasSize.value.height - contentSize * scale).coerceAtMost(0f), 0f
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
                ),
                y = offset.y.coerceIn(
                    (canvasSize.value.height - contentSize * scale).coerceAtMost(0f), 0f
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(adaptiveHeight.dp)
            .background(BackgroundCanvas)
            .border(1.dp, TextGray)
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val scaledW = contentSize * scale
                    val scaledH = contentSize * scale
                    val minX = (canvasSize.value.width - scaledW).coerceAtMost(0f)
                    val minY = (canvasSize.value.height - scaledH).coerceAtMost(0f)
                    val newOffset = offset + dragAmount
                    offset = Offset(
                        x = newOffset.x.coerceIn(minX, 0f),
                        y = newOffset.y.coerceIn(minY, 0f)
                    )
                    change.consume()
                }
            }
            .onSizeChanged {
                canvasSize.value = it
                onSizeChanged(it.width.toFloat(), it.height.toFloat())
            }
    ) {
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
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // красная рамка
                drawRect(
                    color = IconRed,
                    topLeft = Offset.Zero,
                    size = Size(contentSize, contentSize),
                    style = Stroke(width = 4f)
                )
                // сетка
                val step = 40f * scale
                var x = 0f
                while (x <= contentSize) {
                    drawLine(GridLine, Offset(x, 0f), Offset(x, contentSize), 1f)
                    x += step
                }
                var y = 0f
                while (y <= contentSize) {
                    drawLine(GridLine, Offset(0f, y), Offset(contentSize, y), 1f)
                    y += step
                }
            }
            // блоки
            viewModel.programBlocks
                .filter { viewModel.findBlockContaining(it.id) == null } // только корневые
                .forEach { block ->
                    RenderBlockTree(
                        block = block,
                        viewModel = viewModel,
                        containerWidth = contentSize,
                        containerHeight = contentSize,
                        onDelete = { viewModel.removeBlockById(it) }
                    )
                }
            // соединения
            if (viewModel.shouldDrawConnections.value) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    redrawTrigger

                    for (block in viewModel.programBlocks) {
                        val from = BlockPositionTracker.getPosition(block.id)
                        val to = block.nextBlockId?.let { BlockPositionTracker.getPosition(it) }

                        if (from != null && to != null) {
                            val blockWidth = 140.dp.toPx()
                            val blockHeight = 40.dp.toPx()

                            // Точки начала и конца
                            val fromPoint =
                                from + Offset(blockWidth / 2, blockHeight)  // Внизу первого блока
                            val toPoint = to + Offset(blockWidth / 2, 0f)  // Вверху второго блока

                            // Контрольные точки для кривой Безье
                            val control1 = fromPoint + Offset(0f, 20f)  // 20px вниз от начала
                            val control2 = toPoint - Offset(0f, 20f)    // 20px вверх от конца

                            // Рисуем кривую Безье
                            drawPath(
                                androidx.compose.ui.graphics.Path().apply {
                                    moveTo(fromPoint.x, fromPoint.y)
                                    cubicTo(
                                        control1.x, control1.y,
                                        control2.x, control2.y,
                                        toPoint.x, toPoint.y
                                    )
                                },
                                color = TextBlack,
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .background(
                    OverlayWhite.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.coordinates_format, offset.x, offset.y),
                fontSize = 12.sp,
                color = TextBlack,
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { zoomIn() },
                enabled = zoomCount < maxZoom,
                modifier = Modifier.size(20.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = stringResource(id = R.string.zoom_in),
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
                    contentDescription = stringResource(id = R.string.zoom_out),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
