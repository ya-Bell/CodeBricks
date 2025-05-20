package com.example.codebricks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.codebricks.screens.workscreen.WorkScreen
import com.example.codebricks.ui.theme.CodeBricksTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.codebricks.screens.startscreen.StartScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDark by remember { mutableStateOf(false) }
            var isEnglish by remember { mutableStateOf(true) }

            CodeBricksTheme(darkTheme = isDark) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "start"
                ) {
                    composable("start") {
                        StartScreen(
                            onStartClick = { navController.navigate("work") },
                            onAboutClick = { /* TODO: AboutScreen */ },
                            isThemeDark = isDark,
                            onThemeToggle = { isDark = it },
                            isEnglish = isEnglish,
                            onLanguageToggle = { isEnglish = it }
                        )
                    }

                    composable("work") {
                        WorkScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

