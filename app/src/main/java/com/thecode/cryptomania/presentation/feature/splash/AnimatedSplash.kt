package com.thecode.cryptomania.presentation.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.Ubuntu
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// Timeline, in milliseconds.
private const val FALL = 700f
private const val GATHER_START = 1_150f
private const val GATHER = 520f
private const val LOGO_START = 1_620f
private const val REVEAL_START = 2_550f
private const val REVEAL = 600f
private const val TOTAL = REVEAL_START + REVEAL

/** Same blue as the system splash window, so the hand-off is invisible. */
private val SplashBlue = Color(0xFF2A5FE0)
private val Gold = Color(0xFFFFC933)

/** Logo geometry, in the 108-unit launcher icon viewport. */
private const val VIEWPORT = 108f
private val BackCoin = Offset(62f, 46f)
private const val BACK_RADIUS = 18f
private val FrontCoin = Offset(47f, 60f)
private const val FRONT_RADIUS = 20f

private class FallingCoin(
    val glyph: String,
    val color: Color,
    val x: Float,
    val restY: Float,
    val diameter: Float,
    val delay: Float,
    val spin: Float,
    val front: Boolean,
)

private val FallingCoins: List<FallingCoin> = run {
    val brands = listOf(
        "₿" to 0xFFF7931A, "Ξ" to 0xFF627EEA, "◎" to 0xFF9945FF, "Ð" to 0xFFC2A633,
        "Ł" to 0xFF345D9D, "₮" to 0xFF26A17B, "₳" to 0xFF0033AD,
    )
    val count = brands.size * 2
    // Fixed seed: the same shower on every launch, tuned once.
    val random = Random(7)
    val slots = (0 until count).shuffled(random)
    List(count) { i ->
        val (glyph, color) = brands[i % brands.size]
        FallingCoin(
            glyph = glyph,
            color = Color(color),
            x = (slots[i] + 0.5f) / count,
            restY = 0.26f + random.nextFloat() * 0.46f,
            diameter = 36f + random.nextInt(20),
            delay = random.nextFloat() * 420f,
            spin = random.nextFloat() * 720f - 360f,
            front = i % 2 == 0,
        )
    }
}

/**
 * Launch animation, drawn over the app while it loads: crypto coins rain down and bounce, get
 * pulled into a vortex, turn gold and merge into the two coins of the CryptoMania mark, which
 * pops with a shockwave; the splash then opens as an expanding circle onto the app.
 *
 * One clock drives everything and is only read while drawing (no recomposition per frame).
 * A tap skips to the reveal; with animations disabled in system settings it finishes at once.
 * The show starts once [ready] (the system splash window is gone), or after a short timeout.
 */
