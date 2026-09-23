package com.thecode.cryptomania.presentation.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.Typography

private val LocalColors = staticCompositionLocalOf { cryptoManiaColors(darkTheme = true, colorBlindFriendly = false) }
private val LocalSpacing = staticCompositionLocalOf { Spacing() }
private val LocalSizes = staticCompositionLocalOf { Sizes() }

/**
 * CryptoMania theme. Material 3 components receive a color scheme derived from the
 * CryptoMania palette (no dynamic color: the brand and the red/green semantics must stay stable),
 * while app components read the richer semantic tokens through [CryptoManiaTheme].
 */
@Composable
fun CryptoManiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorBlindFriendly: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = remember(darkTheme, colorBlindFriendly) { cryptoManiaColors(darkTheme, colorBlindFriendly) }
    val scheme = remember(colors) { colors.toMaterialScheme() }
    CompositionLocalProvider(LocalColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CryptoManiaTypography,
            shapes = CryptoManiaShapes,
            content = content,
        )
    }
}

object CryptoManiaTheme {
    val colors: CryptoManiaColors
        @Composable @ReadOnlyComposable get() = LocalColors.current
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
    val sizes: Sizes
        @Composable @ReadOnlyComposable get() = LocalSizes.current
    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography
}

private fun CryptoManiaColors.toMaterialScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandContainer,
        onPrimaryContainer = onBrandContainer,
        inversePrimary = brand,
        secondary = textSecondary,
        onSecondary = background,
        secondaryContainer = brandContainer,
        onSecondaryContainer = onBrandContainer,
        tertiary = warning,
        tertiaryContainer = warningContainer,
        background = background,
        onBackground = textPrimary,
        surface = background,
        onSurface = textPrimary,
        surfaceVariant = surfaceHigh,
        onSurfaceVariant = textSecondary,
        surfaceTint = brand,
        surfaceBright = surfaceHigh,
        surfaceDim = background,
        surfaceContainerLowest = background,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerHighest = surfaceHigh,
        inverseSurface = textPrimary,
        inverseOnSurface = background,
        outline = outline,
        outlineVariant = divider,
        error = negative,
        onError = onBrand,
        errorContainer = negativeContainer,
        onErrorContainer = negative,
        scrim = background.copy(alpha = 0.6f),
    )
}
