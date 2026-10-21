package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = EthioEmeraldPrimaryDark,
        onPrimary = Color(0xFF063A20),
        primaryContainer = Color(0xFF0A4E2C),
        onPrimaryContainer = Color(0xFFD6F5E3),
        secondary = EthioGoldSecondaryDark,
        onSecondary = Color(0xFF452600),
        secondaryContainer = Color(0xFF6B3C00),
        onSecondaryContainer = Color(0xFFFEF3C7),
        tertiary = EthioCrimsonTertiaryDark,
        onTertiary = Color(0xFF480909),
        tertiaryContainer = Color(0xFF6B1818),
        onTertiaryContainer = Color(0xFFFEE2E2),
        background = SiraBackgroundDark,
        onBackground = SiraTextPrimaryDark,
        surface = SiraSurfaceDark,
        onSurface = SiraTextPrimaryDark,
        surfaceVariant = SiraSurfaceVariantDark,
        onSurfaceVariant = SiraTextSecondaryDark,
        outline = SiraOutlineDark,
        outlineVariant = SiraOutlineVariantDark
    )

private val LightColorScheme =
    lightColorScheme(
        primary = EthioEmeraldPrimary,
        onPrimary = Color.White,
        primaryContainer = EthioEmeraldContainer,
        onPrimaryContainer = EthioEmeraldDark,
        secondary = EthioGoldSecondary,
        onSecondary = Color.White,
        secondaryContainer = EthioGoldContainer,
        onSecondaryContainer = EthioGoldDark,
        tertiary = EthioCrimsonTertiary,
        onTertiary = Color.White,
        tertiaryContainer = EthioCrimsonContainer,
        onTertiaryContainer = Color(0xFF5E1717),
        background = SiraBackgroundLight,
        onBackground = SiraTextPrimaryLight,
        surface = SiraSurfaceLight,
        onSurface = SiraTextPrimaryLight,
        surfaceVariant = SiraSurfaceVariantLight,
        onSurfaceVariant = SiraTextSecondaryLight,
        outline = SiraOutlineLight,
        outlineVariant = SiraOutlineVariantLight
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand colors consistent for SiraHub identity
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
