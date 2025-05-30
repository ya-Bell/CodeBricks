package com.example.codebricks.blocks.control

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.example.codebricks.viewmodel.blocks.declareControlBlock

@Composable
fun CreateControlBlock(viewModel: VariableViewModel) {
    val buttonModifier = Modifier
        .padding(4.dp)
        .height(35.dp)
        .wrapContentWidth()
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF3F51B5), contentColor = Color.Black
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { viewModel.declareControlBlock("Start") },
            modifier = buttonModifier,
            colors = buttonColors,
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Text(
                text = stringResource(id = R.string.create_start_block),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Button(
            onClick = { viewModel.declareControlBlock("Stop") },
            modifier = buttonModifier,
            colors = buttonColors,
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Text(
                text = stringResource(id = R.string.create_stop_block),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
