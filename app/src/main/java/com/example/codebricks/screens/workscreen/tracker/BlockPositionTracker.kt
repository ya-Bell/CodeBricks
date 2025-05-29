package com.example.codebricks.screens.workscreen.tracker

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset

object BlockPositionTracker {
    var canvasScale = 1f
    var canvasOffset = Offset.Zero
    private val blockPositions = mutableMapOf<String, Offset>()
    val redrawTrigger = mutableIntStateOf(0)

    fun updateBlockPosition(id: String, position: Offset) {
        val scaledPosition = Offset(
            position.x / canvasScale - canvasOffset.x / canvasScale,
            position.y / canvasScale - canvasOffset.y / canvasScale
        )
        
        // Проверяем, действительно ли позиция изменилась
        val currentPosition = blockPositions[id]
        if (currentPosition != scaledPosition) {
            blockPositions[id] = scaledPosition
            redrawTrigger.intValue++
        }
    }

    fun clear() {
        blockPositions.clear()
        redrawTrigger.intValue++
    }

    fun getPosition(id: String): Offset {
        // Возвращаем позицию с учетом масштаба и смещения канваса
        val basePosition = blockPositions[id] ?: Offset.Zero
        return Offset(
            (basePosition.x + canvasOffset.x / canvasScale) * canvasScale,
            (basePosition.y + canvasOffset.y / canvasScale) * canvasScale
        )
    }

    private val blockSizes = mutableMapOf<String, Pair<Float, Float>>()

    fun setBlockSize(id: String, width: Float, height: Float) {
        blockSizes[id] = width to height
    }

//    fun getBlockWidth(id: String): Float? = blockSizes[id]?.first
}