package com.example.codebricks.screens.workscreen.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun WorkSpaceSection(viewModel: VariableViewModel, modifier: Modifier = Modifier) {

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    Column(modifier = modifier.fillMaxWidth()) {
        WorkspaceHeader(viewModel = viewModel)

        WorkspaceCanvas(
            viewModel = viewModel,
            onSizeChanged = { w, h -> },
        )
        WorkspaceControlBar(viewModel = viewModel)
    }
}