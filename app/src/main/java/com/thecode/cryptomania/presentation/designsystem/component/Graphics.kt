package com.thecode.cryptomania.presentation.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme

/**
 * Fixed-size, circular logo. The placeholder occupies the final bounds, so lists never jump
 * when images arrive; the initial letter is shown when there is no logo at all.
 */
@Composable
fun CoinLogo(
    url: String?,
    fallbackLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = CryptoManiaTheme.sizes.logoMedium,
) {
    val colors = CryptoManiaTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.surfaceHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNullOrBlank()) {
            Text(
                text = fallbackLabel.take(1).uppercase(),
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = with(LocalDensity.current) { (size * 0.42f).toSp() },
            )
        } else {
            val placeholder = ColorPainter(colors.surfaceHigh)
            AsyncImage(
                model = url,
                contentDescription = null,
                placeholder = placeholder,
                error = placeholder,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        }
    }
}

/**
 * Minimal trend line for list rows. Geometry is computed once per size/data in
 * [drawWithCache], so scrolling does not rebuild paths.
 */
@Composable
fun Sparkline(
    values: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 1.5.dp,
    fill: Boolean = true,
) {
    if (values.size < 2) {
        Box(modifier)
        return
    }
    Box(
        modifier.drawWithCache {
            val min = values.min()
            val max = values.max()
            val span = (max - min).takeIf { it > 0 } ?: 1.0
            val stroke = strokeWidth.toPx()
            val usableHeight = size.height - stroke
            val stepX = size.width / (values.size - 1)
            val line = Path()
            values.forEachIndexed { index, value ->
                val x = index * stepX
                val y = stroke / 2 + usableHeight * (1f - ((value - min) / span).toFloat())
                if (index == 0) line.moveTo(x, y) else line.lineTo(x, y)
            }
            val area = Path().apply {
                addPath(line)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            val gradient = Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0f)))
            onDrawBehind {
                if (fill) drawPath(area, gradient)
                drawPath(line, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        },
    )
}

/** Horizontal proportion bar, e.g. market dominance or supply progress. */
@Composable
fun SegmentedBar(
    segments: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    trackColor: Color = CryptoManiaTheme.colors.surfaceHigh,
    height: Dp = 6.dp,
) {
    Canvas(modifier.height(height).clip(CircleShape)) {
        drawRect(trackColor)
        var start = 0f
        segments.forEach { (fraction, color) ->
            val width = size.width * fraction.coerceIn(0f, 1f - start)
            drawRect(color, topLeft = Offset(size.width * start, 0f), size = size.copy(width = width))
            start += fraction
        }
    }
}
