package com.example.codebricks.logicblocks

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codebricks.R
import com.example.codebricks.inputoutput.DraggableOutputBlock
import com.example.codebricks.inputoutput.OutputBlock
import java.util.UUID

@Composable
fun DraggableIfBlock(
    block: IfBlock,
    onUpdate: (IfBlock) -> Unit,
    onDelete: (UUID) -> Unit,
    canDelete: Boolean,
    vars: Map<String, Int>
) {
    var offset by remember { mutableStateOf(block.offset) }
    var condition by remember { mutableStateOf(block.condition) }
    var result by remember { mutableStateOf<ConditionEvaluator.Result?>(null) }
    var innerBlocks by remember { mutableStateOf(block.innerBlocks) }

    val errorNoOperator = stringResource(R.string.error_no_operator)
    val errorInvalidFormat = stringResource(R.string.error_invalid_format)
    val errorVarNotFound = stringResource(R.string.error_variable_not_found2)
    val conditionTrue = stringResource(R.string.condition_true)
    val conditionFalse = stringResource(R.string.condition_false)

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
            Column(Modifier.padding(12.dp)) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.if_block_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
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

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = condition,
                    onValueChange = {
                        condition = it
                        onUpdate(block.copy(condition = it))
                        result = null
                    },
                    label = { Text(stringResource(R.string.condition_label)) },
                    placeholder = { Text(stringResource(R.string.condition_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        result = ConditionEvaluator.evaluate(
                            input = condition,
                            errorNoOperator = errorNoOperator,
                            errorInvalidFormat = errorInvalidFormat,
                            errorVarNotFound = errorVarNotFound,
                            conditionTrue = conditionTrue,
                            conditionFalse = conditionFalse
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = condition.isNotBlank()
                ) {
                    Text(stringResource(R.string.check_condition))
                }

                result?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it.text, color = it.color)
                }

                Spacer(Modifier.height(8.dp))

                if (result?.color == Color.Green) {
                    innerBlocks.forEach { innerBlock ->
                        DraggableOutputBlock(
                            block = innerBlock,
                            onUpdate = { updated ->
                                innerBlocks = innerBlocks.map {
                                    if (it.id == updated.id) updated else it
                                }
                                onUpdate(block.copy(innerBlocks = innerBlocks))
                            },
                            onDelete = { id ->
                                innerBlocks = innerBlocks.filter { it.id != id }
                                onUpdate(block.copy(innerBlocks = innerBlocks))
                            },
                            vars = vars,
                            canDelete = true
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val newBlock = OutputBlock()
                        innerBlocks = innerBlocks + newBlock
                        onUpdate(block.copy(innerBlocks = innerBlocks))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add_output_block))
                }
            }
        }
    }
}
