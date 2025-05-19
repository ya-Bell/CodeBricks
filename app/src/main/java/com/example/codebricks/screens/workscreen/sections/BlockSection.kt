package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.codebricks.R
import com.example.codebricks.blocks.control.CreateControlBlock
import com.example.codebricks.blocks.print.CreatePrintBlock
import com.example.codebricks.blocks.variables.DeclareVariable
import com.example.codebricks.ui.theme.CodeBricksTheme
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun BlockSection(selectedClass: String, viewModel: VariableViewModel) {
    Column(modifier = Modifier) {
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

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun BlockSectionPreview() {
    val mockViewModel = VariableViewModel().apply {
        declareVariable("score", 10, "int")
    }

    CodeBricksTheme {
        BlockSection(
            selectedClass = "Variables",
            viewModel = mockViewModel
        )
    }
}