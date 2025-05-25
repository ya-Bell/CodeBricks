package com.example.codebricks.blocks.variables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun SetVariableButton(viewModel: VariableViewModel) {
    Button(
        onClick = {
            viewModel.declareEmptySetVariableBlock()
        },
        modifier = Modifier.padding(4.dp)
    ) {
        Text("Set Variable", fontSize = 12.sp)
    }
}
