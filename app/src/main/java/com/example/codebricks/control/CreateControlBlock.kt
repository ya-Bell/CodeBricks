package com.example.codebricks.control

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun CreateControlBlock(viewModel: VariableViewModel) {
    Column {
        Button(
            onClick = {
                viewModel.declareControlBlock("Start")
            },
            modifier = Modifier.padding(4.dp)
        ) {
            Text(stringResource(id = R.string.create_start_block), fontSize = 12.sp)
        }

        Button(
            onClick = {
                viewModel.declareControlBlock("Stop")
            },
            modifier = Modifier.padding(4.dp)
        ) {
            Text(stringResource(id = R.string.create_stop_block), fontSize = 12.sp)
        }
    }
}

