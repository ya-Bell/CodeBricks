package com.example.codebricks.blocks.variables.varchangeby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.ui.theme.BlockVariables
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.declareEmptyChangeVariableBlock

@Composable
fun ChangeVariableButton(viewModel: VariableViewModel) {
    val changeVariableButtonModifier = Modifier
        .padding(4.dp)
        .height(36.dp)
        .fillMaxWidth()

    val changeVariableButtonColors = ButtonDefaults.buttonColors(
        containerColor = BlockVariables,
        contentColor = TextWhite
    )
    Button(
        onClick = { viewModel.declareEmptyChangeVariableBlock() },
        modifier = changeVariableButtonModifier,
        colors = changeVariableButtonColors,
        border = BorderStroke(2.dp, TextBlack)
    ) {
        Text(
            text = stringResource(id = R.string.change_variable_button),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
