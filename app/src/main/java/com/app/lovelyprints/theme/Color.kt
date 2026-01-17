package com.app.lovelyprints.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Color.kt
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Primary = Color(0xFF6200EE)
val Secondary = Color(0xFF03DAC6)
val Error = Color(0xFFB00020)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    error = Error,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

//Background
val backgroundLight = Color(0xFFFAFAFA)
val SurfaceLight = Color(0xFFFFFFFF)

//Brand
val LpuOrange = Color(0xFFF58220)
val LpuBlue = Color(0xFF7AA6B3)

//Text
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF616161)

//Accent / states
val AccentBlue = Color(0xFF1565C0)
val SuccessGreen = Color(0xFF2E7D32)
val ErrorRed = Color(0xFFC62828)

//Other
val DividerGray = Color(0xFFE0E0E0)