package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BentoIndigo,
    onPrimary = Color.White,
    primaryContainer = BentoIndigoDark,
    onPrimaryContainer = BentoSlateLight,
    secondary = BentoAmber,
    onSecondary = Color(0xFF1A1C1E),
    secondaryContainer = Color(0xFF452B00),
    onSecondaryContainer = BentoAmberLight,
    tertiary = BentoRose,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C0519),
    onTertiaryContainer = Color(0xFFFFE4E6),
    background = BentoBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = BentoCardSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = BentoCardElevated,
    onSurfaceVariant = BentoSlateLight,
    outline = BentoCardBorder,
    error = BentoRose,
    onError = Color.White
)

@Composable
fun LudoBlitzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Bento Grid Dark Slate Theme
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

