package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.codebricks.R
import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.blocks.control.CreateControlBlock
import com.example.codebricks.blocks.math.MathBlockButton
import com.example.codebricks.blocks.print.CreatePrintBlock
import com.example.codebricks.blocks.variables.ChangeVariableButton
import com.example.codebricks.blocks.variables.DeclareVariable
import com.example.codebricks.blocks.variables.SetVariableButton
import com.example.codebricks.blocks.variables.VariableReferenceBlock
import com.example.codebricks.ui.theme.CodeBricksTheme
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun BlockSection(selectedClass: String, viewModel: VariableViewModel) {
    Column(modifier = Modifier) {
        when (selectedClass) {
            "Control" -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CreateControlBlock(viewModel)
                    }
                }
            }

            "Variables" -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        DeclareVariable(viewModel)
                    }
                    item {
                        SetVariableButton(viewModel)
                    }
                    item {
                        ChangeVariableButton(viewModel)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.variables) { variable ->
                        VariableReferenceBlock(variable = variable, viewModel = viewModel)
                    }
                }
            }

            "Math" -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        MathBlockButton("Add") { viewModel.declareMathBlock(BlockType.MATH_ADD) }
                    }
                    item {
                        MathBlockButton("Subtract") { viewModel.declareMathBlock(BlockType.MATH_SUBTRACT) }
                    }
                    item {
                        MathBlockButton("Multiply") { viewModel.declareMathBlock(BlockType.MATH_MULTIPLY) }
                    }
                    item {
                        MathBlockButton("Divide") { viewModel.declareMathBlock(BlockType.MATH_DIVIDE) }
                    }
                }
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
            selectedClass = "Variables", viewModel = mockViewModel
        )
    }
}