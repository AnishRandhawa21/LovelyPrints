package com.app.lovelyprints.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
@Composable
fun LovelyPrintsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
//    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
    Typography(
        bodyLarge = TextStyle(
            fontFamily = ImpactFont
        ),
        titleLarge = TextStyle(
            fontFamily = ImpactFont,
            fontWeight = FontWeight.Bold
        )
    )
    Typography(
        bodyLarge = TextStyle(
            fontFamily = Montserrat
        ),
        titleLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold
        )
    )


}