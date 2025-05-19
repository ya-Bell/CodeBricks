package com.example.codebricks.blocks.variables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun DeclareVariable(viewModel: VariableViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    val name = remember { mutableStateOf("") }
    val value = remember { mutableStateOf("") }
    val type = remember { mutableStateOf("int") }

    Column {
        Button(
            onClick = { showDialog.value = true },
            modifier = Modifier.padding(4.dp)
        ) {
            Text("Create Variable", fontSize = 12.sp)
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Declare Variable") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name.value,
                            onValueChange = { name.value = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = value.value,
                            onValueChange = { value.value = it },
                            label = { Text("Initial Value") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = type.value,
                            onValueChange = { type.value = it },
                            label = { Text("Type (int, string, bool, double)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val parsedValue: Any = when (type.value) {
                            "int" -> value.value.toIntOrNull() ?: 0
                            "bool" -> value.value.toBooleanStrictOrNull() ?: false
                            "double" -> value.value.toDoubleOrNull() ?: 0.0
                            else -> value.value
                        }

                        viewModel.declareVariable(name.value, parsedValue, type.value)
                        showDialog.value = false
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}