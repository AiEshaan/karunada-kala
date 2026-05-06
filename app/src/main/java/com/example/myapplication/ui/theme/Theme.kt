package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Karunada Kala Design System
 * "Minimalist Heritage"
 */

private val HeritageLightColorScheme = lightColorScheme(
    primary = ArtBrand,
    secondary = ArtTerra,
    tertiary = ArtClay,
    background = ArtBG,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ArtBrand,
    onSurface = ArtBrand,
    error = ErrorCrimson
)

// Dark theme is kept subtle for heritage feel
private val HeritageDarkColorScheme = darkColorScheme(
    primary = ArtBrandDark,
    secondary = ArtTerraDark,
    tertiary = ArtClayDark,
    background = ArtBGDark,
    surface = ArtSurfaceDark,
    onPrimary = ArtBGDark,
    onSecondary = Color.Black,
    onBackground = ArtClayDark,
    onSurface = ArtClayDark,
    error = ErrorCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> HeritageDarkColorScheme
        else -> HeritageLightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
