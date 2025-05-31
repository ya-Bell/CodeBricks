@file:Suppress("NAME_SHADOWING")

package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codebricks.R
import com.example.codebricks.ui.theme.BackgroundGray
import com.example.codebricks.ui.theme.BlockComparison
import com.example.codebricks.ui.theme.BlockControl
import com.example.codebricks.ui.theme.BlockFunctions
import com.example.codebricks.ui.theme.BlockInputOutput
import com.example.codebricks.ui.theme.BlockLogic
import com.example.codebricks.ui.theme.BlockLoops
import com.example.codebricks.ui.theme.BlockMath
import com.example.codebricks.ui.theme.BlockVariables
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextGray
import com.example.codebricks.ui.theme.TextWhite
import com.example.codebricks.viewmodel.VariableViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun BottomBlockBar(
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    viewModel: VariableViewModel,
    screenHeight: Int
) {

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    val adaptiveHeight = screenHeight * 0.18f

    Column {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(BackgroundGray)
                .border(
                    1.dp, TextGray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(8.dp)
                .height(20.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
            ) {
                item {
                    val buttonModifier =
                        Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, TextBlack, RoundedCornerShape(10.dp))
                            .height(26.dp)
                            .width(100.dp)

                    val categories = listOf(
                        stringResource(id = R.string.control) to BlockControl,
                        stringResource(id = R.string.variables) to BlockVariables,
                        stringResource(id = R.string.math) to BlockMath,
                        stringResource(id = R.string.logic) to BlockLogic,
                        stringResource(id = R.string.input_output) to BlockInputOutput,
                        stringResource(id = R.string.loops) to BlockLoops,
                        stringResource(id = R.string.comparison) to BlockComparison,
                        stringResource(id = R.string.functions) to BlockFunctions
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedClass == name) color else TextWhite,
                                    contentColor = if (selectedClass == name) TextWhite else color
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        shadow = Shadow(
                                            color = TextBlack.copy(alpha = 0.5f),
                                            offset = Offset(1f, 1f),
                                            blurRadius = 2f
                                        )
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(adaptiveHeight.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(TextWhite)
                .border(
                    1.dp, TextGray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
        ) {
            BlockSection(selectedClass, viewModel)
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Preview(showBackground = true)
@Composable
fun BottomBlockBarPreview() {
    val screenHeight = LocalConfiguration.current.screenHeightDp


    BottomBlockBar(
        selectedClass = "Control",
        onClassSelected = {},
        viewModel = viewModel(),
        screenHeight = screenHeight
    )
}