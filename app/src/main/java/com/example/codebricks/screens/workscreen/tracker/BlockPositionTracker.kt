package com.example.codebricks.screens.workscreen.tracker

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset

object BlockPositionTracker {
    var canvasScale = 1f
    var canvasOffset = Offset.Zero
    private val blockPositions = mutableMapOf<String, Offset>()
    val redrawTrigger = mutableIntStateOf(0)

    fun updateBlockPosition(id: String, position: Offset) {
        // Проверяем, действительно ли позиция изменилась
        val currentPosition = blockPositions[id]
        if (currentPosition != position) {
            blockPositions[id] = position
            // Вызываем перерисовку только если позиция реально изменилась
            redrawTrigger.intValue++
        }
    }

    fun clear() {
        blockPositions.clear()
        redrawTrigger.intValue++
    }

    fun getPosition(id: String): Offset {
        // Возвращаем сохраненную позицию или дефолтную, если блок новый
        return blockPositions[id] ?: Offset.Zero
    }

    private val blockSizes = mutableMapOf<String, Pair<Float, Float>>()

    fun setBlockSize(id: String, width: Float, height: Float) {
        blockSizes[id] = width to height
    }

//    fun getBlockWidth(id: String): Float? = blockSizes[id]?.first
}