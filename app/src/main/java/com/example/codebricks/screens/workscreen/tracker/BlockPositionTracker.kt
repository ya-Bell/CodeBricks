package com.example.codebricks.screens.workscreen.tracker

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset

object BlockPositionTracker {
    private val blockPositions = mutableMapOf<String, Offset>()
    val redrawTrigger = mutableStateOf(0)

    fun updateBlockPosition(id: String, position: Offset) {
        println("📌 update position for $id = $position")
        blockPositions[id] = position
        redrawTrigger.value++ // форсим перерисовку
    }
    fun clear() {
        blockPositions.clear()
        redrawTrigger.value++
    }

    fun getPosition(id: String): Offset? = blockPositions[id]
}