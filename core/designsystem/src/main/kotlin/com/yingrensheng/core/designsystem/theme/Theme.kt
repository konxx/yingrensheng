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
    primary = AmberAccent,
    onPrimary = WarmSurface,
    secondary = SlateBlue,
    tertiary = WineAccent,
    background = FilmPaper,
    surface = WarmSurface,
    onBackground = InkGreen,
    onSurface = InkGreen,
)

private val DarkColors = darkColorScheme(
    primary = AmberAccent,
    secondary = SlateBlue,
    tertiary = WineAccent,
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
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(FilmPaper, Mist, FilmPaperDeep),
                        ),
                    ),
            ) {
                content()
            }
        },
    )
}

