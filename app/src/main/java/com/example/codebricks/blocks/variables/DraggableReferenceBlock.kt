package com.example.codebricks.blocks.variables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker
import com.example.codebricks.screens.workscreen.tracker.BlockSlotTracker
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableReferenceBlock(
    id: String,
    variable: Variable,
    containerWidth: Float,
    containerHeight: Float,
    viewModel: VariableViewModel
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val animOffset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // обновление позиции в трекере
    LaunchedEffect(animOffset.value) {
        BlockPositionTracker.updateBlockPosition(id, animOffset.value)
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords -> layoutCoordinates = coords }
            .offset {
                IntOffset(
                    animOffset.value.x.roundToInt(),
                    animOffset.value.y.roundToInt()
                )
            }
            .requiredSize(60.dp, 24.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFEEEEEE))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            animOffset.snapTo(animOffset.value + dragAmount)
                        }
                    },
                    onDragEnd = {
                        val coords = layoutCoordinates
                        if (coords != null) {
                            val halfW = with(density) { 60.dp.toPx() } / 2f
                            val halfH = with(density) { 24.dp.toPx() } / 2f
                            val localCenter = animOffset.value + Offset(halfW, halfH)
                            val windowCenter = coords.localToWindow(localCenter)

                            val slotBounds: Rect? = BlockSlotTracker.getSlotBounds(id, 1)
                            if (slotBounds != null && slotBounds.contains(windowCenter)) {
                                val slotCenter = Offset(
                                    (slotBounds.left + slotBounds.right) / 2f,
                                    (slotBounds.top + slotBounds.bottom) / 2f
                                )
                                val snappedLocal = coords
                                    .windowToLocal(slotCenter) - Offset(halfW, halfH)

                                scope.launch {
                                    animOffset.animateTo(
                                        targetValue = snappedLocal,
                                        animationSpec = tween(durationMillis = 200)
                                    )
                                    viewModel.tryInsertReferenceBlock(slotCenter, id)
                                }
                            } else {
                                viewModel.tryInsertReferenceBlock(windowCenter, id)
                            }
                        }
                    }
                )
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