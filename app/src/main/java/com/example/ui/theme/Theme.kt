package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EthioEmeraldPrimaryDark,
    onPrimary = EthioEmeraldDark,
    primaryContainer = EthioEmeraldDark,
    onPrimaryContainer = EthioEmeraldLight,
    secondary = EthioGoldSecondaryDark,
    onSecondary = EthioGoldDark,
    secondaryContainer = EthioGoldDark,
    onSecondaryContainer = EthioGoldContainer,
    tertiary = EthioCrimsonTertiaryDark,
    background = SiraBackgroundDark,
    surface = SiraSurfaceDark,
    surfaceVariant = SiraSurfaceVariantDark,
    onBackground = SiraTextPrimaryDark,
    onSurface = SiraTextPrimaryDark,
    outline = SiraOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EthioEmeraldPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = EthioEmeraldContainer,
    onPrimaryContainer = EthioEmeraldDark,
    secondary = EthioGoldSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = EthioGoldContainer,
    onSecondaryContainer = EthioGoldDark,
    tertiary = EthioCrimsonTertiary,
    background = SiraBackgroundLight,
    surface = SiraSurfaceLight,
    surfaceVariant = SiraSurfaceVariantLight,
    onBackground = SiraTextPrimaryLight,
    onSurface = SiraTextPrimaryLight,
    outline = SiraOutlineLight
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
