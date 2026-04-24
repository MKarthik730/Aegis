package com.karthik.aegis.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val AegisBlue = Color(0xFF1976D2)
val AegisBlueDark = Color(0xFF0D47A1)
val AegisBlueLight = Color(0xFF42A5F5)
val AegisGreen = Color(0xFF4CAF50)
val AegisRed = Color(0xFFE53935)
val AegisOrange = Color(0xFFFF9800)
val AegisYellow = Color(0xFFFFEB3B)

private val DarkColorScheme = darkColorScheme(
    primary = AegisBlueLight,
    onPrimary = Color.White,
    primaryContainer = AegisBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AegisGreen,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = AegisRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AegisBlue,
    onPrimary = Color.White,
    primaryContainer = AegisBlueLight,
    onPrimaryContainer = Color.White,
    secondary = AegisGreen,
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = AegisRed,
    onError = Color.White
)

@Composable
fun AegisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}