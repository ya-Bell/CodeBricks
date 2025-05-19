package com.example.codebricks.blocks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.codebricks.R
import com.example.codebricks.control.CreateControlBlock
import com.example.codebricks.print.CreatePrintBlock
import com.example.codebricks.variables.DeclareVariable
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun BlockSection(selectedClass: String, viewModel: VariableViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (selectedClass) {
            "Control" -> {
                CreateControlBlock(viewModel)
            }
            "Variables" -> {
                DeclareVariable(viewModel)
            }
            "Math" -> {
                Text(stringResource(id = R.string.math_blocks))
            }
            "Comparison" -> {
                Text(stringResource(id = R.string.comparison_blocks))
            }
            "Logic" -> {
                Text(stringResource(id = R.string.logic_blocks))
            }
            "Input/Output" -> {
                CreatePrintBlock(viewModel)
            }
            "Loops" -> {
                Text(stringResource(id = R.string.loops_blocks))
            }
            "Functions" -> {
                Text(stringResource(id = R.string.functions_blocks))
            }
        }
    }
}
