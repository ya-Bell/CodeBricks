package com.example.codebricks.blocks.math

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MathBlockButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(4.dp)
            .height(36.dp)
            .width(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4FC3F7), contentColor = Color.Black
        ),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}