package org.example.project.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    background = AppBackground,
    surface = AppSurface,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = AccentDanger
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    background = AppBackground,
    surface = AppSurface,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = AccentDanger
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
