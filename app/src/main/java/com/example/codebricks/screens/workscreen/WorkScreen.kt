package com.example.codebricks.screens.workscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.screens.workscreen.sections.ConsoleSection
import com.example.codebricks.screens.workscreen.sections.WorkSpaceSection
import com.example.codebricks.blocks.variables.DeclareVariable
import com.example.codebricks.screens.workscreen.sections.BottomBlockBar

@Composable
fun WorkScreen(onBackClick: () -> Unit) {
    val viewModel: VariableViewModel = viewModel()
    var selectedClass by remember { mutableStateOf("Control") }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB7EFFF))
            .padding(16.dp)
    ) {
        Header(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(8.dp))

        ConsoleSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(14.dp))

        WorkSpaceSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(14.dp))

        BottomBlockBar(
            selectedClass = selectedClass,
            onClassSelected = { selectedClass = it },
            viewModel = viewModel
        )

        if (showDialog) {
            DeclareVariable(viewModel = viewModel)
            showDialog = false
        }
    }
}

@Composable
fun Header(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()

            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CodeBricks",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row {
            Button(
                onClick = {/* TODO: Help */ },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Help", color = Color.Black)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Back", color = Color.Black)
            }
        }
    }

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WorkScreenPreview() {
    WorkScreen(onBackClick = {})
}
