package com.example.codebricks.blocks.variables.varconvert

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.conversion.convertVariableType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertVariable(viewModel: VariableViewModel) {
    val buttonModifier = Modifier
        .padding(4.dp)
        .height(36.dp)
        .fillMaxWidth()

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFB8C00), contentColor = Color.White
    )

    val showDialog = remember { mutableStateOf(false) }
    val selectedVariable = remember { mutableStateOf("") }
    val newType = remember { mutableStateOf("int") }
    val errorMessage = remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val errorSameType = stringResource(id = R.string.error_same_type)
    val errorNoVariable = stringResource(id = R.string.error_no_variable)

    Column {
        Button(
            onClick = { showDialog.value = true },
            modifier = buttonModifier,
            colors = buttonColors,
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Text(
                text = stringResource(id = R.string.convert_variable),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (showDialog.value) {
            val currentType = viewModel.variables
                .find { it.name == selectedVariable.value }
                ?.type
                ?: ""

            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = stringResource(id = R.string.convert_variable)) },
                text = {
                    Column {
                        if (viewModel.variables.isEmpty()) {
                            Text(stringResource(id = R.string.no_variables), color = Color.Red)
                        } else {

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedVariable.value.ifEmpty { "Select variable" },
                                    onValueChange = {},
                                    label = { Text(stringResource(id = R.string.select_variable)) },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    viewModel.variables.forEach { variable ->
                                        DropdownMenuItem(
                                            text = { Text("${variable.name} (${variable.type})") },
                                            onClick = {
                                                selectedVariable.value = variable.name
                                                newType.value = "int"
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Text(stringResource(id = R.string.select_new_type), fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { newType.value = "int" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            currentType == "int" -> Color.LightGray.copy(alpha = 0.5f)
                                            newType.value == "int" -> Color.Gray
                                            else -> Color.LightGray
                                        }
                                    ),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = currentType != "int"
                                ) {
                                    Text(stringResource(id = R.string.int_type))
                                }
                                Button(
                                    onClick = { newType.value = "bool" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            currentType == "bool" -> Color.LightGray.copy(alpha = 0.5f)
                                            newType.value == "bool" -> Color.Gray
                                            else -> Color.LightGray
                                        }
                                    ),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = currentType != "bool"
                                ) {
                                    Text(stringResource(id = R.string.bool_type))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { newType.value = "string" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            currentType == "string" -> Color.LightGray.copy(alpha = 0.5f)
                                            newType.value == "string" -> Color.Gray
                                            else -> Color.LightGray
                                        }
                                    ),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = currentType != "string"
                                ) {
                                    Text(stringResource(id = R.string.string_type))
                                }
                                Button(
                                    onClick = { newType.value = "double" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            currentType == "double" -> Color.LightGray.copy(alpha = 0.5f)
                                            newType.value == "double" -> Color.Gray
                                            else -> Color.LightGray
                                        }
                                    ),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = currentType != "double"
                                ) {
                                    Text(stringResource(id = R.string.double_type))
                                }
                            }

                            if (errorMessage.value.isNotEmpty()) {
                                Text(errorMessage.value, color = Color.Red)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedVariable.value.isNotEmpty() && newType.value != currentType) {
                                viewModel.convertVariableType(
                                    selectedVariable.value,
                                    newType.value
                                )
                                showDialog.value = false
                            } else if (newType.value == currentType) {
                                errorMessage.value = errorSameType
                            } else {
                                errorMessage.value = errorNoVariable
                            }
                        },
                        enabled = selectedVariable.value.isNotEmpty() && newType.value != currentType
                    ) {
                        Text(stringResource(id = R.string.convert))
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}