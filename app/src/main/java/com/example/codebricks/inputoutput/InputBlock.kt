package com.example.codebricks.inputoutput

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

data class InputBlock(
    val id: UUID = UUID.randomUUID(),
    val variableNames: String = "",
    val offset: Offset = Offset.Zero
)

@Composable
fun DraggableInputBlock(
    block: InputBlock,
    onUpdate: (InputBlock) -> Unit,
    onDelete: (UUID) -> Unit,
    onInput: (List<String>) -> Unit,
    canDelete: Boolean
) {
    var offset by remember { mutableStateOf(block.offset) }
    var variableNames by remember { mutableStateOf(block.variableNames) }
    val scrollState = rememberScrollState()

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
                .width(320.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .heightIn(max = 400.dp)
                    .verticalScroll(scrollState)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    if (canDelete) {
                        IconButton(
                            onClick = { onDelete(block.id) },
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

                Text(
                    text = stringResource(R.string.input_block_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = variableNames,
                    onValueChange = {
                        variableNames = it
                        onUpdate(block.copy(variableNames = it))
                    },
                    label = { Text(stringResource(R.string.input_variables_label)) },
                    placeholder = { Text(stringResource(R.string.example_vars)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val names = variableNames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (names.isNotEmpty()) onInput(names)
                    },
                    enabled = variableNames.isNotBlank()
                ) {
                    Text(stringResource(R.string.button_enter_values))
                }
            }
        }
    }
}
