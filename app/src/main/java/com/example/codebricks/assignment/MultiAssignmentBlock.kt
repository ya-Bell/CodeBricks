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

data class MultiAssignmentBlock(
    val id: UUID = UUID.randomUUID(),
    val variableNames: String = "",
    val expressions: String = "",
    val error: String? = null,
    val offset: Offset = Offset.Zero
)

@Composable
fun DraggableMultiAssignmentBlock(
    block: MultiAssignmentBlock,
    onUpdate: (MultiAssignmentBlock) -> Unit,
    onDelete: (UUID) -> Unit,
    vars: Map<String, Int>,
    canDelete: Boolean
) {
    var offset by remember { mutableStateOf(block.offset) }
    var variableNames by remember { mutableStateOf(block.variableNames) }
    var expressions by remember { mutableStateOf(block.expressions) }
    var error by remember { mutableStateOf<String?>(null) }

    val errorCountMsg = stringResource(R.string.multi_assignment_error_count)
    val errorExprMsg = stringResource(R.string.multi_assignment_error_expr)
    val confirmText = stringResource(R.string.confirm)
    val deleteBlockDesc = stringResource(R.string.delete_block)
    val multiAssignOperatorText = stringResource(R.string.multi_assignment_operator)
    val varNamesLabel = stringResource(R.string.multi_assignment_varnames)
    val exprsLabel = stringResource(R.string.multi_assignment_exprs)
    val exampleNamesPlaceholder = stringResource(R.string.example_names)

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
                .width(340.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
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
                    Text(multiAssignOperatorText, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = variableNames,
                        onValueChange = {
                            variableNames = it
                            onUpdate(block.copy(variableNames = it))
                        },
                        label = { Text(varNamesLabel) },
                        placeholder = { Text(exampleNamesPlaceholder) },
                        singleLine = canDelete,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expressions,
                        onValueChange = {
                            expressions = it
                            onUpdate(block.copy(expressions = it))
                        },
                        label = { Text(exprsLabel) },
                        placeholder = { Text("3+2\n4+6\na+b") },
                        modifier = Modifier.fillMaxWidth().height(96.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    if (error != null) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            val names = variableNames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val exprs = expressions.lines().map { it.trim() }.filter { it.isNotEmpty() }
                            if (names.size != exprs.size) {
                                error = errorCountMsg
                            } else {
                                val tempVars = vars.toMutableMap()
                                try {
                                    for (i in names.indices) {
                                        val value = ExpressionEvaluator.evaluate(exprs[i], tempVars)
                                        tempVars[names[i]] = value
                                    }
                                    for (i in names.indices) {
                                        VariableManager.assign(names[i], tempVars[names[i]] ?: 0)
                                    }
                                    error = null
                                } catch (_: Exception) {
                                    error = errorExprMsg
                                }
                            }
                        },
                        enabled = variableNames.isNotBlank() && expressions.isNotBlank()
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
