package com.example.codebricks.assignment

//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.codebricks.variable_declaration.VariableManager
import java.util.UUID

data class AssignmentBlock(
    val id: UUID = UUID.randomUUID(),
    val variableName: String = "",
    val expression: String = "",
    val error: String? = null,
    val offset: Offset = Offset.Zero
)

@Composable
fun DraggableAssignmentBlock(
    block: AssignmentBlock,
    onUpdate: (AssignmentBlock) -> Unit,
    onDelete: (UUID) -> Unit,
    canDelete: Boolean,
    variables: Map<String, Int>
) {
    var offset by remember { mutableStateOf(block.offset) }
    var variableName by remember { mutableStateOf(block.variableName) }
    var expression by remember { mutableStateOf(block.expression) }
    var error by remember { mutableStateOf<String?>(null) }

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
                .border(
                    width = 1.dp,
                    color = if (error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Box {
                if (canDelete) {
                    IconButton(
                        onClick = { onDelete(block.id) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.delete_block)
                        )
                    }
                }
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.assignment_operator),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = variableName,
                        onValueChange = {
                            variableName = it
                            val validatedBlock = validateAssignmentBlock(block.copy(variableName = it), variables)
                            onUpdate(validatedBlock)
                        },
                        label = { Text(stringResource(R.string.variable_name)) },
                        singleLine = true,
                        isError = error != null || block.error == "variable_not_found",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (block.error == "variable_not_found") {
                        Text(
                            stringResource(R.string.error_variable_not_found),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expression,
                        onValueChange = {
                            expression = it
                            onUpdate(block.copy(expression = it))
                        },
                        label = { Text(stringResource(R.string.arithmetic_expression)) },
                        placeholder = { Text(stringResource(R.string.example_expression)) },
                        singleLine = true,
                        isError = error != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    val errorExpressionText = stringResource(R.string.error_expression)

                    Button(
                        onClick = {
                            try {
                                val value = evaluateExpression(expression, variables)
                                VariableManager.assign(variableName, value)
                                error = null
                            } catch (_: Exception) {
                                error = errorExpressionText
                            }
                        },
                        enabled = variableName.isNotBlank() && expression.isNotBlank() &&
                                block.error == null && variables.containsKey(variableName)
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}

fun validateAssignmentBlock(block: AssignmentBlock, variables: Map<String, Int>): AssignmentBlock {
    val errorCode = when {
        block.variableName.isNotEmpty() && !variables.containsKey(block.variableName) -> "variable_not_found"
        else -> null
    }
    return block.copy(error = errorCode)
}

fun evaluateExpression(expr: String, variables: Map<String, Int>): Int {
    val tokens = expr.split(" ").filter { it.isNotBlank() }

    if (tokens.size == 1) {
        return tokens[0].toIntOrNull() ?: variables[tokens[0]] ?: 0
    }

    if (tokens.size == 3) {
        val left = tokens[0].toIntOrNull() ?: variables[tokens[0]] ?: 0
        val op = tokens[1]
        val right = tokens[2].toIntOrNull() ?: variables[tokens[2]] ?: 0

        return when (op) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> if (right != 0) left / right else 0
            "%" -> if (right != 0) left % right else 0
            else -> 0
        }
    }

    return 0
}
