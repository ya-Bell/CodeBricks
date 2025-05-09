package com.example.codebricks.inputoutput

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.codebricks.R

@Composable
fun InputDialog(
    variableNames: List<String>,
    onConfirm: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var values by remember { mutableStateOf(variableNames.associateWith { "" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_variable_values)) },
        text = {
            Column {
                variableNames.forEach { name ->
                    OutlinedTextField(
                        value = values[name] ?: "",
                        onValueChange = { newValue ->
                            values = values.toMutableMap().apply { put(name, newValue) }
                        },
                        label = { Text(name) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intValues = values.mapValues { it.value.toIntOrNull() ?: 0 }
                    onConfirm(intValues)
                }
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
