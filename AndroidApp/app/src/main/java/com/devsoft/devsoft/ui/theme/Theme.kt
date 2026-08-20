package com.devsoft.devsoft.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryBlueContainer,
    onPrimaryContainer = LightOnPrimaryBlueContainer,
    secondary = AccentSky,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFF6FF),
    onSecondaryContainer = PrimaryBlueDark,
    tertiary = AccentIndigo,
    onTertiary = Color.White,
    background = LightAppBackground,
    onBackground = LightTextDark,
    surface = LightCardSurface,
    onSurface = LightTextDark,
    surfaceVariant = LightCardSurfaceVariant,
    onSurfaceVariant = LightTextMuted,
    outline = LightCardBorder,
    error = StatusError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = StatusError
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = DarkPrimaryBlueContainer,
    onPrimaryContainer = DarkOnPrimaryBlueContainer,
    secondary = AccentSky,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = PrimaryBlueLight,
    tertiary = AccentIndigo,
    onTertiary = Color.White,
    background = DarkAppBackground,
    onBackground = DarkTextLight,
    surface = DarkCardSurface,
    onSurface = DarkTextLight,
    surfaceVariant = DarkCardSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkCardBorder,
    error = StatusErrorLight,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA)
)

@Composable
fun DevsoftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

