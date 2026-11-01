package com.example.sentinel_ip.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkSecurityColorScheme = darkColorScheme(
    primary = CyberRedPrimary,
    onPrimary = CyberRedOnPrimary,
    primaryContainer = CyberRedContainer,
    onPrimaryContainer = CyberRedOnContainer,
    secondary = CyberRedSecondary,
    onSecondary = CyberRedOnSecondary,
    secondaryContainer = CyberRedSecondaryContainer,
    onSecondaryContainer = CyberRedOnSecondaryContainer,
    background = CyberBackground,
    onBackground = CyberOnBackground,
    surface = CyberSurface,
    onSurface = CyberOnSurface,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberOnSurfaceVariant,
    outline = CyberOutline,
    error = ThreatDanger,
    onError = CyberRedOnPrimary
)

private val LightSecurityColorScheme = lightColorScheme(
    primary = CyberRedPrimary,
    onPrimary = CyberRedOnPrimary,
    primaryContainer = CyberRedContainer,
    onPrimaryContainer = CyberRedOnContainer,
    secondary = CyberRedSecondary,
    onSecondary = CyberRedOnSecondary,
    secondaryContainer = CyberRedSecondaryContainer,
    onSecondaryContainer = CyberRedOnSecondaryContainer,
    background = CyberBackground,
    onBackground = CyberOnBackground,
    surface = CyberSurface,
    onSurface = CyberOnSurface,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberOnSurfaceVariant,
    outline = CyberOutline,
    error = ThreatDanger,
    onError = CyberRedOnPrimary
)

@Composable
fun SentinelIPTheme(
    darkTheme: Boolean = true, // Default to dark cyber security theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkSecurityColorScheme else LightSecurityColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
