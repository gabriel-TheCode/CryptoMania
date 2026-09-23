package com.thecode.cryptomania.presentation.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.designsystem.theme.Motion

/**
 * The CryptoMania 1.x header wave, taken verbatim from its 375×94 vector and rescaled to any
 * size, so it adapts to phones, tablets and landscape instead of being a fixed drawable.
 */
private const val WAVE_PATH =
    "M218.175,94c-12.377,0 -20.53,-6.223 -28.414,-12.241 -7.732,-5.9 -15.035,-11.477 -25.914,-11.477 " +
        "-6.635,0 -14.211,3.722 -22.232,7.663 -9.173,4.507 -18.659,9.167 -28.153,9.167A49.856,49.856 0,0 1,99.292 85.1" +
        "a98.4,98.4 0,0 1,-11.67 -4.473c-6.838,-2.967 -13.909,-6.035 -23.809,-6.74 -2.925,-0.208 -5.9,-0.314 -8.851,-0.314" +
        "a131.471,131.471 0,0 0,-39.6 6.223A96.441,96.441 0,0 0,0 86.016L0,0L375,0v85.885a27.153,27.153 0,0 1,-5.007 0.278" +
        "c-4.412,0 -12.909,-0.366 -28.336,-2.107 -6.951,-0.785 -15.6,-2.942 -24.747,-5.225 -12.39,-3.091 -25.2,-6.288 " +
        "-35.023,-6.36h-0.342c-13.353,0 -23.858,5.765 -34.018,11.34C237.978,89.05 228.958,94 218.175,94Z"
private const val VIEWPORT_WIDTH = 375f
private const val VIEWPORT_HEIGHT = 94f

private val wavePath: Path by lazy { PathParser().parsePathString(WAVE_PATH).toPath() }

/** @param mirrored flips the wave horizontally, used for the translucent back layer. */
class WaveShape(private val mirrored: Boolean = false) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val matrix = Matrix().apply {
            if (mirrored) {
                translate(x = size.width)
                scale(x = -size.width / VIEWPORT_WIDTH, y = size.height / VIEWPORT_HEIGHT)
            } else {
                scale(x = size.width / VIEWPORT_WIDTH, y = size.height / VIEWPORT_HEIGHT)
            }
        }
        return Outline.Generic(Path().apply { addPath(wavePath); transform(matrix) })
    }
}

/**
 * Two stacked waves: a translucent mirrored one behind for depth, the brand gradient in front.
 * On first appearance the back layer glides into place, once, as a quiet signature motion.
 */
@Composable
fun WaveBackground(modifier: Modifier = Modifier) {
    val colors = CryptoManiaTheme.colors
    val settle = remember { Animatable(0f) }
    LaunchedEffect(Unit) { settle.animateTo(1f, Motion.spatial()) }
    Box(
        modifier.drawWithCache {
            val front = WaveShape().createOutline(size, layoutDirection, this) as Outline.Generic
            val back = WaveShape(mirrored = true).createOutline(size.copy(height = size.height * 1.08f), layoutDirection, this) as Outline.Generic
            val gradient = Brush.linearGradient(listOf(colors.waveStart, colors.waveEnd), end = Offset(size.width, size.height))
            val drift = 36.dp.toPx()
            onDrawBehind {
                translate(left = (1f - settle.value) * -drift) {
                    drawPath(back.path, colors.waveEnd.copy(alpha = 0.35f))
                }
                drawPath(front.path, gradient)
            }
        },
    )
}

/** Wave that extends behind the status bar, with content laid out below it. */
@Composable
fun WaveSurface(
    modifier: Modifier = Modifier,
    height: Dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(modifier.fillMaxWidth().height(statusBar + height)) {
        WaveBackground(Modifier.matchParentSize())
        CompositionLocalProvider(LocalContentColor provides CryptoManiaTheme.colors.onWave) {
            Box(Modifier.padding(top = statusBar).fillMaxWidth(), content = content)
        }
    }
}

/**
 * Brand top bar drawn on the wave. With a [scrollBehavior] it starts expanded (large Baloo
 * title plus subtitle) and collapses into a compact bar as content scrolls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoManiaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    expandedExtra: Dp = if (scrollBehavior != null) 36.dp else 0.dp,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val barHeight = 64.dp
    val edge = 26.dp
    val extraPx = with(LocalDensity.current) { expandedExtra.toPx() }
    if (scrollBehavior != null) SideEffect { scrollBehavior.state.heightOffsetLimit = -extraPx }
    // Without a scroll behavior the bar is simply compact.
    val collapsed = scrollBehavior?.state?.collapsedFraction ?: 1f
    val extra = lerp(expandedExtra, 0.dp, collapsed)
    val titleStyle = lerp(MaterialTheme.typography.headlineLarge, MaterialTheme.typography.titleLarge, collapsed)

    WaveSurface(modifier = modifier, height = barHeight + extra + edge) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(barHeight + extra)
                    .padding(start = if (onBack == null) spacing.lg else spacing.xs, end = spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = titleStyle,
                        color = colors.onWave,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onWave.copy(alpha = 0.78f),
                            maxLines = 1,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, content = actions)
            }
        }
    }
}
