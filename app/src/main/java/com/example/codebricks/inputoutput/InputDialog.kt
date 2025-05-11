package com.example.codebricks.inputoutput

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codebricks.R

@Composable
fun InputDialog(
    variableNames: List<String>,
    onConfirm: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var values by remember { mutableStateOf(variableNames.associateWith { "" }) }
    val scrollState = rememberScrollState()

    val hasErrors = values.any { it.value.toIntOrNull() == null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_variable_values)) },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                variableNames.forEach { name ->
                    val input = values[name] ?: ""
                    val isError = input.isNotEmpty() && input.toIntOrNull() == null

                    OutlinedTextField(
                        value = input,
                        onValueChange = { newValue ->
                            values = values.toMutableMap().apply { put(name, newValue) }
                        },
                        label = { Text(name) },
                        placeholder = { Text(stringResource(R.string.placeholder_number)) },
                        isError = isError,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    if (isError) {
                        Text(
                            text = stringResource(R.string.error_expression),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intValues = values.mapValues { it.value.toIntOrNull() ?: 0 }
                    onConfirm(intValues)
                },
                enabled = !hasErrors && values.isNotEmpty()
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
