package com.example.codebricks.logicblocks

import androidx.compose.ui.geometry.Offset
import com.example.codebricks.inputoutput.OutputBlock
import java.util.UUID

data class IfBlock(
    val id: UUID = UUID.randomUUID(),
    val condition: String = "",
    val innerBlocks: List<OutputBlock> = emptyList(),
    val offset: Offset = Offset.Zero
)
