package com.example.codebricks.screens.workscreen.tracker

//import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

object BlockSlotTracker {
    const val MAGNETIC_PADDING = 30f

    private val slotBounds = mutableMapOf<Pair<String, Int>, Rect>()

    fun setSlotBounds(blockId: String, slotIndex: Int, bounds: Rect) {
        // Учитываем масштаб канваса при сохранении границ
        val scaledBounds = Rect(
            left = bounds.left / BlockPositionTracker.canvasScale,
            top = bounds.top / BlockPositionTracker.canvasScale,
            right = bounds.right / BlockPositionTracker.canvasScale,
            bottom = bounds.bottom / BlockPositionTracker.canvasScale
        )
        slotBounds[blockId to slotIndex] = scaledBounds
    }

//    fun getSlotCenter(blockId: String, slotIndex: Int): Offset? =
//        slotBounds[blockId to slotIndex]?.let { rect ->
//            Offset(
//                (rect.left + rect.right)  / 2f,
//                (rect.top  + rect.bottom) / 2f
//            )
//        }

    fun getSlotBounds(blockId: String, slotIndex: Int): Rect? {
        return slotBounds[blockId to slotIndex]?.let { bounds ->
            Rect(
                left = bounds.left * BlockPositionTracker.canvasScale,
                top = bounds.top * BlockPositionTracker.canvasScale,
                right = bounds.right * BlockPositionTracker.canvasScale,
                bottom = bounds.bottom * BlockPositionTracker.canvasScale
            )
        }
    }

    fun clear() {
        slotBounds.clear()
    }

    data class SlotInfo(
        val blockId: String, val slotIndex: Int, val bounds: Rect
    )

    fun getAllSlots(): List<SlotInfo> {
        return slotBounds.map { (key, rect) ->
            val (blockId, slotIndex) = key
            // Возвращаем слоты с масштабированными границами
            val scaledRect = Rect(
                left = rect.left * BlockPositionTracker.canvasScale,
                top = rect.top * BlockPositionTracker.canvasScale,
                right = rect.right * BlockPositionTracker.canvasScale,
                bottom = rect.bottom * BlockPositionTracker.canvasScale
            )
            SlotInfo(blockId, slotIndex, scaledRect)
        }
    }

}