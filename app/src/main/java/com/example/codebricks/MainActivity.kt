package com.example.codebricks

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.codebricks.screens.startscreen.SplashScreen
import com.example.codebricks.screens.startscreen.StartScreen
import com.example.codebricks.screens.workscreen.WorkScreen
import com.example.codebricks.ui.theme.CodeBricksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDark by remember { mutableStateOf(false) }
            var isEnglish by remember { mutableStateOf(true) }

            CodeBricksTheme(darkTheme = isDark) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController, startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(navController = navController)
                    }

                    composable("start") {
                        StartScreen(
                            onStartClick = { navController.navigate("work_screen") },
                            onAboutClick = {
                                val url = getString(R.string.repository_url)
                                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                startActivity(intent)
                            },
                            isThemeDark = isDark,
                            onThemeToggle = { isDark = it },
                            isEnglish = isEnglish,
                            onLanguageToggle = { isEnglish = it })
                    }

                    composable("work_screen") {
                        WorkScreen(
                            navController = navController,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

