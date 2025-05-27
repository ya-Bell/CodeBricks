package com.example.codebricks.screens.workscreen.tracker

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset

object BlockPositionTracker {
    var canvasScale = 1f
    var canvasOffset = Offset.Zero
    private val blockPositions = mutableMapOf<String, Offset>()
    val redrawTrigger = mutableIntStateOf(0)

    fun updateBlockPosition(id: String, position: Offset) {
        blockPositions[id] = position
        redrawTrigger.intValue++
    }
    fun clear() {
        blockPositions.clear()
        redrawTrigger.intValue++
    }

    fun getPosition(id: String): Offset? = blockPositions[id]

    private val blockSizes = mutableMapOf<String, Pair<Float, Float>>()

    fun setBlockSize(id: String, width: Float, height: Float) {
        blockSizes[id] = width to height
    }

//    fun getBlockWidth(id: String): Float? = blockSizes[id]?.first
}