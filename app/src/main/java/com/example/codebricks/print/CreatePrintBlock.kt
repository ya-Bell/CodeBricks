package com.example.codebricks.print

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
fun CreatePrintBlock(viewModel: VariableViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    val selectedVariable = remember { mutableStateOf<Variable?>(null) }

    Column {
        Button(
            onClick = {
                showDialog.value = true
            },
            modifier = Modifier.padding(4.dp)
        ) {
            Text(stringResource(id = R.string.create_print_block), fontSize = 12.sp)
        }

        if (viewModel.variables.isNotEmpty() && showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = {
                    Text(stringResource(id = R.string.select_variable_for_print), fontSize = 16.sp)
                },
                text = {
                    Column {
                        viewModel.variables.forEach { variable ->
                            Button(
                                onClick = {
                                    selectedVariable.value = variable
                                    showDialog.value = false
                                    viewModel.declarePrintBlock(variable)
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
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}