@Composable
fun AnimatedSplash(onFinished: () -> Unit, modifier: Modifier = Modifier, ready: () -> Boolean = { true }) {
    val time = remember { Animatable(0f) }
    val finished by rememberUpdatedState(onFinished)
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        withTimeoutOrNull(1_000) { snapshotFlow(ready).first { it } }
        time.animateTo(TOTAL, tween(TOTAL.toInt(), easing = LinearEasing))
        finished()
    }
    val logo = painterResource(R.drawable.ic_launcher_foreground)
    val measurer = rememberTextMeasurer()
    val appName = stringResource(R.string.app_name)

    Spacer(
        modifier
            .fillMaxSize()
            .semantics { contentDescription = appName }
            .pointerInput(Unit) {
                detectTapGestures {
                    if (time.value < REVEAL_START) {
                        scope.launch {
                            time.snapTo(REVEAL_START)
                            time.animateTo(TOTAL, tween(REVEAL.toInt(), easing = LinearEasing))
                            finished()
                        }
                    }
                }
            }
            // Offscreen, so the reveal can punch a transparent hole through the splash.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val logoSize = min(min(size.width, size.height) * 0.72f, 320.dp.toPx())
                val unit = logoSize / VIEWPORT
                val center = Offset(size.width / 2, size.height / 2 - 24.dp.toPx())
                val logoOrigin = center - Offset(logoSize / 2, logoSize / 2)
                val glyphs = FallingCoins.map {
                    measurer.measure(
                        it.glyph,
                        TextStyle(color = Color.White, fontSize = (it.diameter * 0.5f).dp.toSp(), fontWeight = FontWeight.Bold),
                    )
                }
                val wordmark = measurer.measure(
                    appName,
                    TextStyle(color = Color.White, fontSize = 34.sp, fontFamily = Ubuntu, fontWeight = FontWeight.Bold),
                )
                val glow = Brush.radialGradient(
                    0f to Color.White.copy(alpha = 0.2f),
                    0.45f to Color.White.copy(alpha = 0.07f),
                    1f to Color.Transparent,
                    center = center,
                    radius = logoSize,
                )
                val sparkle = sparklePath()
                val sparkles = listOf(Offset(0.42f, -0.34f), Offset(-0.38f, -0.24f), Offset(0.36f, 0.3f), Offset(-0.3f, 0.36f))
                val maxRadius = hypot(size.width, size.height)

                onDrawBehind {
                    val t = time.value
                    val reveal = EaseInOutCubic.transform(progress(t, REVEAL_START, REVEAL))
                    val logoIn = progress(t, LOGO_START, 480f)
                    // The mark zooms toward the viewer and fades just before the circle opens.
                    val exit = EaseInOutCubic.transform(progress(t, REVEAL_START - 200f, 350f))
                    val brandAlpha = 1f - exit
                    val brandScale = 1f + exit * 0.3f

                    drawRect(SplashBlue)
                    drawCircle(glow, radius = logoSize, center = center, alpha = progress(t, GATHER_START, GATHER + 300f) * brandAlpha)

                    // 1-2. Rain, then vortex into the two coins of the mark.
                    val coinsAlpha = 1f - progress(t, LOGO_START, 120f)
                    if (coinsAlpha > 0f) {
                        FallingCoins.forEachIndexed { i, coin ->
                            val fall = progress(t, coin.delay, FALL)
                            if (fall == 0f) return@forEachIndexed
                            var radius = coin.diameter.dp.toPx() / 2
                            val rest = Offset(coin.x * size.width, coin.restY * size.height)
                            var position = lerp(Offset(rest.x, -radius * 2), rest, EaseOutBounce.transform(fall))
                            var rotation = coin.spin * (1f - EaseOutCubic.transform(fall))
                            var color = coin.color
                            var glyphAlpha = 1f
                            val gather = EaseInOutCubic.transform(progress(t, GATHER_START + i * 18f, GATHER))
                            if (gather > 0f) {
                                val target = logoOrigin + (if (coin.front) FrontCoin else BackCoin) * unit
                                val targetRadius = (if (coin.front) FRONT_RADIUS else BACK_RADIUS) * unit * 0.9f
                                position = target + (rest - target).rotated(gather * 2.4f) * (1f - gather)
                                radius += (targetRadius - radius) * gather
                                rotation += gather * 360f
                                color = lerp(coin.color, Gold, gather)
                                glyphAlpha = 1f - gather
                            }
                            drawCoin(position, radius, color, rotation, coinsAlpha) {
                                val layout = glyphs[i]
                                drawText(
                                    layout,
                                    topLeft = position - Offset(layout.size.width / 2f, layout.size.height / 2f),
                                    alpha = glyphAlpha * coinsAlpha,
                                )
                            }
                        }
                    }

                    // 3. The mark pops out of the merged coins, with a shockwave and sparkles.
                    if (logoIn > 0f) {
                        val wave = progress(t, LOGO_START, 500f)
                        if (wave < 1f) {
                            drawCircle(
                                Color.White.copy(alpha = 0.45f * (1f - wave)),
                                radius = logoSize * (0.3f + 0.6f * EaseOutCubic.transform(wave)),
                                center = center,
                                style = Stroke(width = 6.dp.toPx() * (1f - wave) + 1f),
                            )
                        }
                        val scale = (0.8f + 0.2f * EaseOutBack.transform(logoIn)) * brandScale
                        withTransform({
                            translate(logoOrigin.x, logoOrigin.y)
                            scale(scale, scale, pivot = Offset(logoSize / 2, logoSize / 2))
                        }) {
                            with(logo) { draw(Size(logoSize, logoSize), alpha = progress(t, LOGO_START, 120f) * brandAlpha) }
                        }
                        sparkles.forEachIndexed { k, at ->
                            val twinkle = progress(t, LOGO_START + 150f + k * 110f, 600f)
                            if (twinkle in 0.001f..0.999f) {
                                val s = sin(twinkle * PI.toFloat()) * 9.dp.toPx()
                                withTransform({
                                    translate(center.x + at.x * logoSize * brandScale, center.y + at.y * logoSize * brandScale)
                                    scale(s, s, pivot = Offset.Zero)
                                }) { drawPath(sparkle, Color.White, alpha = brandAlpha) }
                            }
                        }
                        val word = EaseOutCubic.transform(progress(t, LOGO_START + 120f, 400f))
                        translate(
                            left = center.x - wordmark.size.width / 2f,
                            top = center.y + logoSize * 0.36f * brandScale + (1f - word) * 16.dp.toPx(),
                        ) { drawText(wordmark, alpha = word * brandAlpha) }
                    }

                    // 4. Open onto the app.
                    if (reveal > 0f) drawCircle(Color.Black, radius = reveal * maxRadius, center = center, blendMode = BlendMode.Clear)
                }
            },
    )
}

private fun progress(t: Float, start: Float, duration: Float) = ((t - start) / duration).coerceIn(0f, 1f)

private fun Offset.rotated(radians: Float): Offset {
    val c = cos(radians)
    val s = sin(radians)
    return Offset(x * c - y * s, x * s + y * c)
}

/** A minted coin: darker rim below for thickness, face, inner ring, then its symbol. */
private inline fun DrawScope.drawCoin(
    center: Offset,
    radius: Float,
    color: Color,
    rotation: Float,
    alpha: Float,
    symbol: DrawScope.() -> Unit,
) {
    drawCircle(lerp(color, Color.Black, 0.3f), radius, center + Offset(0f, radius * 0.14f), alpha = alpha)
    drawCircle(color, radius, center, alpha = alpha)
    drawCircle(Color.White.copy(alpha = 0.3f), radius * 0.78f, center, alpha = alpha, style = Stroke(radius * 0.07f))
    rotate(rotation, pivot = center) { symbol() }
}

/** Four-point star of unit radius, like the sparkles of the launcher icon. */
private fun sparklePath() = Path().apply {
    moveTo(0f, -1f)
    quadraticTo(0.15f, -0.15f, 1f, 0f)
    quadraticTo(0.15f, 0.15f, 0f, 1f)
    quadraticTo(-0.15f, 0.15f, -1f, 0f)
    quadraticTo(-0.15f, -0.15f, 0f, -1f)
    close()
}
