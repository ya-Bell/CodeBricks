package com.example.codebricks.screens.workscreen.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codebricks.viewmodel.VariableViewModel

@Composable
fun BottomBlockBar(
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    viewModel: VariableViewModel
) {
    Column {
        // Кнопки выбора категории
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

                    val categories = listOf(
                        "Control" to Color(0xFF3F51B5),
                        "Variables" to Color(0xFFFFA500),
                        "Math" to Color(0xFF4FC3F7),
                        "Comparison" to Color(0xFF9C27B0),
                        "Logic" to Color(0xFF81C784),
                        "Input/Output" to Color(0xFFE57373),
                        "Loops" to Color(0xFFFFEB3B),
                        "Functions" to Color(0xFFE91E63)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { (name, color) ->
                            Button(
                                onClick = { onClassSelected(name) },
                                modifier = buttonModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = color),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text = name, fontSize = 12.sp, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }
                }
            }
        }

        // Отображение блоков выбранной категории
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Color.White)
                .border(1.dp, Color.Gray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            BlockSection(selectedClass, viewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomBlockBarPreview() {
    BottomBlockBar(
        selectedClass = "Control",
        onClassSelected = {},
        viewModel = viewModel()
    )
}