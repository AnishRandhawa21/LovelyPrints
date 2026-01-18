package com.app.lovelyprints

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.app.lovelyprints.theme.LovelyPrintsTheme
import com.app.lovelyprints.ui.main.MainScreen
import com.app.lovelyprints.ui.navigation.AppNavHost
import com.app.lovelyprints.ui.navigation.Routes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🎨 Prevent white flash - set window background immediately
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#151419"))

        // ✅ Required for edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LovelyPrintsTheme {

                FixSystemBars(
                    enabled = true
                )

                val navController = rememberNavController()
                var showSplash by remember { mutableStateOf(true) }

                // ✅ ROOT NEVER DISAPPEARS
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    if (showSplash) {

                        AppNavHost(
                            navController = navController,
                            startDestination = Routes.Splash.route,
                            appContainer = (application as LovelyPrintsApp).appContainer,
                            modifier = Modifier.fillMaxSize()
                        )

                        LaunchedEffect(Unit) {
                            navController.addOnDestinationChangedListener { _, destination, _ ->
                                if (destination.route != Routes.Splash.route) {
                                    showSplash = false
                                }
                            }
                        }

                    } else {

                        MainScreen(navController = navController) { paddingModifier ->
                            AppNavHost(
                                navController = navController,
                                startDestination = Routes.Splash.route,
                                appContainer = (application as LovelyPrintsApp).appContainer,
                                modifier = paddingModifier
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun FixSystemBars(
    enabled: Boolean
) {
    val view = LocalView.current
    // 🎨 Use your actual hardcoded background color instead of MaterialTheme
    val backgroundColor = Color(0xFF151419)

    SideEffect {
        if (!enabled) return@SideEffect

        val window = (view.context as Activity).window

        window.statusBarColor = backgroundColor.toArgb()
        window.navigationBarColor = backgroundColor.toArgb()

        WindowCompat.getInsetsController(window, view).apply {
            // ✅ Set to false because you're using dark background
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}