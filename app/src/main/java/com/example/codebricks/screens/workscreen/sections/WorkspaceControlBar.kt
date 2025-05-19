package com.example.codebricks.workscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.viewmodel.VariableViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkspaceControlBar(viewModel: VariableViewModel) {
    var processRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD9D9D9))
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Trash icon
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                contentDescription = "Trash Bin",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Run / Reset button
        Button(
            onClick = {
                if (!processRunning) {
                    processRunning = true
                    scope.launch {
                        viewModel.executeProgram(onFinish = {
                            processRunning = false
                        })
                    }
                } else {
                    processRunning = false
                }
            },
            modifier = Modifier
                .padding(1.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                .height(35.dp)
                .width(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = if (processRunning) "Reset" else "Run",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun WorkspaceControlBarPreview() {
    val mockViewModel = VariableViewModel()

    WorkspaceControlBar(viewModel = mockViewModel)
}