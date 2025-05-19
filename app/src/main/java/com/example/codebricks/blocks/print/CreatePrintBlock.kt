package com.example.codebricks.blocks.print

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.viewmodel.VariableViewModel


@Composable
fun CreatePrintBlock(viewModel: VariableViewModel) {
    val showDialog = remember { mutableStateOf(false) }

    Column {
        Button(
            onClick = { showDialog.value = true },
            modifier = Modifier.padding(4.dp)
        ) {
            Text("Create Print Block", fontSize = 12.sp)
        }

        if (showDialog.value && viewModel.variables.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Select variable to print") },
                text = {
                    Column {
                        viewModel.variables.forEach { variable ->
                            Button(
                                onClick = {
                                    viewModel.declarePrintBlock(variable)
                                    showDialog.value = false
                                },
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(variable.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
