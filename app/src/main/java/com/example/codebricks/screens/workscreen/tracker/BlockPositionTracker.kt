package com.example.codebricks.screens.workscreen.tracker

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset

object BlockPositionTracker {
    var canvasScale = 1f
    var canvasOffset = Offset.Zero
    private val blockPositions = mutableMapOf<String, Offset>()
    val redrawTrigger = mutableIntStateOf(0)

    fun updateBlockPosition(id: String, position: Offset) {
        // Сначала преобразуем позицию в абсолютные координаты канваса
        val absolutePosition = Offset(
            position.x - canvasOffset.x,
            position.y - canvasOffset.y
        )

        // Затем масштабируем
        val scaledPosition = Offset(
            absolutePosition.x / canvasScale,
            absolutePosition.y / canvasScale
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

    fun getPosition(id: String): Offset? {
        val basePosition = blockPositions[id] ?: return null

        // Сначала применяем масштаб
        val scaledPosition = Offset(
            basePosition.x * canvasScale,
            basePosition.y * canvasScale
        )

        // Затем добавляем смещение канваса
        return Offset(
            scaledPosition.x + canvasOffset.x,
            scaledPosition.y + canvasOffset.y
        )
    }

    fun getRawPosition(id: String): Offset? {
        return blockPositions[id]
    }

    private val blockSizes = mutableMapOf<String, Pair<Float, Float>>()

    fun setBlockSize(id: String, width: Float, height: Float) {
        blockSizes[id] = width to height
    }

//    fun getBlockWidth(id: String): Float? = blockSizes[id]?.first
}