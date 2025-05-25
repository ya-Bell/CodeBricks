package com.example.codebricks.screens.workscreen.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.codebricks.viewmodel.VariableViewModel

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
        WorkspaceControlBar(viewModel = viewModel)
    }
}


@Preview(
    name = "WorkSpaceSection Preview",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun WorkSpaceSectionPreview() {
    val previewViewModel = remember { VariableViewModel() }
    WorkSpaceSection(viewModel = previewViewModel)
}