package com.example.codebricks.screens.workscreen.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkspaceHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFD9D9D9))
            .border(1.dp, Color.Gray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Workspace",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )

        Button(
            onClick = { /* TODO: Debug */ },
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                .height(26.dp)
                .width(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color.Black
            )
        ) {
            Text("Debug", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
