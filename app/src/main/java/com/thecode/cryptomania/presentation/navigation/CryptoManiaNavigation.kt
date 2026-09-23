package com.thecode.cryptomania.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.designsystem.theme.Motion
import com.thecode.cryptomania.presentation.feature.coin.CoinDetailRoute
import com.thecode.cryptomania.presentation.feature.exchanges.ExchangesRoute
import com.thecode.cryptomania.presentation.feature.market.MarketRoute
import com.thecode.cryptomania.presentation.feature.onboarding.OnboardingRoute
import com.thecode.cryptomania.presentation.feature.search.SearchRoute
import com.thecode.cryptomania.presentation.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// Type-safe destinations. Screens never touch the NavController: they expose callbacks and
// this file decides what those callbacks mean.
@Serializable data object MarketDestination
@Serializable data object ExchangesDestination
@Serializable data object SettingsDestination
@Serializable data object SearchDestination
@Serializable data class CoinDestination(val coinId: String)
@Serializable data object OnboardingDestination

private enum class TopLevel(
    val route: Any,
    val routeClass: KClass<*>,
    @param:StringRes val label: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    Market(MarketDestination, MarketDestination::class, R.string.nav_market, Icons.AutoMirrored.Rounded.ShowChart, Icons.AutoMirrored.Rounded.ShowChart),
    Exchanges(ExchangesDestination, ExchangesDestination::class, R.string.nav_exchanges, Icons.Rounded.AccountBalance, Icons.Outlined.AccountBalance),
    Settings(SettingsDestination, SettingsDestination::class, R.string.nav_settings, Icons.Rounded.Settings, Icons.Outlined.Settings),
}

@Composable
fun CryptoManiaNavigation(
    showOnboarding: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val current = TopLevel.entries.firstOrNull { destination?.hasRoute(it.routeClass) == true }
    val colors = CryptoManiaTheme.colors
    val adaptiveType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())
    // Detail and search screens are full-screen; the bar/rail only frames top-level tabs and
    // slides away (rather than vanishing) while a detail screen slides in.
    val suiteState = rememberNavigationSuiteScaffoldState(
        if (showOnboarding) NavigationSuiteScaffoldValue.Hidden else NavigationSuiteScaffoldValue.Visible,
    )
    val showSuite = current != null
    LaunchedEffect(showSuite) { if (showSuite) suiteState.show() else suiteState.hide() }

    NavigationSuiteScaffold(
        layoutType = adaptiveType,
        state = suiteState,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = colors.surface,
            navigationRailContainerColor = colors.surface,
        ),
        navigationSuiteItems = {
            TopLevel.entries.forEach { item ->
                val selected = item == current
                item(
                    selected = selected,
                    onClick = { navController.navigateToTopLevel(item.route) },
                    icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null) },
                    label = { Text(stringResource(item.label)) },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = if (showOnboarding) OnboardingDestination else MarketDestination,
            // Screens are opaque, so the screen underneath always stays fully drawn while the
            // other one moves or fades over it: no blank frame between two pages.
            enterTransition = { fadeThroughEnter() },
            exitTransition = { if (targetState.isDetail()) parallaxExit() else ExitTransition.KeepUntilTransitionsFinished },
            popEnterTransition = { if (initialState.isDetail()) parallaxPopEnter() else EnterTransition.None },
            popExitTransition = { fadeThroughExit() },
        ) {
            composable<OnboardingDestination> {
                OnboardingRoute(
                    onFinished = {
                        navController.navigate(MarketDestination) { popUpTo(OnboardingDestination) { inclusive = true } }
                    },
                )
            }
            composable<MarketDestination> {
                MarketRoute(
                    onOpenCoin = { navController.navigate(CoinDestination(it)) },
                    onOpenSearch = { navController.navigate(SearchDestination) },
                )
            }
            composable<ExchangesDestination> { ExchangesRoute() }
            composable<SettingsDestination> { SettingsRoute() }
            composable<SearchDestination>(
                enterTransition = { pushEnter() },
                popExitTransition = { popExit() },
            ) {
                SearchRoute(
                    onBack = { navController.popBackStack() },
                    onOpenCoin = { navController.navigate(CoinDestination(it)) },
                )
            }
            composable<CoinDestination>(
                enterTransition = { pushEnter() },
                popExitTransition = { popExit() },
            ) {
                CoinDetailRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateToTopLevel(route: Any) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

private fun NavBackStackEntry.isDetail() =
    destination.hasRoute(CoinDestination::class) || destination.hasRoute(SearchDestination::class)

// Detail screens slide in over the current one, which drifts back a little (parallax); going
// back plays it in reverse. Predictive back scrubs these same transitions with the gesture.
private const val PUSH_DURATION = 400
private const val PARALLAX = 4
private const val FADE_DURATION = 350

private fun pushEnter(): EnterTransition =
    slideInHorizontally(tween(PUSH_DURATION, easing = Motion.Emphasized)) { it }

private fun parallaxExit(): ExitTransition =
    slideOutHorizontally(tween(PUSH_DURATION, easing = Motion.Emphasized)) { -it / PARALLAX }

private fun parallaxPopEnter(): EnterTransition =
    slideInHorizontally(tween(PUSH_DURATION, easing = Motion.Emphasized)) { -it / PARALLAX }

private fun popExit(): ExitTransition =
    slideOutHorizontally(tween(PUSH_DURATION, easing = Motion.Emphasized)) { it }

// Top-level pages fade and settle in over the previous page, which stays put underneath.
private fun fadeThroughEnter(): EnterTransition =
    fadeIn(tween(FADE_DURATION, easing = Motion.Emphasized)) +
        scaleIn(tween(FADE_DURATION, easing = Motion.Emphasized), initialScale = 0.97f)

private fun fadeThroughExit(): ExitTransition =
    fadeOut(tween(FADE_DURATION, easing = Motion.Emphasized)) +
        scaleOut(tween(FADE_DURATION, easing = Motion.Emphasized), targetScale = 0.97f)
