package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkOledColorScheme = darkColorScheme(
    primary = OledPrimary,
    secondary = OledSecondary,
    tertiary = OledAccent,
    background = OledBackground,
    surface = OledSurface,
    surfaceVariant = OledSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = OledTextPrimary,
    onSurface = OledTextPrimary,
    onSurfaceVariant = OledTextSecondary
)

private val VibrantNeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    secondary = NeonSecondary,
    tertiary = NeonAccent,
    background = NeonBackground,
    surface = NeonSurface,
    surfaceVariant = NeonSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = NeonTextPrimary,
    onSurface = NeonTextPrimary,
    onSurfaceVariant = NeonTextSecondary
)

private val CleanLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary
)

@Composable
fun StreamFlowTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_OLED,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.DARK_OLED -> DarkOledColorScheme
        AppThemeMode.VIBRANT_NEON -> VibrantNeonColorScheme
        AppThemeMode.CLEAN_LIGHT -> CleanLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
