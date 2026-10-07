package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberBlue,
    secondary = CyberGreen,
    tertiary = CyberAlertWarning,
    background = CyberDarkBg,
    surface = CyberSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = CyberTextPrimary,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberCard,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // Force cybersecurity dark mode theme as NEXUS is a dark SOC operations terminal
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}

