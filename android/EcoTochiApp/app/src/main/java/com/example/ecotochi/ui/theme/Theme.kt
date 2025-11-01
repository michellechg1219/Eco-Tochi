package com.example.ecotochi.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EcoLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenPrimary,

    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,

    background = AppBackground,
    onBackground = TextOnLight,

    surface = AppBackground,          // 👈 superficies blancas
    onSurface = TextOnLight,

    error = RedError,
    onError = OnError,
)

@Composable
fun EcoTochiTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = EcoLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // status bar con iconos oscuros sobre barra clara -> false
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,   // usa tu Typography.kt
        content = content
    )
}
