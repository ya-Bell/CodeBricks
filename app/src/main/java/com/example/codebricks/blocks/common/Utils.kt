package com.example.codebricks.blocks.common

import androidx.compose.ui.geometry.Offset
import java.util.UUID

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

fun Block.cloneWithNewId(): Block {
    return Block(
        id = UUID.randomUUID().toString(),
        type = this.type,
        value = this.value,
        changeSign = this.changeSign,
        changeAmount = this.changeAmount,
        inputBlocks = this.inputBlocks.map { it?.cloneWithNewId() }.toMutableList(),
        children = this.children.map { it.cloneWithNewId() }.toMutableList(),
        nextBlockId = this.nextBlockId,
        creationTime = System.currentTimeMillis()
    )
}
