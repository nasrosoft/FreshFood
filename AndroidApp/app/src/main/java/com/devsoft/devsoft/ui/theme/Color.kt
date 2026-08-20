package com.devsoft.devsoft.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Static Base Palette - Brand Royal Blue & Accents
val PrimaryBlue = Color(0xFF2563EB) // Blue 600
val PrimaryBlueLight = Color(0xFF3B82F6) // Blue 500
val PrimaryBlueDark = Color(0xFF1D4ED8) // Blue 700

val AccentSky = Color(0xFF0EA5E9) // Sky 500
val AccentIndigo = Color(0xFF4F46E5) // Indigo 600

// Static Base Palette - Light Theme Constants
val LightAppBackground = Color(0xFFF8FAFC) // Slate 50
val LightCardSurface = Color(0xFFFFFFFF) // Pure White
val LightCardBorder = Color(0xFFE2E8F0) // Slate 200
val LightCardSurfaceVariant = Color(0xFFF1F5F9) // Slate 100
val LightPrimaryBlueContainer = Color(0xFFEFF6FF) // Blue 50
val LightOnPrimaryBlueContainer = Color(0xFF1E40AF)
val LightTextDark = Color(0xFF0F172A) // Slate 900
val LightTextMuted = Color(0xFF64748B) // Slate 500
val LightTextSubtle = Color(0xFF94A3B8) // Slate 400

// Static Base Palette - Dark Theme Constants
val DarkAppBackground = Color(0xFF0F172A) // Slate 900 Deep Canvas
val DarkCardSurface = Color(0xFF1E293B) // Slate 800 Elevated Card
val DarkCardBorder = Color(0xFF334155) // Slate 700
val DarkCardSurfaceVariant = Color(0xFF243044) // Slate 750 Surface Variant
val DarkPrimaryBlueContainer = Color(0xFF1E293B) // Slate 800 Container
val DarkOnPrimaryBlueContainer = Color(0xFF93C5FD) // Blue 300
val DarkTextLight = Color(0xFFF8FAFC) // Slate 50
val DarkTextMuted = Color(0xFF94A3B8) // Slate 400
val DarkTextSubtle = Color(0xFF64748B) // Slate 500

// Semantic Status Colors (shared base)
val StatusSuccess = Color(0xFF10B981) // Emerald 500
val StatusSuccessLight = Color(0xFF34D399) // Emerald 400
val StatusWarning = Color(0xFFF59E0B) // Amber 500
val StatusWarningLight = Color(0xFFFBBF24) // Amber 400
val StatusError = Color(0xFFEF4444) // Red 500
val StatusErrorLight = Color(0xFFF87171) // Red 400
val StatusInfo = Color(0xFF3B82F6) // Blue 500

// Dynamic Theme-Aware Tokens for Composable Usage
val AppBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val CardSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val CardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val CardSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val PrimaryBlueContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkPrimaryBlueContainer else LightPrimaryBlueContainer

val OnPrimaryBlueContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkOnPrimaryBlueContainer else LightOnPrimaryBlueContainer

val TextDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkTextSubtle else LightTextSubtle

val StatusSuccessContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)

val StatusWarningContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF78350F).copy(alpha = 0.5f) else Color(0xFFFEF3C7)

val StatusErrorContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF7F1D1D).copy(alpha = 0.5f) else Color(0xFFFEE2E2)

val StatusInfoContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFEFF6FF)

