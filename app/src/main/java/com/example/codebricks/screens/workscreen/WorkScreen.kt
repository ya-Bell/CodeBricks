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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.codebricks.R
import com.example.codebricks.blocks.variables.vardeclare.DeclareVariable
import com.example.codebricks.screens.workscreen.sections.BottomBlockBar
import com.example.codebricks.screens.workscreen.sections.ConsoleSection
import com.example.codebricks.screens.workscreen.sections.WorkSpaceSection
import com.example.codebricks.ui.theme.BackgroundBlue
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.VariableViewModelFactory

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun WorkScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: VariableViewModel = viewModel(
        factory = VariableViewModelFactory(context)
    )
    var selectedClass by remember { mutableStateOf("Control") }
    var showDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Header(
            onBackClick = { navController.popBackStack() },
            onHelpClick = { showHelpDialog = true })

        Spacer(modifier = Modifier.height(4.dp))

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
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
}


@Composable
fun Header(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit
) {
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
                onClick = onHelpClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextWhite, contentColor = TextBlack
                )
            ) {
                Text(stringResource(id = R.string.help), color = TextBlack)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextWhite, contentColor = TextBlack
                )
            ) {
                Text(stringResource(id = R.string.back), color = TextBlack)
            }
        }
    }
}