package com.thecode.cryptomania.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
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
fun CryptoManiaNavigation(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val current = TopLevel.entries.firstOrNull { destination?.hasRoute(it.routeClass) == true }
    val colors = CryptoManiaTheme.colors
    val adaptiveType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())

    NavigationSuiteScaffold(
        // Detail and search screens are full-screen; the bar/rail only frames top-level tabs.
        layoutType = if (current != null || destination == null) adaptiveType else NavigationSuiteType.None,
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
            startDestination = MarketDestination,
            enterTransition = { fadeIn(tween(Motion.MEDIUM)) },
            exitTransition = { fadeOut(tween(Motion.SHORT)) },
        ) {
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

private fun AnimatedContentTransitionScope<NavBackStackEntry>.pushEnter(): EnterTransition =
    slideInHorizontally(tween(Motion.MEDIUM, easing = Motion.Emphasized)) { it / 6 } + fadeIn(tween(Motion.MEDIUM))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    slideOutHorizontally(tween(Motion.MEDIUM, easing = Motion.Emphasized)) { it / 6 } + fadeOut(tween(Motion.SHORT))
