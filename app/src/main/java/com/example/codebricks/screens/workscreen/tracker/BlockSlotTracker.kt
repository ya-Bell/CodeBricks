package com.example.codebricks.screens.workscreen.tracker

//import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

object BlockSlotTracker {
    const val MAGNETIC_PADDING = 30f

    private val slotBounds = mutableMapOf<Pair<String, Int>, Rect>()

    fun setSlotBounds(blockId: String, slotIndex: Int, bounds: Rect) {
        slotBounds[blockId to slotIndex] = bounds
    }

//    fun getSlotCenter(blockId: String, slotIndex: Int): Offset? =
//        slotBounds[blockId to slotIndex]?.let { rect ->
//            Offset(
//                (rect.left + rect.right)  / 2f,
//                (rect.top  + rect.bottom) / 2f
//            )
//        }

    fun getSlotBounds(blockId: String, slotIndex: Int): Rect? {
        return slotBounds[blockId to slotIndex]
    }

//    fun clear() {
//        slotBounds.clear()
//    }

    data class SlotInfo(
        val blockId: String, val slotIndex: Int, val bounds: Rect
    )

    fun getAllSlots(): List<SlotInfo> {
        return slotBounds.map { (key, rect) ->
            val (blockId, slotIndex) = key
            SlotInfo(blockId, slotIndex, rect)
        }
    }

}