package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codebricks.R
import com.example.codebricks.viewmodel.VariableViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ConsoleSection(viewModel: VariableViewModel) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val consoleHeight = screenHeight * 0.22f

    val scrollStateVertical = rememberScrollState()
    var userInput by remember { mutableStateOf("") }

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
                    1.dp, Color.Gray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.console_header),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                IconButton(
                    onClick = { viewModel.clearConsole() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.clear_console),
                        tint = Color.Gray
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 12.dp)
                    .verticalScroll(scrollStateVertical)
            ) {
                Text(
                    text = consoleText,
                    fontSize = 12.sp,
                    color = Color.Black,
                    lineHeight = 16.sp
                )
            }

            val proportion = scrollStateVertical.maxValue.takeIf { it > 0 }?.let {
                scrollStateVertical.value.toFloat() / it.toFloat()
            } ?: 0f

            val thumbHeight = 90f * (90f / (scrollStateVertical.maxValue + 90f))
            val thumbOffset = (90f - thumbHeight) * proportion

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = thumbOffset.dp)
                        .height(thumbHeight.dp)
                        .background(Color.DarkGray.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactTextField(
                value = userInput,
                onValueChange = { userInput = it },
                onSendClick = {
                    if (userInput.isNotEmpty()) {
                        viewModel.processUserInput(userInput)
                        userInput = ""
                    }
                }
            )
        }
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Enter command...",
    onSendClick: () -> Unit
) {
    val shape = RoundedCornerShape(6.dp)
    val borderColor = Color(0xFF2196F3)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, borderColor, shape)
                .clip(shape)
                .background(Color(0xFFF8F8F8))
                .height(30.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart

        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Black
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}