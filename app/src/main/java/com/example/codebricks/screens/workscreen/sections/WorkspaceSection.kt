package com.example.codebricks.screens.workscreen.sections

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codebricks.viewmodel.VariableViewModel
import com.example.codebricks.workscreen.WorkspaceControlBar

@Composable
fun WorkSpaceSection(viewModel: VariableViewModel) {
    var containerWidth by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        WorkspaceHeader()

        WorkspaceCanvas(
            viewModel = viewModel,
            onSizeChanged = { w, h ->
                containerWidth = w
                containerHeight = h
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        WorkspaceControlBar(viewModel = viewModel)
    }
}