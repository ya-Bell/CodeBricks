@file:Suppress("NAME_SHADOWING")

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
//    containerWidth: Float,
//    containerHeight: Float,
    viewModel: VariableViewModel
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val animOffset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }


    val isInserted = viewModel.findBlockContaining(id) != null


    val redrawTrigger = BlockPositionTracker.redrawTrigger.value

    val parent = viewModel.findBlockContaining(id)
    val slotBounds = BlockSlotTracker.getSlotBounds(parent?.id ?: "", 1)
    slotBounds?.let {
        Offset(it.left, it.top)
    } ?: Offset.Zero
    val localOffset = remember { mutableStateOf(Offset.Zero) }


    // обновление позиции блока
    LaunchedEffect(id, isInserted, redrawTrigger) {
        if (isInserted && layoutCoordinates != null) {
            val slotBounds = BlockSlotTracker.getSlotBounds(parent?.id ?: "", 1)
            val windowOffset = slotBounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
            localOffset.value = layoutCoordinates!!.windowToLocal(windowOffset)
        }
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                layoutCoordinates = coords

                val widthPx = coords.size.width.toFloat()
                val heightPx = coords.size.height.toFloat()
                BlockPositionTracker.setBlockSize(id, widthPx, heightPx)

                // пересчёт localOffset только если вставлен
                if (isInserted) {
                    val slotBounds = BlockSlotTracker.getSlotBounds(parent?.id ?: "", 1)
                    val windowOffset = slotBounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
                    localOffset.value = coords.windowToLocal(windowOffset)
                }
            }
            .offset {
                val offsetToUse = if (isInserted) localOffset.value else animOffset.value
                IntOffset(offsetToUse.x.roundToInt(), offsetToUse.y.roundToInt())
            }
            .requiredSize(60.dp, 24.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFEEEEEE))
            .pointerInput(id, isInserted) {
                detectDragGestures(
                    onDragStart = {
                        if (isInserted) {
                            viewModel.removeReferenceFromParent(id)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            animOffset.snapTo(animOffset.value + dragAmount)
                        }
                    },
                    onDragEnd = {
                        val coords = layoutCoordinates ?: return@detectDragGestures

                        val halfW = with(density) { 60.dp.toPx() } / 2f
                        val halfH = with(density) { 24.dp.toPx() } / 2f
                        val localCenter = animOffset.value + Offset(halfW, halfH)
                        val windowCenter = coords.localToWindow(localCenter)

                        val matchedSlot = BlockSlotTracker.getAllSlots().find { (_, _, bounds) ->
                            val magneticPadding = 20f
                            val expandedBounds = Rect(
                                left = bounds.left - magneticPadding,
                                top = bounds.top - magneticPadding,
                                right = bounds.right + magneticPadding,
                                bottom = bounds.bottom + magneticPadding
                            )
                            expandedBounds.contains(windowCenter)
                        }

                        if (matchedSlot != null) {
                            val slotCenter = Offset(
                                (matchedSlot.bounds.left + matchedSlot.bounds.right) / 2f,
                                (matchedSlot.bounds.top + matchedSlot.bounds.bottom) / 2f
                            )
                            val snappedLocal =
                                coords.windowToLocal(slotCenter) - Offset(halfW, halfH)

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
