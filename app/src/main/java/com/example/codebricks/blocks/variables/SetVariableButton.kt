package com.example.codebricks.blocks.variables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun SetVariableButton(viewModel: VariableViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    val selectedVariable = remember { mutableStateOf<Variable?>(null) }
    val newValue = remember { mutableStateOf("") }

    Button(
        onClick = { showDialog.value = true },
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = stringResource(id = R.string.set_variable), fontSize = 12.sp)
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.select_variable_and_set_value)) },
            text = {
                Column {
                    viewModel.variables.forEach { variable ->
                        Button(
                            onClick = {
                                selectedVariable.value = variable
                            },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(text = "${variable.type} ${variable.name}")
                        }
                    }

                    selectedVariable.value?.let {
                        OutlinedTextField(
                            value = newValue.value,
                            onValueChange = { newValue.value = it },
                            label = { Text(text = stringResource(id = R.string.new_value_for, it.name)) },
                            modifier = Modifier.padding(4.dp)
                        )

                        Button(
                            onClick = {
                                selectedVariable.value?.let { variable ->
                                    val updatedValue = when (variable.type) {
                                        "int" -> newValue.value.toIntOrNull() ?: 0
                                        "double" -> newValue.value.toDoubleOrNull() ?: 0.0
                                        else -> newValue.value
                                    }
                                    viewModel.declareSetVariable(variable, updatedValue)
                                }
                                showDialog.value = false
                            },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(text = stringResource(id = R.string.set_value))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog.value = false }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}
