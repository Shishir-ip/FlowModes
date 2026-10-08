package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class FlowGlassColors(
    val glassSurface: Color,
    val glassBorder: Color,
    val glassCardHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentCyan: Color = FlowCyan,
    val accentIndigo: Color = FlowIndigo,
    val accentRose: Color = FlowRose
)

val LocalFlowGlassColors = staticCompositionLocalOf {
    FlowGlassColors(
        glassSurface = DarkGlassSurface,
        glassBorder = DarkGlassBorder,
        glassCardHighlight = Color(0x1AFFFFFF),
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        textMuted = DarkTextMuted
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = FlowCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0C354C),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = FlowIndigo,
    onSecondary = Color.Black,
    tertiary = FlowViolet,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder
)

private val OledColorScheme = darkColorScheme(
    primary = FlowCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0A293B),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = FlowIndigo,
    onSecondary = Color.Black,
    tertiary = FlowViolet,
    background = OledBackground,
    surface = OledSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    tertiary = Color(0xFF8B5CF6),
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder
)

@Composable
fun FlowModesTheme(
    themeMode: String = "system", // "system", "dark", "light"
    oledBlack: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark && oledBlack -> OledColorScheme
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val glassColors = if (isDark) {
        if (oledBlack) {
            FlowGlassColors(
                glassSurface = OledGlassSurface,
                glassBorder = Color(0x2EFFFFFF),
                glassCardHighlight = Color(0x1FFFFFFF),
                textPrimary = DarkTextPrimary,
                textSecondary = DarkTextSecondary,
                textMuted = DarkTextMuted
            )
        } else {
            FlowGlassColors(
                glassSurface = DarkGlassSurface,
                glassBorder = DarkGlassBorder,
                glassCardHighlight = Color(0x1AFFFFFF),
                textPrimary = DarkTextPrimary,
                textSecondary = DarkTextSecondary,
                textMuted = DarkTextMuted
            )
        }
    } else {
        FlowGlassColors(
            glassSurface = LightGlassSurface,
            glassBorder = LightGlassBorder,
            glassCardHighlight = Color(0x66FFFFFF),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalFlowGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
