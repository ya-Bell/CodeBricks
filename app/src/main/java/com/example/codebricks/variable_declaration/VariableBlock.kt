package com.example.codebricks.variable_declaration

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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

data class VariableDeclarationBlock(
    val id: UUID = UUID.randomUUID(),
    val variableNames: String = "",
    val error: String? = null,
    val offset: Offset = Offset.Zero
)

@Composable
fun DraggableVariableBlock(
    block: VariableDeclarationBlock,
    onUpdate: (VariableDeclarationBlock) -> Unit,
    onDelete: (UUID) -> Unit,
    canDelete: Boolean,
) {
    var offset by remember { mutableStateOf(block.offset) }
    var text by remember { mutableStateOf(block.variableNames) }

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
                .border(
                    width = 1.dp,
                    color = if (block.error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Box {
                IconButton(
                    onClick = { onDelete(block.id) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete_block))
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.declare_variables), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { newText ->
                            text = newText
                            val validatedBlock = validateVariableBlock(block.copy(variableNames = newText))
                            onUpdate(validatedBlock)
                        },
                        label = { Text(stringResource(R.string.variable_names)) },
                        placeholder = { Text(stringResource(R.string.example_names)) },
                        isError = block.error != null,
                        singleLine = canDelete,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (block.error != null) {
                        val names = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val errorText = when (block.error) {
                            "min_one_var" -> stringResource(R.string.error_min_one_var)
                            "invalid_chars" -> stringResource(R.string.error_invalid_chars)
                            "duplicates" -> {
                                val duplicates = names.groupBy { it }.filter { it.value.size > 1 }.keys.joinToString()
                                stringResource(R.string.error_duplicates, duplicates)
                            }
                            else -> block.error
                        }
                        Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                    } else if (text.isNotEmpty()) {
                        val createdNames = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }.joinToString()
                        Text(stringResource(R.string.will_be_created, createdNames), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                    Text(stringResource(R.string.default_value), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                val names = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                VariableManager.declare(names)
                            },
                            enabled = block.error == null && text.isNotBlank()
                        ) { Text(stringResource(R.string.confirm)) }
                        Button(onClick = { VariableManager.clear() }) {
                            Text(stringResource(R.string.clear_variables))
                        }
                    }
                }
            }
        }
    }
}
