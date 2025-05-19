package com.example.codebricks.blocks.variables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun DeclareVariable(viewModel: VariableViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    val isMultiple = remember { mutableStateOf(false) }
    val names = remember { mutableStateOf("") }
    val value = remember { mutableStateOf("") }
    val type = remember { mutableStateOf("int") }

    Column {
        Button(
            onClick = { showDialog.value = true },
            modifier = Modifier.padding(4.dp)
        ) {
            Text(text = stringResource(id = R.string.create_variable), fontSize = 12.sp)
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = stringResource(id = R.string.declare_variable)) },
                text = {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { isMultiple.value = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isMultiple.value) Color.Gray else Color.LightGray),
                                modifier = Modifier.padding(4.dp).weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = stringResource(id = R.string.single), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isMultiple.value = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isMultiple.value) Color.Gray else Color.LightGray),
                                modifier = Modifier.padding(4.dp).weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = stringResource(id = R.string.multiple), fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isMultiple.value) {
                            OutlinedTextField(
                                value = names.value,
                                onValueChange = { names.value = it },
                                label = { Text(text = stringResource(id = R.string.variable_names)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = value.value,
                                onValueChange = { value.value = it },
                                label = { Text(text = stringResource(id = R.string.variable_values)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = names.value,
                                onValueChange = { names.value = it },
                                label = { Text(text = stringResource(id = R.string.name)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = value.value,
                                onValueChange = { value.value = it },
                                label = { Text(text = stringResource(id = R.string.initial_value)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Text(text = stringResource(id = R.string.select_type), fontWeight = FontWeight.Bold)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { type.value = "int" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type.value == "int") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.integers))
                                }
                                Button(
                                    onClick = { type.value = "bool" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type.value == "bool") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.bool))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { type.value = "string" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type.value == "string") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.string))
                                }
                                Button(
                                    onClick = { type.value = "double" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type.value == "double") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.doubles))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isMultiple.value) {

                                val namesList = names.value.split(",").map { it.trim() }
                                val valuesList = value.value.split(",").map { it.trim() }

                                val valuesFinal = if (valuesList.size < namesList.size) {
                                    valuesList + List(namesList.size - valuesList.size) { valuesList.last() }
                                } else {
                                    valuesList
                                }

                                namesList.forEachIndexed { index, name ->
                                    val parsedValue: Any = when (type.value) {
                                        "int" -> valuesFinal[index].toIntOrNull() ?: 0
                                        "bool" -> valuesFinal[index].toBooleanStrictOrNull() == true
                                        "double" -> valuesFinal[index].toDoubleOrNull() ?: 0.0
                                        else -> valuesFinal[index]
                                    }
                                    viewModel.declareVariable(name, parsedValue, type.value)
                                }
                            } else {
                                val parsedValue: Any = when (type.value) {
                                    "int" -> value.value.toIntOrNull() ?: 0
                                    "bool" -> value.value.toBooleanStrictOrNull() == false
                                    "double" -> value.value.toDoubleOrNull() ?: 0.0
                                    else -> value.value
                                }
                                viewModel.declareVariable(names.value, parsedValue, type.value)
                            }

                            showDialog.value = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(text = stringResource(id = R.string.create), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}
