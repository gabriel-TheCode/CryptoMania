package com.thecode.cryptomania.presentation.designsystem.component.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.designsystem.theme.Motion

/**
 * Lightweight Compose-native price chart.
 *
 * - Geometry is built once per data/size in [drawWithCache]; scrubbing and the reveal
 *   animation only invalidate the draw phase, never recomposition or path building.
 * - Scrubbing starts on a horizontal drag or a long press, so vertical scrolling through
 *   the screen is never hijacked. The selected point is reported through [onScrub] and the
 *   caller shows it in the header, as modern trading apps do.
 * - Points are plotted by timestamp, not by index, so gaps in sparse history stay visible
 *   instead of being silently stretched.
 */
@Composable
fun PriceChart(
    points: List<PricePoint>,
    lineColor: Color,
    highLabel: String,
    lowLabel: String,
    accessibilityDescription: String,
    onScrub: (PricePoint?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CryptoManiaTheme.colors
    val haptics = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
    val currentOnScrub by rememberUpdatedState(onScrub)

    var scrubIndex by remember(points) { mutableStateOf<Int?>(null) }
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(points) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(Motion.LONG, easing = Motion.Emphasized))
    }

    Spacer(
        modifier
            .semantics { contentDescription = accessibilityDescription }
            .pointerInput(points) {
                if (points.size < 2) return@pointerInput
                fun indexAt(x: Float): Int {
                    val first = points.first().epochMillis
                    val span = (points.last().epochMillis - first).coerceAtLeast(1)
                    val target = first + (x / size.width).coerceIn(0f, 1f) * span
                    val insertion = points.binarySearchBy(target.toLong()) { it.epochMillis }
                    val candidate = if (insertion >= 0) insertion else (-insertion - 1)
                    return listOf(candidate - 1, candidate)
                        .filter { it in points.indices }
                        .minBy { kotlin.math.abs(points[it].epochMillis - target) }
                }

                fun select(x: Float) {
                    val index = indexAt(x)
                    if (index != scrubIndex) {
                        scrubIndex = index
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                        currentOnScrub(points[index])
                    }
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var cancelled = false
                    val slop = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                            .also { if (it == null) cancelled = true }
                    }
                    if (cancelled) return@awaitEachGesture
                    // Either a horizontal drag passed the slop, or the finger was held long enough.
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    select((slop ?: down).position.x)
                    drag(slop?.id ?: down.id) { change ->
                        select(change.position.x)
                        change.consume()
                    }
                    scrubIndex = null
                    currentOnScrub(null)
                }
            }
            .drawWithCache {
                val topPadding = 22.dp.toPx()
                val bottomPadding = 22.dp.toPx()
                val stroke = 2.dp.toPx()
                if (points.size < 2) return@drawWithCache onDrawBehind { }

                val min = points.minOf { it.price }
                val max = points.maxOf { it.price }
                val priceSpan = (max - min).takeIf { it > 0 } ?: (max.takeIf { it != 0.0 } ?: 1.0) * 0.01
                val firstTime = points.first().epochMillis
                val timeSpan = (points.last().epochMillis - firstTime).coerceAtLeast(1).toFloat()
                val plotHeight = size.height - topPadding - bottomPadding
                val xs = FloatArray(points.size) { (points[it].epochMillis - firstTime) / timeSpan * size.width }
                val ys = FloatArray(points.size) {
                    topPadding + plotHeight * (1f - ((points[it].price - min) / priceSpan).toFloat())
                }

                val line = Path().apply {
                    moveTo(xs[0], ys[0])
                    for (i in 1 until points.size) lineTo(xs[i], ys[i])
                }
                val area = Path().apply {
                    addPath(line)
                    lineTo(xs.last(), size.height)
                    lineTo(xs.first(), size.height)
                    close()
                }
                val fill = Brush.verticalGradient(
                    listOf(lineColor.copy(alpha = 0.24f), lineColor.copy(alpha = 0f)),
                    startY = topPadding,
                    endY = size.height,
                )
                val baselineY = ys.first()
                val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))

                val maxIndex = points.indices.maxBy { points[it].price }
                val minIndex = points.indices.minBy { points[it].price }
                val high = textMeasurer.measure(highLabel, labelStyle)
                val low = textMeasurer.measure(lowLabel, labelStyle)
                val highX = (xs[maxIndex] - high.size.width / 2f).coerceIn(0f, size.width - high.size.width)
                val lowX = (xs[minIndex] - low.size.width / 2f).coerceIn(0f, size.width - low.size.width)

                onDrawBehind {
                    drawLine(
                        color = colors.textTertiary.copy(alpha = 0.5f),
                        start = Offset(0f, baselineY),
                        end = Offset(size.width, baselineY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dash,
                    )
                    clipRect(right = size.width * reveal.value) {
                        drawPath(area, fill)
                        drawPath(line, lineColor, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    drawText(high, topLeft = Offset(highX, (ys[maxIndex] - high.size.height - 4.dp.toPx()).coerceAtLeast(0f)))
                    drawText(low, topLeft = Offset(lowX, (ys[minIndex] + 4.dp.toPx()).coerceAtMost(size.height - low.size.height)))

                    val selected = scrubIndex
                    if (selected != null) {
                        val x = xs[selected]
                        val y = ys[selected]
                        drawLine(colors.textSecondary, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                        drawCircle(colors.background, radius = 7.dp.toPx(), center = Offset(x, y))
                        drawCircle(lineColor, radius = 5.dp.toPx(), center = Offset(x, y))
                    }
                }
            },
    )
}
