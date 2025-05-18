package com.example.codebricks.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.example.codebricks.ui.theme.CodeBricksTheme
import androidx.compose.ui.graphics.graphicsLayer
import com.example.codebricks.R


@Composable
fun StartScreen(
    onStartClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
) {
    var isSettingsVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFAEEFFF))
            .padding(24.dp)
    ) {
        // Кнопка настроек
        SettingsButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { isSettingsVisible = true }
        )

        // Основной контент
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Логотип
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.logo_description),
                modifier = Modifier
                    .size(282.dp)
            )


            Spacer(modifier = Modifier.height(24.dp))

            // Название приложения
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color.Black,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(0f, 4f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Кнопка Start Work
            StartWorkButtonHoverable(onClick = onStartClick)

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка About App
            AboutAppButtonHoverable(onClick = onAboutClick)
        }
            // Версия внизу
            Text(
                text = stringResource(R.string.version),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }


@Composable
fun StartWorkButtonHoverable(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            Color.Black.copy(alpha = 0.85f)
        else
            Color.Black,
        animationSpec = tween(durationMillis = 120)
    )

    Box(
        modifier = modifier
            .size(width = 291.dp, height = 46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(100.dp),
                clip = false
            )
            .background(color = backgroundColor, shape = RoundedCornerShape(100.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                            onClick()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.start_work),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
@Composable
fun AboutAppButtonHoverable(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            Color(0xFFEEEEEE)
        else
            Color.White,
        animationSpec = tween(120)
    )

    val textColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF222222) else Color.Black,
        animationSpec = tween(120)
    )

    Box(
        modifier = modifier
            .size(width = 291.dp, height = 46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(100.dp),
                clip = false
            )
            .background(color = backgroundColor, shape = RoundedCornerShape(100.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                            onClick()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.about_app),
            color = textColor,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120)
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                clip = false
            )
            .background(color = Color.White, shape = CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                            onClick()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = stringResource(R.string.settings),
            tint = Color.Black
        )
    }
}





@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    CodeBricksTheme {
        StartScreen(
            onStartClick = {},
            onAboutClick = {},
        )
    }
}


