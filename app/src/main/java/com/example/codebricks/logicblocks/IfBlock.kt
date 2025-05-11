package com.example.codebricks.logicblocks

import androidx.compose.ui.geometry.Offset
import java.util.UUID

data class IfBlock(
    val id: UUID = UUID.randomUUID(),
    val condition: String = "",
    val offset: Offset = Offset.Zero
)
