package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Dark OLED Palette (#0B0E14)
val OledBackground = Color(0xFF0B0E14)
val OledSurface = Color(0xFF141923)
val OledSurfaceVariant = Color(0xFF1E2433)
val OledPrimary = Color(0xFF00E5FF)
val OledSecondary = Color(0xFF7C4DFF)
val OledAccent = Color(0xFFFF5252)
val OledTextPrimary = Color(0xFFFFFFFF)
val OledTextSecondary = Color(0xFFA0ABC0)

// Vibrant Neon / Cyberpunk Palette
val NeonBackground = Color(0xFF08020F)
val NeonSurface = Color(0xFF1A0A2E)
val NeonSurfaceVariant = Color(0xFF281145)
val NeonPrimary = Color(0xFF00F5FF)
val NeonSecondary = Color(0xFFFF007F)
val NeonAccent = Color(0xFFFFE600)
val NeonTextPrimary = Color(0xFFFFFFFF)
val NeonTextSecondary = Color(0xFFD4AF37)

// Clean Light Palette
val LightBackground = Color(0xFFF4F6F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE9ECEF)
val LightPrimary = Color(0xFF1E56A0)
val LightSecondary = Color(0xFF00A8CC)
val LightTextPrimary = Color(0xFF1A1A24)
val LightTextSecondary = Color(0xFF6C757D)

enum class AppThemeMode {
    DARK_OLED,
    VIBRANT_NEON,
    CLEAN_LIGHT
}
