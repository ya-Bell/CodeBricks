package com.example.codebricks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codebricks.assignment.AssignmentBlock
import com.example.codebricks.assignment.DraggableAssignmentBlock
import com.example.codebricks.assignment.DraggableMultiAssignmentBlock
import com.example.codebricks.assignment.MultiAssignmentBlock
import com.example.codebricks.ui.theme.CodeBricksTheme
import com.example.codebricks.variable_declaration.DraggableVariableBlock
import com.example.codebricks.variable_declaration.VariableDeclarationBlock
import com.example.codebricks.variable_declaration.VariableManager
import com.example.codebricks.variable_declaration.VariablePanel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeBricksTheme {
                var blocks by remember { mutableStateOf(listOf(VariableDeclarationBlock())) }
                var assignmentBlocks by remember { mutableStateOf(listOf<AssignmentBlock>()) }
                var multiAssignmentBlocks by remember { mutableStateOf(listOf<MultiAssignmentBlock>()) }
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                var vars by remember { mutableStateOf(VariableManager.all()) }
                val statsText = stringResource(R.string.stats_format, vars.size)

                LaunchedEffect(Unit) {
                    VariableManager.addListener { vars = VariableManager.all() }
                }

                Box(Modifier.fillMaxSize()) {
                    blocks.forEach { block ->
                        DraggableVariableBlock(
                            block = block,
                            onUpdate = { updated -> blocks = blocks.map { if (it.id == updated.id) updated else it } },
                            onDelete = { id -> blocks = blocks.filter { it.id != id } },
                            canDelete = true
                        )
                    }
                    assignmentBlocks.forEach { block ->
                        DraggableAssignmentBlock(
                            block = block,
                            onUpdate = { updated -> assignmentBlocks = assignmentBlocks.map { if (it.id == updated.id) updated else it } },
                            onDelete = { id -> assignmentBlocks = assignmentBlocks.filter { it.id != id } },
                            canDelete = true,
                            vars = vars
                        )
                    }
                    multiAssignmentBlocks.forEach { block ->
                        DraggableMultiAssignmentBlock(
                            block = block,
                            onUpdate = { updated -> multiAssignmentBlocks = multiAssignmentBlocks.map { if (it.id == updated.id) updated else it } },
                            onDelete = { id -> multiAssignmentBlocks = multiAssignmentBlocks.filter { it.id != id } },
                            canDelete = true,
                            vars = vars
                        )
                    }
                    VariablePanel(vars = vars, modifier = Modifier.align(Alignment.TopEnd))
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    ) {
                        Button(
                            onClick = { blocks = blocks + VariableDeclarationBlock() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.add_block)) }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { assignmentBlocks = assignmentBlocks + AssignmentBlock() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.add_assignment_block)) }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { multiAssignmentBlocks = multiAssignmentBlocks + MultiAssignmentBlock() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.add_multi_assignment_block)) }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { coroutineScope.launch { snackbarHostState.showSnackbar(statsText) } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.show_stats)) }
                    }
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
