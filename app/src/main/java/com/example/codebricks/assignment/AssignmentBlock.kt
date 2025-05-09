package com.example.codebricks.assignment

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
    vars: Map<String, Int>,
    canDelete: Boolean
) {
    var offset by remember { mutableStateOf(block.offset) }
    var variableName by remember { mutableStateOf(block.variableName) }
    var expression by remember { mutableStateOf(block.expression) }
    var errorCode by remember { mutableStateOf<String?>(null) }

    val errorExprMsg = stringResource(R.string.error_expression)
    val confirmText = stringResource(R.string.confirm)
    val deleteBlockDesc = stringResource(R.string.delete_block)
    val assignmentOperatorText = stringResource(R.string.assignment_operator)
    val variableNameLabel = stringResource(R.string.variable_name)
    val arithmeticExpressionLabel = stringResource(R.string.arithmetic_expression)
    val exampleExpressionPlaceholder = stringResource(R.string.example_expression)

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
                    1.dp,
                    if (errorCode != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
        ) {
            Box {
                IconButton(
                    onClick = { onDelete(block.id) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = deleteBlockDesc)
                }
                Column(Modifier.padding(16.dp)) {
                    Text(assignmentOperatorText, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = variableName,
                        onValueChange = {
                            variableName = it
                            onUpdate(block.copy(variableName = it))
                        },
                        label = { Text(variableNameLabel) },
                        singleLine = canDelete,
                        isError = errorCode != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expression,
                        onValueChange = {
                            expression = it
                            onUpdate(block.copy(expression = it))
                        },
                        label = { Text(arithmeticExpressionLabel) },
                        placeholder = { Text(exampleExpressionPlaceholder) },
                        singleLine = canDelete,
                        isError = errorCode != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    if (errorCode != null) {
                        Text(errorCode!!, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            try {
                                val value = ExpressionEvaluator.evaluate(expression, vars)
                                VariableManager.assign(variableName, value)
                                errorCode = null
                            } catch (_: Exception) {
                                errorCode = errorExprMsg
                            }
                        },
                        enabled = variableName.isNotBlank() && expression.isNotBlank()
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
