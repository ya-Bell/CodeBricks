package com.example.codebricks.variables

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun DeclareVariable(viewModel: VariableViewModel) {

    var variableNames by remember { mutableStateOf("") }
    var variableType by remember { mutableStateOf("int") }
    var isMultiple by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    val containerWidth = 380.dp
    val containerHeight = 175.dp


    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .requiredSize(140.dp, 40.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    offset = Offset(
                        (offset.x + dragAmount.x).coerceIn(0f, (containerWidth - 140.dp).toPx()),
                        (offset.y + dragAmount.y).coerceIn(0f, (containerHeight - 40.dp).toPx())
                    )
                    change.consume()
                }
            }
    ) {
        Button(
            onClick = {
                variableNames = ""
                showDialog = true
            },
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(14.dp))
                .height(36.dp)
                .width(150.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA500),
                contentColor = Color.Black
            ),
            interactionSource = remember { MutableInteractionSource() },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Create Variable",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Create Variable", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { isMultiple = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isMultiple) Color.Gray else Color.LightGray),
                                modifier = Modifier.padding(4.dp).weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Single", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isMultiple = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isMultiple) Color.Gray else Color.LightGray),
                                modifier = Modifier.padding(4.dp).weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Multiple", fontWeight = FontWeight.Bold)
                            }
                        }

                        TextField(
                            value = variableNames,
                            onValueChange = { variableNames = it },
                            label = { Text("Variable Names") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Text("Select Type", fontWeight = FontWeight.Bold)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { variableType = "int" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (variableType == "int") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("int")
                                }
                                Button(
                                    onClick = { variableType = "bool" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (variableType == "bool") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("bool")
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { variableType = "string" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (variableType == "string") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("string")
                                }
                                Button(
                                    onClick = { variableType = "double" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (variableType == "double") Color.Gray else Color.LightGray
                                    ),
                                    modifier = Modifier.padding(4.dp).weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("double")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val value = when (variableType) {
                                "int" -> 0
                                "double" -> 0.0
                                "bool" -> false
                                "string" -> ""
                                else -> 0
                            }

                            if (isMultiple) {
                                val names = variableNames.split(",").map { it.trim() }
                                names.forEach { name ->
                                    viewModel.declareVariable(name, value, variableType)
                                }
                            } else {
                                viewModel.declareVariable(variableNames, value, variableType)
                            }

                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Create", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

