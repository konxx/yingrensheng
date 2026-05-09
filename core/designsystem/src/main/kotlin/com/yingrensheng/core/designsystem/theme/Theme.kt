package com.yingrensheng.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

private val LightColors = lightColorScheme(
    primary = WeUiGreen,
    onPrimary = WeUiSurface,
    secondary = WeUiTextSecondary,
    tertiary = WeUiQr,
    background = WeUiBackground,
    surface = WeUiSurface,
    onBackground = WeUiTextPrimary,
    onSurface = WeUiTextPrimary,
    outline = WeUiBorder,
)

private val DarkColors = darkColorScheme(
    primary = WeUiGreen,
    secondary = WeUiTextSecondary,
    tertiary = WeUiQr,
)

@Composable
fun YingRenShengTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = YingRenShengTypography,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WeUiBackground),
            ) {
                content()
            }
        },
    )
}
