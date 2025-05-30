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
import com.example.codebricks.blocks.logic.elseblocks.ElseBlock
import com.example.codebricks.blocks.logic.elseifblocks.ElseIfBlock
import com.example.codebricks.blocks.logic.endifblocks.EndIfBlock
import com.example.codebricks.blocks.logic.ifblocks.IfBlock
import com.example.codebricks.blocks.math.MathBlockButton
import com.example.codebricks.blocks.print.CreatePrintBlock
import com.example.codebricks.blocks.variables.varchangeby.ChangeVariableButton
import com.example.codebricks.blocks.variables.varconvert.ConvertVariable
import com.example.codebricks.blocks.variables.vardeclare.DeclareVariable
import com.example.codebricks.blocks.variables.varreference.VariableReferenceBlock
import com.example.codebricks.blocks.variables.varset.SetVariableButton
import com.example.codebricks.ui.theme.CodeBricksTheme
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.declareMathBlock
import com.example.codebricks.viewmodel.blocks.declareVariable

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
                    item {
                        ConvertVariable(viewModel)
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
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        IfBlock(viewModel)
                    }
                    item {
                        ElseIfBlock(viewModel)
                    }
                    item {
                        ElseBlock(viewModel)
                    }
                    item {
                        EndIfBlock(viewModel)
                    }
                }
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