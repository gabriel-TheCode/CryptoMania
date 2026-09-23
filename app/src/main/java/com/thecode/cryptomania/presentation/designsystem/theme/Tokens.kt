package com.thecode.cryptomania.presentation.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** 4 dp grid. */
@Immutable
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
)

@Immutable
data class Sizes(
    val logoSmall: Dp = 28.dp,
    val logoMedium: Dp = 36.dp,
    val logoLarge: Dp = 44.dp,
    val minTouchTarget: Dp = 48.dp,
    val listItemMinHeight: Dp = 64.dp,
    val sparklineWidth: Dp = 64.dp,
    val sparklineHeight: Dp = 28.dp,
    val chartHeight: Dp = 240.dp,
    /** Readable line length for text-heavy content on large screens. */
    val maxContentWidth: Dp = 840.dp,
)

/**
 * Motion rules: animate state changes, not decoration.
 *
 * Interactive components use Material 3 Expressive spring physics: "spatial" springs for
 * things that move (a slight overshoot makes selection feel physical), critically damped
 * "effects" springs for color/opacity. Durations remain for content swaps and chart reveals.
 */
object Motion {
    const val SHORT = 150
    const val MEDIUM = 250
    const val LONG = 450
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> spatial(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = 380f)
    fun <T> effects(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 1600f)
}

val CryptoManiaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * System sans-serif with tabular figures everywhere: prices in a list line up digit by digit
 * and do not jitter horizontally when they update.
 */
private fun style(size: Int, weight: FontWeight, lineHeight: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    fontFeatureSettings = "tnum",
)

val CryptoManiaTypography = Typography(
    displayLarge = style(40, FontWeight.SemiBold, 48, -0.02),
    displayMedium = style(34, FontWeight.SemiBold, 42, -0.02),
    displaySmall = style(28, FontWeight.SemiBold, 36, -0.015),
    headlineLarge = style(26, FontWeight.SemiBold, 32, -0.01),
    headlineMedium = style(22, FontWeight.SemiBold, 28, -0.01),
    headlineSmall = style(20, FontWeight.SemiBold, 26),
    titleLarge = style(18, FontWeight.SemiBold, 24),
    titleMedium = style(16, FontWeight.SemiBold, 22),
    titleSmall = style(14, FontWeight.Medium, 20),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(14, FontWeight.Normal, 20),
    bodySmall = style(12, FontWeight.Normal, 16),
    labelLarge = style(14, FontWeight.Medium, 20),
    labelMedium = style(12, FontWeight.Medium, 16, 0.01),
    labelSmall = style(11, FontWeight.Medium, 14, 0.02),
)
