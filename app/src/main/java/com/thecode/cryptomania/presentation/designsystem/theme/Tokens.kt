package com.thecode.cryptomania.presentation.designsystem.theme

import com.thecode.cryptomania.R
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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
 * Ubuntu, carried over from CryptoMania 1.x: a humanist face with character for headlines,
 * and tabular digits by default, so prices in a list line up and never jitter horizontally
 * when they update.
 */
val Ubuntu = FontFamily(
    Font(R.font.ubuntu_regular, FontWeight.Normal),
    Font(R.font.ubuntu_medium, FontWeight.Medium),
    Font(R.font.ubuntu_bold, FontWeight.Bold),
)

private fun style(family: FontFamily, size: Int, weight: FontWeight, lineHeight: Int, tracking: Double = 0.0) = TextStyle(
    // Trimmed line boxes keep a subtitle snug under a title.
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    fontFeatureSettings = "tnum",
)

val CryptoManiaTypography = Typography(
    // Figures: hero price, total market cap.
    displayLarge = style(Ubuntu, 40, FontWeight.Bold, 48, -0.02),
    displayMedium = style(Ubuntu, 34, FontWeight.Bold, 42, -0.02),
    displaySmall = style(Ubuntu, 28, FontWeight.Bold, 36, -0.015),
    // Words: screen titles, onboarding, section headers.
    headlineLarge = style(Ubuntu, 30, FontWeight.Bold, 36, -0.01),
    headlineMedium = style(Ubuntu, 26, FontWeight.Bold, 32, -0.01),
    headlineSmall = style(Ubuntu, 22, FontWeight.Bold, 28),
    titleLarge = style(Ubuntu, 20, FontWeight.Bold, 26),
    titleMedium = style(Ubuntu, 16, FontWeight.Medium, 22),
    titleSmall = style(Ubuntu, 14, FontWeight.Medium, 20),
    bodyLarge = style(Ubuntu, 16, FontWeight.Normal, 24),
    bodyMedium = style(Ubuntu, 14, FontWeight.Normal, 20),
    bodySmall = style(Ubuntu, 12, FontWeight.Normal, 16),
    labelLarge = style(Ubuntu, 14, FontWeight.Medium, 20),
    labelMedium = style(Ubuntu, 12, FontWeight.Medium, 16, 0.01),
    labelSmall = style(Ubuntu, 11, FontWeight.Medium, 14, 0.02),
)
