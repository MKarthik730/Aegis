package com.karthik.aegis.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// --- Color Palette ---
// Deep navy/charcoal dark background (#0D1117)
// Electric blue primary (#2979FF)
// Red SOS/error (#FF3D3D)

// Dark theme - always used (dynamicColor = false)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2979FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0043A0),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF00C853),
    onSecondary = Color(0xFF003A00),
    secondaryContainer = Color(0xFF004D1A),
    onSecondaryContainer = Color(0xFFA5D6A7),
    tertiary = Color(0xFFFF5252),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFFF3D3D),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5C0000),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF8B949E),
    outline = Color(0xFF6E7681),
    outlineVariant = Color(0xFF30363D),
)

// Light theme - clean, professional
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2979FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF00C853),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB9F6CA),
    onSecondaryContainer = Color(0xFF003300),
    tertiary = Color(0xFFE91E63),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFFF3D3D),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF0F4F8),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
)

val AegisShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AegisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AegisShapes,
        content = content
    )
}
