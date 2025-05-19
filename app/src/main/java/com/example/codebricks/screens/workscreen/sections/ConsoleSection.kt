package com.example.codebricks.screens.workscreen.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun ConsoleSection(viewModel: VariableViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD9D9D9))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Console",
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Scrollable output
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(8.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                Text(
                    text = viewModel.consoleOutput.value,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .clickable {
                        viewModel.clearConsole()
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Console",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Gray
                )
            }
        }
    }
}
