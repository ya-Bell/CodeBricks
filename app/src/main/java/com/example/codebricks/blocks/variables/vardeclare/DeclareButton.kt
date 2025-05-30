package com.example.codebricks.blocks.variables.vardeclare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.codebricks.viewmodel.blocks.declareVariable
import com.example.codebricks.viewmodel.blocks.isVariableAlreadyDeclared

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclareVariable(viewModel: VariableViewModel) {
    val buttonModifier = Modifier
        .padding(4.dp)
        .height(36.dp)
        .fillMaxWidth()

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFB8C00), contentColor = Color.White
    )

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

            if (valuesList.size > namesList.size) {
                errorMessage.value =
                    "Too many values. Expected ${namesList.size}, but got ${valuesList.size}."
                isValueError.value = true
                isSubmitDisabled.value = true
                return
            }

            valuesList.forEachIndexed { index, valueStr ->
                val parsedValue = when (type.value) {
                    "int" -> valueStr.toIntOrNull() ?: 0
                    "bool" -> valueStr.toBooleanStrictOrNull() ?: false
                    "double" -> valueStr.toDoubleOrNull() ?: 0.0
                    "string" -> valueStr
                    else -> valueStr
                }

                if (parsedValue == null) {
                    errorMessage.value =
                        "Invalid value format for ${namesList.getOrNull(index) ?: "?"}"
                    isValueError.value = true
                    isSubmitDisabled.value = true
                    return
                }

                if (type.value == "string" && parsedValue.toString().trim().isEmpty()) {
                    errorMessage.value = "String value cannot be empty."
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

            if (type.value == "string" && value.value.trim().isEmpty()) {
                errorMessage.value = "String value cannot be empty."
                isValueError.value = true
                isSubmitDisabled.value = true
                return
            }

            val parsedValue = when (type.value) {
                "int" -> value.value.toIntOrNull() ?: 0
                "bool" -> value.value.toBooleanStrictOrNull() ?: false
                "double" -> value.value.toDoubleOrNull() ?: 0.0
                "string" -> value.value
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
            modifier = buttonModifier,
            colors = buttonColors,
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Text(
                text = stringResource(id = R.string.create_variable),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = stringResource(id = R.string.declare_variable)) },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { isMultiple.value = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isMultiple.value) Color.Gray else Color.LightGray),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.single),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { isMultiple.value = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isMultiple.value) Color.Gray else Color.LightGray),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.multiple),
                                    fontWeight = FontWeight.Bold
                                )
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

                        if (!(type.value == "bool" && !isMultiple.value)) {
                            OutlinedTextField(
                                value = value.value,
                                onValueChange = { it ->
                                    val parts = it.split(",").map { it.trim() }

                                    val isValid = if (isMultiple.value) {
                                        parts.all { part ->
                                            when (type.value) {
                                                "int" -> part.matches(Regex("^-?\\d*"))
                                                "double" -> part.matches(Regex("^-?\\d*\\.?\\d*"))
                                                "bool" -> part.equals(
                                                    "true", ignoreCase = true
                                                ) || part.equals("false", ignoreCase = true)

                                                else -> true
                                            }
                                        }
                                    } else {
                                        when (type.value) {
                                            "int" -> it.matches(Regex("^-?\\d*"))
                                            "double" -> it.matches(Regex("^-?\\d*\\.?\\d*"))
                                            "bool" -> it.equals(
                                                "true", ignoreCase = true
                                            ) || it.equals("false", ignoreCase = true)

                                            else -> true
                                        }
                                    }

                                    if (isValid) {
                                        value.value = it
                                        validateInputs()
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.initial_value)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                isError = isValueError.value
                            )
                        } else {
                            val options = listOf("true", "false")
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = value.value,
                                    onValueChange = {},
                                    label = { Text("Select Boolean") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded, onDismissRequest = { expanded = false }) {
                                    options.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = { Text(selectionOption) },
                                            onClick = {
                                                value.value = selectionOption
                                                selectedBool.value = selectionOption
                                                expanded = false
                                                validateInputs()
                                            })
                                    }
                                }
                            }
                        }

                        if (isMultiple.value && value.value.isNotEmpty()) {
                            val entered = value.value.split(",").map { it.trim() }
                                .filter { it.isNotEmpty() }.size
                            val expected = names.value.split(",").map { it.trim() }
                                .filter { it.isNotEmpty() }.size
                            if (entered < expected && expected > 1) {
                                Text(
                                    text = "Will auto-fill ${expected - entered} missing value(s) with '${
                                        value.value.split(
                                            ','
                                        ).last().trim()
                                    }'",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = stringResource(id = R.string.select_type),
                            fontWeight = FontWeight.Bold
                        )
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { type.value = "int" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "int") Color.Gray else Color.LightGray),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.integers))
                                }
                                Button(
                                    onClick = { type.value = "bool" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "bool") Color.Gray else Color.LightGray),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.bool))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { type.value = "string" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "string") Color.Gray else Color.LightGray),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.string))
                                }
                                Button(
                                    onClick = { type.value = "double" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type.value == "double") Color.Gray else Color.LightGray),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.doubles))
                                }
                            }

                            if (type.value == "bool" && isMultiple.value) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            value.value =
                                                if (value.value.isEmpty()) "true" else "${value.value}, true"
                                            validateInputs()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(
                                                0xFF4CAF50
                                            )
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(36.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 8.dp, vertical = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.true_button),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            value.value =
                                                if (value.value.isEmpty()) "false" else "${value.value}, false"
                                            validateInputs()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(
                                                0xFFF44336
                                            )
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(36.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 8.dp, vertical = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.false_button),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val list = value.value.split(",").map { it.trim() }
                                                .filter { it.isNotEmpty() }.toMutableList()
                                            if (list.isNotEmpty()) {
                                                list.removeAt(list.lastIndex)
                                                value.value = list.joinToString(", ")
                                                validateInputs()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(36.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 8.dp, vertical = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.remove_button),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
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
                                            "bool" -> valuesFinal[index].toBooleanStrictOrNull()
                                                ?: false

                                            "double" -> valuesFinal[index].toDoubleOrNull() ?: 0.0
                                            "string" -> "\"${valuesFinal[index]}\""
                                            else -> valuesFinal[index]
                                        }
                                        viewModel.declareVariable(name, parsedValue, type.value)
                                    }
                                } else {
                                    val parsedValue: Any = when (type.value) {
                                        "int" -> value.value.toIntOrNull() ?: 0
                                        "bool" -> selectedBool.value.toBooleanStrictOrNull()
                                            ?: false

                                        "double" -> value.value.toDoubleOrNull() ?: 0.0
                                        "string" -> "\"${value.value}\""
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
                })
        }
    }
}
