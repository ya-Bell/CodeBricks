package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(Color(0xFFD9D9D9))
            .border(1.dp, Color.Gray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Trash icon
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    viewModel.clearWorkspace()
                }) {
            Image(
                painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                contentDescription = stringResource(id = R.string.trash_bin),
                modifier = Modifier.fillMaxSize()
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
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                .height(26.dp)
                .width(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF), contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(id = if (processRunning) R.string.reset else R.string.run),
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
