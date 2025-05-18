package com.example.codebricks.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    isThemeDark: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    isEnglish: Boolean = true,
    onLanguageToggle: (Boolean) -> Unit = {}
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
            StartWorkButton(onClick = onStartClick)

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка About App
            AboutAppButton(onClick = onAboutClick)
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

    AnimatedVisibility(
        visible = isSettingsVisible,
        enter = fadeIn(tween(150)) + scaleIn(tween(200), initialScale = 0.9f),
        exit = ExitTransition.None
    ) {
        SettingsOverlay(
            onDismiss = { isSettingsVisible = false },
            isThemeDark = isThemeDark,
            onThemeToggle = onThemeToggle,
            isEnglish = isEnglish,
            onLanguageToggle = onLanguageToggle
        )
    }
}


@Composable
fun SettingsOverlay(
    onDismiss: () -> Unit,
    isThemeDark: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    isEnglish: Boolean,
    onLanguageToggle: (Boolean) -> Unit
) {
    val visibleState = remember { androidx.compose.animation.core.MutableTransitionState(true) }

    if (!visibleState.currentState && !visibleState.targetState) {
        LaunchedEffect(Unit) {
            onDismiss()
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(150)) + scaleIn(tween(200), initialScale = 0.9f),
        exit = fadeOut(tween(150)) + scaleOut(tween(200), targetScale = 0.9f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    visibleState.targetState = false
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(235.dp)
                    .height(162.dp)
                    .border(1.dp, Color(0xFFCCCCCC), shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.Black),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .height(32.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { visibleState.targetState = false },
                        modifier = Modifier
                            .size(21.dp)
                            .align(Alignment.TopCenter)
                            .offset(x = 90.dp, y = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 8.dp, top = 50.dp, bottom = 10.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "Language",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.height(24.dp),
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            modifier = Modifier.padding(start = 9.dp)
                        ) {
                            LanguageOption(
                                text = "English",
                                isSelected = isEnglish,
                                modifier = Modifier.size(width = 90.dp, height = 31.dp),
                                onClick = { onLanguageToggle(true) }
                            )
                            LanguageOption(
                                text = "Русский",
                                isSelected = !isEnglish,
                                modifier = Modifier.size(width = 90.dp, height = 31.dp),
                                onClick = { onLanguageToggle(false) }
                            )
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        ) {
                            Text(
                                text = "Dark theme",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )

                            CustomSwitch(
                                checked = isThemeDark,
                                onCheckedChange = onThemeToggle,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 133.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(checked, label = "switchTransition")

    val thumbOffset by transition.animateDp(
        label = "thumbOffset"
    ) { if (it) 24.dp else 2.dp }

    val trackColor by transition.animateColor(
        label = "trackColor"
    ) { if (it) Color(0xFF00D651) else Color(0xFFE0E0E0) }

    Box(
        modifier = modifier
            .width(50.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}


@Composable
fun LanguageOption(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (isSelected) Color(0xFF00D651) else Color.White
    val border = if (isSelected) Color.Transparent else Color(0xFFCCCCCC)
    val textColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = modifier
            .background(background, shape = RoundedCornerShape(12.dp))
            .border(1.dp, border, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun StartWorkButton(
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
fun AboutAppButton(
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




@Preview(showBackground = true, device = "spec:width=239dp,height=176dp")
@Composable
fun SettingsOverlayPreview() {
    CodeBricksTheme {
        SettingsOverlay(
            onDismiss = {},
            isThemeDark = false,
            onThemeToggle = {},
            isEnglish = true,
            onLanguageToggle = {}
        )
    }
}


