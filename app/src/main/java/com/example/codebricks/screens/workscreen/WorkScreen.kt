package com.example.codebricks.screens.workscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codebricks.R
import com.example.codebricks.blocks.variables.DeclareVariable
import com.example.codebricks.screens.workscreen.sections.BottomBlockBar
import com.example.codebricks.screens.workscreen.sections.ConsoleSection
import com.example.codebricks.screens.workscreen.sections.WorkSpaceSection
import com.example.codebricks.viewmodel.VariableViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun WorkScreen(onBackClick: () -> Unit) {
    val viewModel: VariableViewModel = viewModel()
    var selectedClass by remember { mutableStateOf("Control") }
    var showDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB7EFFF))
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        Header(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(6.dp))

        ConsoleSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(10.dp))

        WorkSpaceSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(10.dp))

        BottomBlockBar(
            selectedClass = selectedClass,
            onClassSelected = { selectedClass = it },
            viewModel = viewModel,
            screenHeight = screenHeight
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.code_bricks_logo),
                modifier = Modifier.size(30.dp)
            )


            Text(
                text = stringResource(id = R.string.app_name),
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
                    containerColor = Color.White, contentColor = Color.Black
                )
            ) {
                Text(stringResource(id = R.string.help), color = Color.Black)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, contentColor = Color.Black
                )
            ) {
                Text(stringResource(id = R.string.back), color = Color.Black)
            }
        }
    }
}

@Preview(

    name = "Phone - Pixel 4",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
fun WorkScreenPreview_Pixel4() {
    WorkScreen(onBackClick = {})
}

@Preview(
    name = "Tablet - Nexus 9",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=768dp,height=1024dp,dpi=320"
)
@Composable
fun WorkScreenPreview_Tablet() {
    WorkScreen(onBackClick = {})
}

@Preview(
    name = "Small Phone - Nexus One",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=320dp,height=480dp,dpi=160"
)
@Composable
fun WorkScreenPreview_Small() {
    WorkScreen(onBackClick = {})
}