package com.example.codebricks.variable_declaration

import com.example.codebricks.R
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import java.util.*

data class VariableDeclarationBlock(
    val id: UUID = UUID.randomUUID(),
    val variableNames: String = "",
    val error: String? = null,
    val offset: Offset = Offset.Zero
)

@Composable
fun DraggableVariableBlock(
    block: VariableDeclarationBlock,
    onUpdate: (VariableDeclarationBlock) -> Unit
) {
    var offset by remember { mutableStateOf(block.offset) }
    var text by remember { mutableStateOf(block.variableNames) }

    Box(
        modifier = Modifier
            .offset { offset.toIntOffset() }
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.declare_variables),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { newText ->
                        text = newText
                        val validatedBlock = validateVariableBlock(block.copy(variableNames = newText))
                        onUpdate(validatedBlock)
                        if (validatedBlock.error == null) {
                            val names = newText.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            VariableManager.declareVariables(names)
                        }
                    },
                    label = { Text(stringResource(R.string.variable_names)) },
                    placeholder = { Text(stringResource(R.string.example_names)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = true
                    ),
                    isError = block.error != null,
                    singleLine = true,
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
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (text.isNotEmpty()) {
                    val createdNames = text.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString()
                    Text(
                        stringResource(R.string.will_be_created, createdNames),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    stringResource(R.string.default_value),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())

fun validateVariableBlock(block: VariableDeclarationBlock): VariableDeclarationBlock {
    val names = block.variableNames.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val errorCode = when {
        names.isEmpty() -> "min_one_var"
        names.any { !it.matches(Regex("^[\\p{L}_][\\p{L}0-9_]*$")) } -> "invalid_chars"
        names.groupBy { it }.any { it.value.size > 1 } -> "duplicates"
        else -> null
    }
    return block.copy(error = errorCode)
}
