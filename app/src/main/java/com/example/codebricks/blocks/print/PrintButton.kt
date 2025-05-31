package com.example.codebricks.blocks.print

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.ui.theme.BlockHighlighted
import com.example.codebricks.ui.theme.BlockIO
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextGray
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.declarePrintBlock
import com.example.codebricks.viewmodel.blocks.declarePrintTextBlock

@Composable
fun CreatePrintBlock(viewModel: VariableViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var isTextMode by remember { mutableStateOf(false) }

    val buttonModifier = Modifier
        .padding(4.dp)
        .height(36.dp)
        .wrapContentWidth()

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = BlockIO,
        contentColor = TextWhite
    )

    Button(
        onClick = { showDialog.value = true },
        modifier = buttonModifier,
        colors = buttonColors,
        border = BorderStroke(2.dp, TextBlack)
    ) {
        Text(
            text = stringResource(id = R.string.create_print_block),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.select_print_type)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { isTextMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isTextMode) BlockHighlighted else TextGray
                            )
                        ) {
                            Text(stringResource(id = R.string.variable_button))
                        }
                        Button(
                            onClick = { isTextMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTextMode) BlockHighlighted else TextGray
                            )
                        ) {
                            Text(stringResource(id = R.string.text_button))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isTextMode) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text(stringResource(id = R.string.enter_text_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (textInput.isNotEmpty()) {
                                    viewModel.declarePrintTextBlock(textInput)
                                    showDialog.value = false
                                    textInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.add_text_block))
                        }
                    } else {
                        if (viewModel.variables.isNotEmpty()) {
                            viewModel.variables.forEach { variable ->
                                Button(
                                    onClick = {
                                        viewModel.declarePrintBlock(variable)
                                        showDialog.value = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(variable.name)
                                }
                            }
                        } else {
                            Text(text = stringResource(id = R.string.no_variables_created))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}