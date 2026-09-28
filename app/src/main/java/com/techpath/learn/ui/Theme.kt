package com.techpath.learn.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF39A0FF),
    secondary = Color(0xFF55D68B),
    tertiary = Color(0xFFFFB74D),
    background = Color(0xFF08111D),
    surface = Color(0xFF101D2D),
    surfaceVariant = Color(0xFF17283C),
    onPrimary = Color.White,
    onSecondary = Color(0xFF04150B),
    onBackground = Color(0xFFF4F7FB),
    onSurface = Color(0xFFF4F7FB),
    onSurfaceVariant = Color(0xFFB9C7D8),
    error = Color(0xFFFF6B6B)
)

@Composable
fun TechPathTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, typography = Typography(), content = content)
}
