package com.example.codebricks.blocks.loops.whileendblocks

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
import com.example.codebricks.ui.theme.BlockLoops
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.declareWhileEndBlock

@Composable
fun WhileEndBlock(viewModel: VariableViewModel) {
    val buttonModifier = Modifier
        .padding(4.dp)
        .height(36.dp)
        .fillMaxWidth()

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = BlockLoops,
        contentColor = TextWhite
    )
    Button(
        onClick = { viewModel.declareWhileEndBlock() },
        modifier = buttonModifier,
        colors = buttonColors,
        border = BorderStroke(2.dp, TextBlack)
    ) {
        Text(
            text = stringResource(id = R.string.loops_while_end),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}