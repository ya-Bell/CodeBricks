package com.example.codebricks.blocks.common

import androidx.compose.ui.geometry.Offset

fun limitPosition(
    offset: Offset,
    containerWidth: Float,
    containerHeight: Float,
    blockWidth: Float,
    blockHeight: Float
): Offset {
    val clampedX = offset.x.coerceIn(0f, containerWidth - blockWidth)
    val clampedY = offset.y.coerceIn(0f, containerHeight - blockHeight)
    return Offset(clampedX, clampedY)
}
