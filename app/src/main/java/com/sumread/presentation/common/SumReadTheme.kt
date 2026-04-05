package com.sumread.presentation.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF195B7A),
    onPrimary = Color.White,
    secondary = Color(0xFF5A6B7B),
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F1720),
    onSurface = Color(0xFF0F1720),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BC1DC),
    onPrimary = Color(0xFF002838),
    secondary = Color(0xFFB3C5D2),
    background = Color(0xFF0D1720),
    surface = Color(0xFF14222D),
    onBackground = Color(0xFFE8F0F5),
    onSurface = Color(0xFFE8F0F5),
)

@Composable
fun SumReadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
