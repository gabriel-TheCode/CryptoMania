package com.thecode.cryptomania.presentation.feature.onboarding

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.thecode.cryptomania.R
import com.thecode.cryptomania.domain.repository.SettingsRepository
import com.thecode.cryptomania.presentation.designsystem.component.WaveSurface
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.designsystem.theme.Motion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val finished = Channel<Unit>(Channel.CONFLATED)
    val onFinished = finished.receiveAsFlow()

    /** Persists completion before navigating, so leaving the screen cannot lose the write. */
    fun finish() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
            finished.send(Unit)
        }
    }
}

private data class OnboardingPage(@param:RawRes val animation: Int?, @param:StringRes val title: Int, @param:StringRes val description: Int)

// The three messages and animations of CryptoMania 1.x, plus its welcome screen.
private val pages = listOf(
    OnboardingPage(R.raw.lottie_money, R.string.title_onboarding_1, R.string.description_onboarding_1),
    OnboardingPage(R.raw.lottie_money_circle, R.string.title_onboarding_2, R.string.description_onboarding_2),
    OnboardingPage(R.raw.lottie_market_analyst, R.string.title_onboarding_3, R.string.description_onboarding_3),
    OnboardingPage(null, R.string.title_onboarding_finish, R.string.description_onboarding_finish),
)

@Composable
fun OnboardingRoute(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(viewModel) { viewModel.onFinished.collect { finish() } }
    OnboardingScreen(onFinish = viewModel::finish)
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        WaveSurface {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = colors.onWave, modifier = Modifier.weight(1f))
                if (!isLast) {
                    TextButton(onClick = { scope.launch { pagerState.animateScrollToPage(pages.lastIndex) } }) {
                        Text(stringResource(R.string.onboarding_skip), color = colors.onWave)
                    }
                }
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            PageContent(page = pages[index], index = index, pagerState = pagerState)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl, vertical = spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PageIndicator(pagerState, Modifier.weight(1f))
            // Standard M3 filled button: one component whose width morphs (anchored to the end)
            // while the label cross-fades, instead of swapping two buttons.
            Button(
                onClick = { if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                // The old label fades out first, the button resizes around its centre, and only
                // then does the new label fade in: no frame where text is wider than the button.
                AnimatedContent(
                    targetState = isLast,
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        (fadeIn(tween(Motion.SHORT, delayMillis = Motion.MEDIUM)) togetherWith fadeOut(tween(Motion.SHORT / 2)))
                            .using(SizeTransform(clip = true) { _, _ -> tween(Motion.MEDIUM, easing = Motion.Emphasized) })
                    },
                    label = "onboardingCta",
                ) { last ->
                    Text(stringResource(if (last) R.string.onboarding_get_started else R.string.onboarding_next))
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage, index: Int, pagerState: PagerState) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val illustration: @Composable (Modifier) -> Unit = { modifier ->
        Box(
            modifier.graphicsLayer {
                // Parallax, read only while drawing: the illustration moves and fades faster than the text.
                val offset = (pagerState.currentPage - index) + pagerState.currentPageOffsetFraction
                translationX = offset * size.width * 0.35f
                alpha = 1f - offset.absoluteValue.coerceIn(0f, 1f)
                val scale = 1f - 0.15f * offset.absoluteValue.coerceIn(0f, 1f)
                scaleX = scale
                scaleY = scale
            },
            contentAlignment = Alignment.Center,
        ) {
            if (page.animation != null) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(page.animation))
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = pagerState.currentPage == index,
                )
                LottieAnimation(composition, { progress }, Modifier.fillMaxSize())
            } else {
                BrandMark()
            }
        }
    }
    val text: @Composable (Modifier, Alignment.Horizontal, TextAlign) -> Unit = { modifier, alignment, align ->
        Column(modifier, horizontalAlignment = alignment, verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Text(stringResource(page.title), style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary, textAlign = align)
            Text(stringResource(page.description), style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary, textAlign = align)
        }
    }
    val pageLabel = stringResource(R.string.cd_onboarding_page, index + 1, pages.size)
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .semantics { contentDescription = pageLabel }
            .padding(horizontal = spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        val width = maxWidth
        val height = maxHeight
        if (width > height) {
            // Landscape / large screens: illustration and copy side by side.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.xxxl)) {
                illustration(Modifier.weight(1f).heightIn(max = height))
                text(Modifier.weight(1f).widthIn(max = 480.dp), Alignment.Start, TextAlign.Start)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val size = (width * 0.8f).coerceAtMost(320.dp).coerceAtMost(height * 0.55f)
                illustration(Modifier.size(size))
                Spacer(Modifier.height(spacing.xxl))
                text(Modifier.widthIn(max = 480.dp), Alignment.CenterHorizontally, TextAlign.Center)
            }
        }
    }
}

/** The app mark with a slow "breathing" halo, closing the story on the brand. */
@Composable
private fun BrandMark() {
    val colors = CryptoManiaTheme.colors
    val halo by rememberInfiniteTransition(label = "halo").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1_600), RepeatMode.Reverse),
        label = "haloScale",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(200.dp)
                .graphicsLayer { scaleX = halo; scaleY = halo }
                .background(colors.brand.copy(alpha = 0.12f), CircleShape),
        )
        Box(
            Modifier
                .size(140.dp)
                .background(colors.brand, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(260.dp))
        }
    }
}

@Composable
private fun PageIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    val colors = CryptoManiaTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(pages.size) { index ->
            val selected = pagerState.currentPage == index
            val width by animateDpAsState(if (selected) 28.dp else 8.dp, Motion.spatial(), label = "dotWidth")
            val color by animateColorAsState(if (selected) colors.brand else colors.outline, Motion.effects(), label = "dotColor")
            Box(
                Modifier
                    .width(width)
                    .height(8.dp)
                    .background(color, CircleShape),
            )
        }
    }
}
