package com.app.lovelyprints

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.app.lovelyprints.theme.LovelyPrintsTheme
import com.app.lovelyprints.ui.main.MainScreen
import com.app.lovelyprints.ui.navigation.AppNavHost
import com.app.lovelyprints.ui.navigation.Routes
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Make status bar transparent with light content
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        window.decorView.setBackgroundColor(android.graphics.Color.RED)


        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        val app = application as LovelyPrintsApp

        setContent {
            LovelyPrintsTheme {
                val navController = rememberNavController()
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    // Render splash without MainScreen wrapper
                    AppNavHost(
                        navController = navController,
                        startDestination = Routes.Splash.route,
                        appContainer = app.appContainer,
                        modifier = Modifier
                    )

                    // Listen for navigation away from splash
                    LaunchedEffect(Unit) {
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            if (destination.route != Routes.Splash.route) {
                                showSplash = false
                            }
                        }
                    }
                } else {
                    // Use MainScreen wrapper for all other screens
                    MainScreen(navController = navController) { paddingModifier ->
                        AppNavHost(
                            navController = navController,
                            startDestination = Routes.Splash.route,
                            appContainer = app.appContainer,
                            modifier = paddingModifier
                        )
                    }
                }
            }
        }
    }
}