package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R
import com.example.codebricks.viewmodel.VariableViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ConsoleSection(viewModel: VariableViewModel) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val consoleHeight = screenHeight * 0.15f

    val scrollStateVertical = rememberScrollState()

    val consoleText = viewModel.consoleOutput.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(consoleHeight.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD9D9D9))
                .border(
                    1.dp,
                    Color.Gray,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Text(
                text = stringResource(id = R.string.console_header),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .verticalScroll(scrollStateVertical)
            ) {
                Column {
                    Text(
                        text = consoleText,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }

            val proportion = scrollStateVertical.maxValue.takeIf { it > 0 }?.let {
                scrollStateVertical.value.toFloat() / it.toFloat()
            } ?: 0f

            val thumbHeight = 70f * (70f / (scrollStateVertical.maxValue + 70f))
            val thumbOffset = (70f - thumbHeight) * proportion

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.TopEnd),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = thumbOffset.dp)
                        .height(thumbHeight.dp)
                        .background(Color.DarkGray.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .clickable {
                        viewModel.clearConsole()
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.clear_console),
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Gray
                )
            }
        }
    }
}
