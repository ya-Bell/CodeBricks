package com.example.codebricks.blocks.variables.varset

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.declareEmptySetVariableBlock

@Composable
fun SetVariableButton(viewModel: VariableViewModel) {
    val buttonModifier = Modifier
        .padding(4.dp)
        .height(36.dp)
        .fillMaxWidth()

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFFA500), contentColor = Color.Black
    )
    Button(
        onClick = { viewModel.declareEmptySetVariableBlock() },
        modifier = buttonModifier,
        colors = buttonColors,
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(text = stringResource(id = R.string.set_variable), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}