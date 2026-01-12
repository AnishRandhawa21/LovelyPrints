package com.app.lovelyprints

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.app.lovelyprints.theme.LovelyPrintsTheme
import com.app.lovelyprints.ui.main.MainScreen
import com.app.lovelyprints.ui.navigation.AppNavHost
import com.app.lovelyprints.ui.navigation.Routes

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LovelyPrintsApp

        setContent {
            LovelyPrintsTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    // 🔑 Directly read token from DataStore
                    val token = app.appContainer.tokenManager.getTokenBlocking()

                    startDestination = if (token.isNullOrEmpty()) {
                        Routes.Login.route
                    } else {
                        Routes.Main.route
                    }
                }

                startDestination?.let { destination ->
                    MainScreen(navController = navController) { paddingModifier ->
                        AppNavHost(
                            navController = navController,
                            startDestination = destination,
                            appContainer = app.appContainer,
                            modifier = paddingModifier
                        )
                    }
                }
            }
        }
    }
}

