package com.thecode.cryptomania.presentation.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic palette. Screens never reference raw hex values: they ask for a role
 * (surface, textSecondary, positive, ...) so both themes and the color-blind mode stay coherent.
 */
@Immutable
data class CryptoManiaColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceHigh: Color,
    val outline: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val brand: Color,
    val onBrand: Color,
    val brandContainer: Color,
    val onBrandContainer: Color,
    /** Price up. Always paired with an arrow/sign so direction never relies on color alone. */
    val positive: Color,
    val positiveContainer: Color,
    /** Price down. */
    val negative: Color,
    val negativeContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val skeleton: Color,
    val skeletonHighlight: Color,
)

private val InkDark = CryptoManiaColors(
    isDark = true,
    background = Color(0xFF0A0D14),
    surface = Color(0xFF10141D),
    surfaceRaised = Color(0xFF151A25),
    surfaceHigh = Color(0xFF1D2331),
    outline = Color(0xFF2A3143),
    divider = Color(0xFF1A202C),
    textPrimary = Color(0xFFE9EDF5),
    textSecondary = Color(0xFF98A2B3),
    textTertiary = Color(0xFF6B7487),
    brand = Color(0xFF4D86FF),
    onBrand = Color(0xFFFFFFFF),
    brandContainer = Color(0xFF172750),
    onBrandContainer = Color(0xFFB4C9FF),
    positive = Color(0xFF26C28E),
    positiveContainer = Color(0xFF0E2B22),
    negative = Color(0xFFF2566F),
    negativeContainer = Color(0xFF34141C),
    warning = Color(0xFFF2B84B),
    warningContainer = Color(0xFF362A11),
    skeleton = Color(0xFF171C27),
    skeletonHighlight = Color(0xFF222938),
)

private val PaperLight = CryptoManiaColors(
    isDark = false,
    background = Color(0xFFF5F6F9),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFEDF0F5),
    outline = Color(0xFFD5DBE5),
    divider = Color(0xFFE7EAF0),
    textPrimary = Color(0xFF0D1220),
    textSecondary = Color(0xFF525D6E),
    textTertiary = Color(0xFF7C8699),
    brand = Color(0xFF2F6BEF),
    onBrand = Color(0xFFFFFFFF),
    brandContainer = Color(0xFFE2EAFF),
    onBrandContainer = Color(0xFF0F2F7A),
    // Darker than typical "trading green" so small text keeps a 4.5:1 contrast on white.
    positive = Color(0xFF0A7D55),
    positiveContainer = Color(0xFFDDF4EA),
    negative = Color(0xFFC9294A),
    negativeContainer = Color(0xFFFCE5E9),
    warning = Color(0xFF9A6410),
    warningContainer = Color(0xFFFCF0DA),
    skeleton = Color(0xFFE9ECF2),
    skeletonHighlight = Color(0xFFF5F6F9),
)

/** Blue/orange replaces green/red for users with red–green color vision deficiency. */
private fun CryptoManiaColors.colorBlindFriendly(): CryptoManiaColors = if (isDark) {
    copy(
        positive = Color(0xFF4AA3FF),
        positiveContainer = Color(0xFF0F2439),
        negative = Color(0xFFFF9F43),
        negativeContainer = Color(0xFF3A2410),
    )
} else {
    copy(
        positive = Color(0xFF1560C8),
        positiveContainer = Color(0xFFE0ECFD),
        negative = Color(0xFFB45309),
        negativeContainer = Color(0xFFFDEBD8),
    )
}

fun cryptoManiaColors(darkTheme: Boolean, colorBlindFriendly: Boolean): CryptoManiaColors {
    val base = if (darkTheme) InkDark else PaperLight
    return if (colorBlindFriendly) base.colorBlindFriendly() else base
}
