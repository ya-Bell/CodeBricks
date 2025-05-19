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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Text("Create Variable", fontSize = 12.sp)
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Declare Variable") },
                text = {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { isMultiple.value = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isMultiple.value) Color.Gray else Color.LightGray),
                                modifier = Modifier.padding(4.dp).weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Single", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isMultiple.value = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isMultiple.value) Color.Gray else Color.LightGray),
                                modifier = Modifier.padding(4.dp).weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Multiple", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isMultiple.value) {
                            OutlinedTextField(
                                value = names.value,
                                onValueChange = { names.value = it },
                                label = { Text("Variable Names (comma separated)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = value.value,
                                onValueChange = { value.value = it },
                                label = { Text("Variable Values (comma separated)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = names.value,
                                onValueChange = { names.value = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = value.value,
                                onValueChange = { value.value = it },
                                label = { Text("Initial Value") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Text("Select Type", fontWeight = FontWeight.Bold)
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
                                    Text("int")
                                }
                                Button(
                                    onClick = { type.value = "bool" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type.value == "bool") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("bool")
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
                                    Text("string")
                                }
                                Button(
                                    onClick = { type.value = "double" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type.value == "double") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("double")
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
                        Text("Create", color = Color.White)
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


