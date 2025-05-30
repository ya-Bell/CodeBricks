package com.example.codebricks.screens.workscreen.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

    ConsoleSectionContent(
        consoleHeight = consoleHeight,
        consoleText = consoleText,
        userInput = userInput,
        onUserInputChange = { userInput = it },
        onSendClick = {
            if (userInput.isNotEmpty()) {
                viewModel.processUserInput(userInput)
                userInput = ""
            }
        },
        scrollState = scrollStateVertical
    )
}

@Composable
private fun ConsoleSectionContent(
    consoleHeight: Float,
    consoleText: String,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
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
            Text(
                text = stringResource(id = R.string.console_header),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
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
                    .padding(4.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = consoleText,
                    fontSize = 12.sp,
                    color = Color.Black,
                    lineHeight = 16.sp
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
                onValueChange = onUserInputChange,
                onSendClick = onSendClick
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