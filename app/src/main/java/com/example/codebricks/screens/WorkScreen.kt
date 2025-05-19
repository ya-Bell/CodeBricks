package com.example.codebricks.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codebricks.R
import com.example.codebricks.blocks.BlockSection
import com.example.codebricks.control.DraggableControlBlock
import com.example.codebricks.dragging.DraggableItem
import com.example.codebricks.print.DraggablePrintBlock
import com.example.codebricks.variables.DeclareVariable
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun WorkScreen() {

    val viewModel: VariableViewModel = viewModel()

    var selectedClass by remember { mutableStateOf("Control") }

    var showDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB7EFFF))
            .padding(16.dp)
    ) {

        Header()

        Spacer(modifier = Modifier.height(8.dp))

        ConsoleSection()

        Spacer(modifier = Modifier.height(14.dp))

        WorkSpaceSection(viewModel)

        Spacer(modifier = Modifier.height(14.dp))

        BottomBar(
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
fun Header() {
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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.code_bricks_logo),
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row {
            Button(
                onClick = {  },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(stringResource(id = R.string.help_button), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(stringResource(id = R.string.back_button), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun ConsoleSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFFD9D9D9))
                .border(1.dp, Color.Gray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.console_label),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterStart)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Color.White)
                .border(1.dp, Color.Gray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
        }
    }
}

@Composable
fun WorkSpaceSection(viewModel: VariableViewModel) {
    val isStarted = remember { mutableStateOf(false) }
    val processRunning = remember { mutableStateOf(false) }

    var containerWidth by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }

    val blocks = viewModel.variables
    val controlBlocks = viewModel.blocks.filter { it.type == "Control" }

    fun toggleProcess() {
        processRunning.value = !processRunning.value
        isStarted.value = !isStarted.value
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFFD9D9D9))
                .border(1.dp, Color.Gray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.workspace_label),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {},
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                    .height(26.dp)
                    .width(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.debug_button),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .background(Color.White)
                .border(1.dp, Color.Gray)
                .onSizeChanged { size ->
                    containerWidth = size.width.toFloat()
                    containerHeight = size.height.toFloat()
                }
        ) {
            blocks.forEach { variable ->
                if (variable.type != "Control") {
                    DraggableItem(variable = variable, containerWidth = containerWidth, containerHeight = containerHeight)
                }
            }

            controlBlocks.forEach { controlBlock ->
                if (controlBlock.name == "Print") {
                    val selectedVariable = controlBlock.value as? Variable
                    if (selectedVariable != null) {
                        DraggablePrintBlock(variable = selectedVariable, containerWidth = containerWidth, containerHeight = containerHeight)
                    }
                } else {
                    DraggableControlBlock(type = controlBlock.name, containerWidth = containerWidth, containerHeight = containerHeight)
                }
            }

            if (blocks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_variables_created),
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

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
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                    contentDescription = "Trash Bin",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = { toggleProcess() },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                    .height(26.dp)
                    .width(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (processRunning.value) stringResource(id = R.string.stop_button) else stringResource(id = R.string.run_button),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BottomBar(
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    viewModel: VariableViewModel
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFFD9D9D9))
                .border(
                    1.dp,
                    Color.Gray,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(8.dp)
                .height(20.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                item {
                    val buttonModifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(10.dp))
                        .height(26.dp)
                        .width(100.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Button(
                            onClick = { onClassSelected("Control") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFF3F51B5
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.control_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onClassSelected("Variables") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFFFFA500
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.variables_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onClassSelected("Math") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFF4FC3F7
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.math_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onClassSelected("Comparison") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFF9C27B0
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.comparison_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onClassSelected("Logic") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFF81C784
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.logic_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onClassSelected("Input/Output") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFFE57373
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.input_output_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onClassSelected("Loops") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFFFFEB3B
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.loops_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onClassSelected("Functions") },
                            modifier = buttonModifier,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFFE91E63
                                )
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.functions_button),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(Color.White)
                .border(
                    1.dp,
                    Color.Gray,
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
        ) {
            BlockSection(selectedClass, viewModel)
        }
    }
}
