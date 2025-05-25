package com.example.codebricks.screens.startscreen

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.codebricks.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var isLogoVisible by remember { mutableStateOf(false) }
    var isProgressVisible by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val progressAnim by animateFloatAsState(
        targetValue = progress.coerceAtMost(1f),
        animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
    )

    LaunchedEffect(Unit) {
        isLogoVisible = true
        isProgressVisible = true
        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate("start")
        }, 4000)

        (1..10).forEach { i ->
            delay(220)
            progress += 0.2f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.horizontalGradient(listOf(Color(0xFF67CAD9), Color(0xFF9ADCE7)))))
    {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            AnimatedVisibility(
                visible = isLogoVisible,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(animationSpec = tween(1000, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(250.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp)
        ) {
            AnimatedVisibility(
                visible = isProgressVisible,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp, 10.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxHeight()
                            .width(300.dp * progressAnim)
                            .background(Color(0xFF239EDE), shape = RoundedCornerShape(5.dp))
                            .animateContentSize()
                    )
                }
            }

            Text(
                text = "${(progressAnim * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 20.dp)
            )
        }
    }
}

