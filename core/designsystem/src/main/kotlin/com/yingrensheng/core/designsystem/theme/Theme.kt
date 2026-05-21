package com.yingrensheng.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColors = lightColorScheme(
    primary = YrsSage,
    onPrimary = ColorTokens.onPrimary,
    primaryContainer = ColorTokens.primaryContainer,
    onPrimaryContainer = ColorTokens.onPrimaryContainer,
    secondary = YrsSlate,
    onSecondary = ColorTokens.onPrimary,
    secondaryContainer = ColorTokens.secondaryContainer,
    onSecondaryContainer = ColorTokens.onSecondaryContainer,
    tertiary = YrsAmber,
    onTertiary = ColorTokens.onPrimary,
    background = YrsMist,
    onBackground = YrsInk,
    surface = YrsSurface,
    onSurface = YrsInk,
    surfaceVariant = YrsCloud,
    onSurfaceVariant = YrsMutedInk,
    outline = YrsHairline,
    outlineVariant = ColorTokens.outlineVariant,
    error = YrsTerracotta,
    onError = ColorTokens.onPrimary,
)

private val DarkColors = darkColorScheme(
    primary = ColorTokens.darkPrimary,
    onPrimary = ColorTokens.darkOnPrimary,
    primaryContainer = ColorTokens.darkPrimaryContainer,
    onPrimaryContainer = ColorTokens.darkOnPrimaryContainer,
    secondary = ColorTokens.darkSecondary,
    onSecondary = ColorTokens.darkOnPrimary,
    secondaryContainer = ColorTokens.darkSecondaryContainer,
    onSecondaryContainer = ColorTokens.darkOnSecondaryContainer,
    tertiary = ColorTokens.darkTertiary,
    onTertiary = ColorTokens.darkOnPrimary,
    background = ColorTokens.darkBackground,
    onBackground = ColorTokens.darkOnSurface,
    surface = ColorTokens.darkSurface,
    onSurface = ColorTokens.darkOnSurface,
    surfaceVariant = ColorTokens.darkSurfaceVariant,
    onSurfaceVariant = ColorTokens.darkOnSurfaceVariant,
    outline = ColorTokens.darkOutline,
    outlineVariant = ColorTokens.darkOutlineVariant,
    error = ColorTokens.darkError,
    onError = ColorTokens.darkOnPrimary,
)

@Composable
fun YingRenShengTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(
        LocalYrsSpacing provides YrsSpacing(),
        LocalYrsRadius provides YrsRadius(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = YingRenShengTypography,
            shapes = YingRenShengShapes,
            content = content,
        )
    }
}

private object ColorTokens {
    val onPrimary = androidx.compose.ui.graphics.Color.White
    val primaryContainer = androidx.compose.ui.graphics.Color(0xFFDDE7DF)
    val onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF1E2F25)
    val secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE3E8EF)
    val onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF243043)
    val outlineVariant = androidx.compose.ui.graphics.Color(0xFFECEFEB)
    val darkPrimary = androidx.compose.ui.graphics.Color(0xFFB7C9BC)
    val darkOnPrimary = androidx.compose.ui.graphics.Color(0xFF162017)
    val darkPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF304137)
    val darkOnPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFDDE7DF)
    val darkSecondary = androidx.compose.ui.graphics.Color(0xFFC3CAD6)
    val darkSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF343B48)
    val darkOnSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFE5E9F0)
    val darkTertiary = androidx.compose.ui.graphics.Color(0xFFE0C484)
    val darkBackground = androidx.compose.ui.graphics.Color(0xFF111312)
    val darkSurface = androidx.compose.ui.graphics.Color(0xFF181B19)
    val darkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF202420)
    val darkOnSurface = androidx.compose.ui.graphics.Color(0xFFE7EAE5)
    val darkOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFAEB7AF)
    val darkOutline = androidx.compose.ui.graphics.Color(0xFF3A413B)
    val darkOutlineVariant = androidx.compose.ui.graphics.Color(0xFF282D29)
    val darkError = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
}
