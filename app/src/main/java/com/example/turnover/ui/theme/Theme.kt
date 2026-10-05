package com.example.turnover.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DadsBlue,
    onPrimary = DadsBgSurface,
    primaryContainer = DadsBlueSubtle,
    onPrimaryContainer = DadsBlueActive,
    secondary = DadsBlue,
    onSecondary = DadsBgSurface,
    secondaryContainer = DadsBlueSubtle,
    onSecondaryContainer = DadsBlueActive,
    background = DadsBgCanvas,
    onBackground = DadsTextPrimary,
    surface = DadsBgSurface,
    onSurface = DadsTextPrimary,
    surfaceVariant = DadsBgCanvas,
    onSurfaceVariant = DadsTextSecondary,
    outline = DadsBorder
)

@Composable
fun TurnOverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
