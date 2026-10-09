package com.buggedlab.pocketops.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OneUiBlue,
    onPrimary = Color.White,
    primaryContainer = OneUiBlueVariant,
    onPrimaryContainer = Color.White,
    secondary = OneUiTeal,
    onSecondary = Color.Black,
    background = OneUiDarkBackground,
    onBackground = TextPrimary,
    surface = OneUiDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = OneUiDarkSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = RiskHighRed,
    onError = Color.White
)

@Composable
fun PocketOpsTheme(
    darkTheme: Boolean = true, // Default to clean dark interface
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
