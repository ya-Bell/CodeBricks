package com.example.codebricks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codebricks.ui.theme.CodeBricksTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.codebricks.variable_declaration.VariableManager
import com.example.codebricks.variable_declaration.VariableDeclarationBlock
import com.example.codebricks.variable_declaration.DraggableVariableBlock

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeBricksTheme {
                var blocks by remember { mutableStateOf(listOf(VariableDeclarationBlock())) }
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                var variables by remember { mutableStateOf(VariableManager.getAllVariables()) }
                val statsText = stringResource(R.string.stats_format, variables.size)

                LaunchedEffect(Unit) {
                    VariableManager.addListener {
                        variables = VariableManager.getAllVariables()
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    blocks.forEach { block ->
                        DraggableVariableBlock(
                            block = block,
                            onUpdate = { updated ->
                                blocks = blocks.map { if (it.id == updated.id) updated else it }
                            },
                            onDelete = { id ->
                                if (blocks.size > 1) {
                                    blocks = blocks.filter { it.id != id }
                                }
                            },
                            canDelete = blocks.size > 1
                        )
                    }

                    VariablePanel(
                        variables = variables,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { blocks = blocks + VariableDeclarationBlock() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.add_block))
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(statsText)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.show_stats))
                        }
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
@Composable
fun VariablePanel(
    variables: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .width(150.dp)
    ) {
        Text(
            stringResource(R.string.variables_title),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(8.dp))

        if (variables.isEmpty()) {
            Text(
                stringResource(R.string.no_variables),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Column {
                variables.forEach { (name, value) ->
                    Text(
                        "$name = $value",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
