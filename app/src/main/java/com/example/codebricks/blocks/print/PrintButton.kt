package com.example.codebricks.blocks.print

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.codebricks.viewmodel.blocks.declarePrintBlock

@Composable
fun CreatePrintBlock(viewModel: VariableViewModel) {
    val showDialog = remember { mutableStateOf(false) }

    val buttonModifier = Modifier
        .padding(4.dp)
        .height(35.dp)
        .wrapContentWidth()

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFE57373), contentColor = Color.Black
    )

    Button(
        onClick = { showDialog.value = true },
        modifier = buttonModifier,
        colors = buttonColors,
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(
            text = stringResource(id = R.string.create_print_block),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }

    if (showDialog.value && viewModel.variables.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.select_variable_to_print)) },
            text = {
                Column {
                    viewModel.variables.forEach { variable ->
                        Button(
                            onClick = {
                                viewModel.declarePrintBlock(variable)
                                showDialog.value = false
                            }, modifier = Modifier.padding(4.dp)
                        ) {
                            Text(variable.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            })
    }
}