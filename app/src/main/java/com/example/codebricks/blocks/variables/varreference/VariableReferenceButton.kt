package com.example.codebricks.blocks.variables.varreference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.blocks.addReferenceBlock

@Composable
fun VariableReferenceBlock(variable: Variable, viewModel: VariableViewModel) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .widthIn(min = 70.dp)
            .height(36.dp)
            .background(Color(0xFFFB8C00), RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
            .clickable {
                viewModel.addReferenceBlock(variable)
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = variable.name,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
