package com.example.codebricks.blocks.variables

import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.LaunchedEffect
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
    val errorMessage = remember { mutableStateOf("") }
    val isNameError = remember { mutableStateOf(false) }
    val isValueError = remember { mutableStateOf(false) }
    val isSubmitDisabled = remember { mutableStateOf(true) }

    val selectedBool = remember { mutableStateOf("true") }

    LaunchedEffect(isMultiple.value) {
        names.value = ""
        value.value = ""
        errorMessage.value = ""
        isNameError.value = false
        isValueError.value = false
        isSubmitDisabled.value = true
    }

    LaunchedEffect(showDialog.value) {
        if (showDialog.value) {
            names.value = ""
            value.value = ""
            errorMessage.value = ""
            isNameError.value = false
            isValueError.value = false
            isSubmitDisabled.value = true
        }
    }

    fun validateInputs() {
        errorMessage.value = ""
        isNameError.value = false
        isValueError.value = false

        if (names.value.isEmpty()) {
            errorMessage.value = "Please provide a variable name."
            isNameError.value = true
            isSubmitDisabled.value = true
            return
        }

        val namesList = names.value.split(",").map { it.trim() }
        namesList.forEach { name ->
            if (viewModel.isVariableAlreadyDeclared(name)) {
                errorMessage.value = "Variable '$name' already exists."
                isNameError.value = true
                isSubmitDisabled.value = true
                return
            }
        }

        if (namesList.size != namesList.distinct().size) {
            errorMessage.value = "Variable names must be unique."
            isNameError.value = true
            isSubmitDisabled.value = true
            return
        }

        namesList.forEach { name ->
            if (!name.matches(Regex("[a-zA-Z][a-zA-Z0-9]*"))) {
                errorMessage.value = "Invalid variable name: $name"
                isNameError.value = true
                isSubmitDisabled.value = true
                return
            }
        }

        if (!isMultiple.value && names.value.contains(",")) {
            errorMessage.value = "Single mode doesn't allow commas."
            isNameError.value = true
            isSubmitDisabled.value = true
            return
        }

        if (isMultiple.value) {
            val valuesList = value.value.split(",").map { it.trim() }

            valuesList.forEachIndexed { index, value ->
                val parsedValue = when (type.value) {
                    "int" -> value.toIntOrNull() ?: 0
                    "bool" -> value.toBooleanStrictOrNull() ?: false
                    "double" -> value.toDoubleOrNull() ?: 0.0
                    else -> value
                }

                if (parsedValue == null) {
                    errorMessage.value = "Invalid value format for ${namesList[index]}"
                    isValueError.value = true
                    isSubmitDisabled.value = true
                    return
                }
            }
        } else {

            if (!names.value.matches(Regex("[a-zA-Z][a-zA-Z0-9]*"))) {
                errorMessage.value = "Invalid variable name."
                isNameError.value = true
                isSubmitDisabled.value = true
                return
            }

            if (type.value == "int") {
                if (value.value.toDoubleOrNull() != null && value.value.contains(".")) {
                    errorMessage.value = "Invalid value format. Please enter an integer (e.g. 5)."
                    isValueError.value = true
                    isSubmitDisabled.value = true
                    return
                }
            }

            val parsedValue = when (type.value) {
                "int" -> value.value.toIntOrNull() ?: 0
                "bool" -> value.value.toBooleanStrictOrNull() ?: false
                "double" -> value.value.toDoubleOrNull() ?: 0.0
                else -> value.value
            }

            if (parsedValue == null) {
                errorMessage.value = "Invalid value format."
                isValueError.value = true
                isSubmitDisabled.value = true
                return
            }
        }

        isSubmitDisabled.value = false
    }

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

                        if (errorMessage.value.isNotEmpty()) {
                            Text(text = errorMessage.value, color = Color.Red)
                        }

                        OutlinedTextField(
                            value = names.value,
                            onValueChange = { names.value = it; validateInputs() },
                            label = { Text(text = stringResource(id = R.string.name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            isError = isNameError.value
                        )

                        OutlinedTextField(
                            value = value.value,
                            onValueChange = { value.value = it; validateInputs() },
                            label = { Text(text = stringResource(id = R.string.initial_value)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            isError = isValueError.value
                        )

                        Text(text = stringResource(id = R.string.select_type), fontWeight = FontWeight.Bold)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { type.value = "int" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "int") Color.Gray else Color.LightGray),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.integers))
                                }
                                Button(
                                    onClick = { type.value = "bool" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "bool") Color.Gray else Color.LightGray),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.bool))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { type.value = "string" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "string") Color.Gray else Color.LightGray),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.string))
                                }
                                Button(
                                    onClick = { type.value = "double" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "double") Color.Gray else Color.LightGray),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.doubles))
                                }
                            }
                            if (type.value == "bool") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            if (isMultiple.value) {
                                                value.value = if (value.value.isEmpty()) "true" else "${value.value}, true"
                                            } else {
                                                selectedBool.value = "true"
                                                value.value = "true"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedBool.value == "true") Color(0xFF4CAF50) else Color.Gray
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .weight(1f)
                                            .animateContentSize()
                                    ) {
                                        Text(
                                            text = "True",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (isMultiple.value) {
                                                value.value = if (value.value.isEmpty()) "false" else "${value.value}, false"
                                            } else {
                                                selectedBool.value = "false"
                                                value.value = "false"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedBool.value == "false") Color(0xFFf44336) else Color.Gray
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .weight(1f)
                                            .animateContentSize()
                                    ) {
                                        Text(
                                            text = "False",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!isSubmitDisabled.value) {
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
                                            "bool" -> valuesFinal[index].toBooleanStrictOrNull() ?: false
                                            "double" -> valuesFinal[index].toDoubleOrNull() ?: 0.0
                                            else -> valuesFinal[index]
                                        }
                                        viewModel.declareVariable(name, parsedValue, type.value)
                                    }
                                } else {
                                    val parsedValue: Any = when (type.value) {
                                        "int" -> value.value.toIntOrNull() ?: 0
                                        "bool" -> selectedBool.value.toBooleanStrictOrNull() ?: false
                                        "double" -> value.value.toDoubleOrNull() ?: 0.0
                                        else -> value.value
                                    }
                                    viewModel.declareVariable(names.value, parsedValue, type.value)
                                }

                                names.value = ""
                                value.value = ""
                                showDialog.value = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !isSubmitDisabled.value
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
