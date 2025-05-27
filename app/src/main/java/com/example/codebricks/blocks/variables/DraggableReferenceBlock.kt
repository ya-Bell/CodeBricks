@file:Suppress("NAME_SHADOWING")

package com.example.codebricks.blocks.variables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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

    val blockModifier = if (isInserted) {
        Modifier
            .wrapContentWidth()
            .heightIn(min = 32.dp)
    } else {
        Modifier
            .wrapContentWidth()
            .height(32.dp)
    }


    val redrawTrigger = BlockPositionTracker.redrawTrigger.value

    val parent = viewModel.findBlockContaining(id)
    val localOffset = remember { mutableStateOf(Offset.Zero) }


    // обновление позиции блока
    LaunchedEffect(id, isInserted, redrawTrigger) {
        if (isInserted && layoutCoordinates != null) {
            val parent = viewModel.findBlockContaining(id)
            val slot = BlockSlotTracker.getAllSlots().find {
                it.blockId == parent?.id &&
                        parent?.inputBlocks?.getOrNull(it.slotIndex)?.id == id
            }
            val windowOffset = slot?.bounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
            localOffset.value = layoutCoordinates!!.windowToLocal(windowOffset)
        }
    }

    Box(
        modifier = blockModifier
            .defaultMinSize(minWidth = 30.dp)
            .onGloballyPositioned { coords ->
                layoutCoordinates = coords

                val widthPx = coords.size.width.toFloat()
                val heightPx = coords.size.height.toFloat()
                BlockPositionTracker.setBlockSize(id, widthPx, heightPx)

                if (isInserted) {
                    val parent = viewModel.findBlockContaining(id)
                    val slot = BlockSlotTracker.getAllSlots().find {
                        it.blockId == parent?.id &&
                                parent?.inputBlocks?.getOrNull(it.slotIndex)?.id == id
                    }
                    val windowOffset = slot?.bounds?.let { Offset(it.left, it.top) } ?: Offset.Zero
                    localOffset.value = coords.windowToLocal(windowOffset)
                }
            }
            .offset {
                val offsetToUse = if (isInserted) localOffset.value else animOffset.value
                IntOffset(offsetToUse.x.roundToInt(), offsetToUse.y.roundToInt())
            }
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isInserted) 0.dp else 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color(0xFFEEEEEE))
            .zIndex(if (isInserted) 0f else 1f)
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
                                viewModel.tryInsertIntoSlot(windowCenter, id)
                            }
                        } else {
                            viewModel.tryInsertIntoSlot(windowCenter, id)
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
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DraggableReferenceBlockPreview() {
    val viewModel = VariableViewModel()
    val variable = Variable(name = "e", value = 10, type = "int")

    DraggableReferenceBlock(
        id = "ref-preview-id",
        variable = variable,
        viewModel = viewModel
    )
}
