package com.example.codebricks.inputoutput

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codebricks.R
import java.util.UUID

data class OutputBlock(
    val id: UUID = UUID.randomUUID(),
    val variableNames: String = "",
    val offset: Offset = Offset.Zero
)

@Composable
fun DraggableOutputBlock(
    block: OutputBlock,
    onUpdate: (OutputBlock) -> Unit,
    onDelete: (UUID) -> Unit,
    vars: Map<String, Int>,
    canDelete: Boolean
) {
    var offset by remember { mutableStateOf(block.offset) }
    var variableNames by remember { mutableStateOf(block.variableNames) }
    var output by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offset += dragAmount
                    onUpdate(block.copy(offset = offset))
                }
            }
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.output_block_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    if (canDelete) {
                        IconButton(
                            onClick = {
                                variableNames = ""
                                onDelete(block.id)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.delete_block),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = variableNames,
                    onValueChange = {
                        variableNames = it
                        onUpdate(block.copy(variableNames = it))
                        output = null
                    },
                    label = { Text(stringResource(R.string.input_variables_label)) },
                    placeholder = { Text(stringResource(R.string.example_vars)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                val notDeclaredText = stringResource(R.string.variable_not_declared)

                Button(
                    onClick = {

                        val names = variableNames.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        val result = StringBuilder()

                        for (name in names) {
                            val value = vars[name]?.toString() ?: notDeclaredText
                            result.append("$name = $value\n")
                        }

                        output = result.toString().trim()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = variableNames.isNotBlank()
                ) {
                    Text(stringResource(R.string.button_show_values))
                }

                output?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
