package com.app.lovelyprints.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.R
import com.app.lovelyprints.viewmodel.SplashViewModelFactory

@Composable
fun SplashScreen(
    viewModelFactory: SplashViewModelFactory,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val viewModel: SplashViewModel = viewModel(factory = viewModelFactory)
    val navigationDestination by viewModel.navigationDestination.collectAsState()

    var startAnimation by remember { mutableStateOf(false) }

    // Fade in animation
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )

    // Ring rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(navigationDestination) {
        when (navigationDestination) {
            "login" -> onNavigateToLogin()
            "main" -> onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B1E)),
        contentAlignment = Alignment.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.lovely_prints),
            contentDescription = "Lovely Prints Logo",
            modifier = Modifier
                .size(80.dp)
                .alpha(alphaAnim.value)
        )

        // Loading ring around logo (much closer)
        Canvas(
            modifier = Modifier
                .size(82.dp)
                .alpha(alphaAnim.value)
        ) {
            drawArc(
                color = Color(0xFFFF8C00), // Orange color to match your logo
                startAngle = rotation,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}