package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.ui.theme.BackgroundGray
import com.example.codebricks.ui.theme.IconGray
import com.example.codebricks.ui.theme.IconGreen
import com.example.codebricks.ui.theme.IconRed
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextGray
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.viewmodel.execution.checkBlockOrder
import com.example.codebricks.viewmodel.execution.linkBlocksByPosition
import kotlinx.coroutines.launch

@Composable
fun WorkspaceControlBar(viewModel: VariableViewModel) {
    var processRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(BackgroundGray)
            .border(1.dp, TextGray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Trash icon
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    viewModel.clearWorkspace()
                }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.trash_bin),
                modifier = Modifier.fillMaxSize(),
                tint = IconGray
            )
        }

        // Run / Reset button
        Button(
            onClick = {
                if (!processRunning) {
                    processRunning = true
                    scope.launch {
                        val orderCheck = viewModel.checkBlockOrder()
                        if (!orderCheck.isValid) {
                            viewModel.consoleOutput.value =
                                orderCheck.errorMessage ?: "❌ Unknown block order error."
                            processRunning = false
                            return@launch
                        }

                        viewModel.shouldDrawConnections.value = true
                        viewModel.linkBlocksByPosition()
                        viewModel.executeProgram {
                            processRunning = false
                        }
                    }
                } else {
                    processRunning = false
                }
            },
            modifier = Modifier
                .padding(1.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TextBlack, RoundedCornerShape(12.dp))
                .height(26.dp)
                .width(65.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TextWhite, contentColor = TextBlack
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = if (processRunning) R.string.reset else R.string.run),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (processRunning) Icons.Filled.Refresh else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(id = if (processRunning) R.string.reset else R.string.run),
                    tint = if (processRunning) IconRed else IconGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
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
