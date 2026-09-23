package com.thecode.cryptomania.presentation.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.util.SyncStatus
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

/** Height of the wavy edge. Fixed: taller headers stretch the flat part, never the waves. */
val WaveEdgeHeight: Dp = 28.dp

/** Viewport y where the original artwork stops being flat and the waves begin. */
private const val EDGE_TOP = 68f

private val rawWave: Path by lazy { PathParser().parsePathString(WAVE_PATH).toPath() }

/**
 * Builds the wave for a [width]×[height] box: the wavy part keeps a constant [edge] height at
 * the bottom and a flat rectangle fills everything above it (extended [bleed] above the top so
 * vertical motion never opens a gap). One path, so translucent layers never double-blend.
 */
private fun wavePath(width: Float, height: Float, edge: Float, bleed: Float, mirrored: Boolean): Path {
    val sx = width / VIEWPORT_WIDTH
    val sy = edge / (VIEWPORT_HEIGHT - EDGE_TOP)
    val matrix = Matrix().apply {
        translate(x = if (mirrored) width else 0f, y = height - VIEWPORT_HEIGHT * sy)
        scale(x = if (mirrored) -sx else sx, y = sy)
    }
    val waves = Path().apply { addPath(rawWave); transform(matrix) }
    val flat = Path().apply { addRect(Rect(0f, -bleed, width, height - edge + 1f)) }
    return Path().apply { op(flat, waves, PathOperation.Union) }
}

/**
 * Two layered waves in slow, continuous motion, like liquid: the translucent back layer sways
 * and bobs, the gradient front layer drifts gently against it. Both layers are drawn wider
 * than the screen on each side, so swaying never reveals an edge. A single phase value drives
 * everything and is read only while drawing: no recomposition, no layout, one path per layer.
 * With animations disabled in system settings the waves simply stay still.
 */
@Composable
fun WaveBackground(modifier: Modifier = Modifier) {
    val colors = CryptoManiaTheme.colors
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 9_000, easing = LinearEasing)),
        label = "wavePhase",
    )
    Box(
        modifier.drawWithCache {
            val overscan = 48.dp.toPx()
            val bleed = 8.dp.toPx()
            val edge = WaveEdgeHeight.toPx()
            val width = size.width + overscan * 2
            val front = wavePath(width, size.height, edge, bleed, mirrored = false)
            val back = wavePath(width, size.height + edge * 0.25f, edge * 1.25f, bleed, mirrored = true)
            val gradient = Brush.linearGradient(listOf(colors.waveStart, colors.waveEnd), end = Offset(width, size.height))
            val backColor = colors.waveEnd.copy(alpha = 0.35f)
            val bob = 3.dp.toPx()
            onDrawBehind {
                val p = phase
                translate(left = -overscan + sin(p) * overscan * 0.75f, top = bob * sin(p + 1.3f)) {
                    drawPath(back, backColor)
                }
                translate(left = -overscan + sin(p + PI.toFloat()) * overscan * 0.3f, top = bob * 0.6f * cos(p)) {
                    drawPath(front, gradient)
                }
            }
        },
    )
}

/** Wave that extends behind the status bar; content sits below the status bar, above the edge. */
@Composable
fun WaveSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(modifier.fillMaxWidth()) {
        WaveBackground(Modifier.matchParentSize())
        CompositionLocalProvider(LocalContentColor provides CryptoManiaTheme.colors.onWave) {
            Box(
                Modifier
                    // Content always ends above the waves, on the flat part of the header.
                    .padding(top = statusBar, bottom = WaveEdgeHeight + 4.dp)
                    .fillMaxWidth(),
                content = content,
            )
        }
    }
}

/**
 * Brand top bar drawn on the wave, meant for a Scaffold `topBar` slot: the wave is drawn over
 * the content, so lists scroll *under* it and show through the curve, with no hard edge.
 *
 * With a [scrollBehavior] it starts expanded (large title) and collapses into a compact
 * bar as content scrolls. Data freshness problems appear as a pill on the wave ([status]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoManiaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    status: SyncStatus = SyncStatus.UpToDate,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    expandedExtra: Dp = if (scrollBehavior != null) 32.dp else 0.dp,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val extraPx = with(LocalDensity.current) { expandedExtra.toPx() }
    if (scrollBehavior != null) SideEffect { scrollBehavior.state.heightOffsetLimit = -extraPx }
    // Without a scroll behavior the bar is simply compact.
    val collapsed = scrollBehavior?.state?.collapsedFraction ?: 1f
    val titleStyle = lerp(MaterialTheme.typography.headlineLarge, MaterialTheme.typography.titleLarge, collapsed)

    WaveSurface(modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp + lerp(expandedExtra, 0.dp, collapsed))
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
                        Spacer(Modifier.height(2.dp))
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onWave.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, content = actions)
            }
            AnimatedVisibility(
                visible = status !is SyncStatus.UpToDate,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    // Shrinks together with the title as the bar collapses.
                    NetworkStatusIndicator(status, Modifier.padding(horizontal = spacing.lg), compact = collapsed)
                    Spacer(Modifier.height(spacing.xs))
                }
            }
        }
    }
}
