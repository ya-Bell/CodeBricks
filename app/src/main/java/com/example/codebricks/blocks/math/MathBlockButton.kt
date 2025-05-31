package com.example.codebricks.blocks.math

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.ui.theme.BlockMath
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite


@Composable
fun MathBlockButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(4.dp)
            .height(36.dp)
            .widthIn(min = 100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BlockMath,
            contentColor = TextWhite
        ),
        border = BorderStroke(2.dp, TextBlack)
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}