package com.example.codebricks.screens.workscreen.tracker

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset

object BlockPositionTracker {
    private val blockPositions = mutableMapOf<String, Offset>()
    val redrawTrigger = mutableStateOf(0)

    fun updateBlockPosition(id: String, position: Offset) {
        blockPositions[id] = position
        redrawTrigger.value++
    }
    fun clear() {
        blockPositions.clear()
        redrawTrigger.value++
    }

    fun getPosition(id: String): Offset? = blockPositions[id]

    private val blockSizes = mutableMapOf<String, Pair<Float, Float>>()

    fun setBlockSize(id: String, width: Float, height: Float) {
        blockSizes[id] = width to height
    }

    fun getBlockWidth(id: String): Float? = blockSizes[id]?.first
}