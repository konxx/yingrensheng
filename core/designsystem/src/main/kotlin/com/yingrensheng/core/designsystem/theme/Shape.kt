package com.yingrensheng.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

val YingRenShengShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Immutable
data class YrsRadius(
    val control: RoundedCornerShape = RoundedCornerShape(14.dp),
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(24.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp),
)

val LocalYrsRadius = androidx.compose.runtime.staticCompositionLocalOf { YrsRadius() }
