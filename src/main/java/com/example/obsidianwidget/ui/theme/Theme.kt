package com.example.obsidianwidget.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ObsidianPurple80,
    onPrimary = ObsidianGrey10,
    primaryContainer = ObsidianPurple30,
    onPrimaryContainer = ObsidianPurple90,
    secondary = ObsidianBlue80,
    onSecondary = ObsidianGrey10,
    secondaryContainer = ObsidianBlue30,
    onSecondaryContainer = ObsidianBlue90,
    tertiary = ObsidianTeal80,
    onTertiary = ObsidianGrey10,
    tertiaryContainer = ObsidianTeal30,
    onTertiaryContainer = ObsidianTeal90,
    error = ObsidianRed80,
    onError = ObsidianRed20,
    errorContainer = ObsidianRed30,
    onErrorContainer = ObsidianRed90,
    background = ObsidianGrey10,
    onBackground = ObsidianGrey90,
    surface = ObsidianGrey10,
    onSurface = ObsidianGrey90,
    surfaceVariant = ObsidianGrey30,
    onSurfaceVariant = ObsidianGrey80,
    outline = ObsidianGrey60
)

private val ObsidianLightColorScheme = lightColorScheme(
    primary = ObsidianPurple40,
    onPrimary = ObsidianGrey100,
    primaryContainer = ObsidianPurple90,
    onPrimaryContainer = ObsidianPurple10,
    secondary = ObsidianBlue40,
    onSecondary = ObsidianGrey100,
    secondaryContainer = ObsidianBlue90,
    onSecondaryContainer = ObsidianBlue10,
    tertiary = ObsidianTeal40,
    onTertiary = ObsidianGrey100,
    tertiaryContainer = ObsidianTeal90,
    onTertiaryContainer = ObsidianTeal10,
    error = ObsidianRed40,
    onError = ObsidianGrey100,
    errorContainer = ObsidianRed90,
    onErrorContainer = ObsidianRed10,
    background = ObsidianGrey99,
    onBackground = ObsidianGrey10,
    surface = ObsidianGrey99,
    onSurface = ObsidianGrey10,
    surfaceVariant = ObsidianGrey90,
    onSurfaceVariant = ObsidianGrey30,
    outline = ObsidianGrey50
)

@Composable
fun ObsidianWidgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> ObsidianDarkColorScheme
        else -> ObsidianLightColorScheme
    }
    
    // Google Keep風のShape設定
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(24.dp)
    )
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes,
        content = content
    )
}