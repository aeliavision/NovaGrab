package com.aeliavision.novagrab.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun AppNavHost(
    openUrlEvents: SharedFlow<String>? = null,
) {
    val navController = rememberNavController()

    LaunchedEffect(openUrlEvents) {
        openUrlEvents?.collect { url ->
            navController.getBackStackEntry(NavRoutes.Browser.route)
                .savedStateHandle["open_url"] = url
            navController.navigate(NavRoutes.Browser.route) {
                launchSingleTop = true
                popUpTo(NavRoutes.Browser.route) { inclusive = false }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Browser.route
    ) {
        appNavGraph(
            onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
            onNavigateToDownloads = { navController.navigate(NavRoutes.Downloads.route) },
            onNavigateToDownloadHistory = { navController.navigate(NavRoutes.DownloadHistory.route) },
            onNavigateToPlayer = { uri ->
                navController.navigate(NavRoutes.Player.createRoute(uri))
            },
            onNavigateToBrowserTabs = { navController.navigate(NavRoutes.BrowserTabs.route) },
            onNavigateToBrowserBookmarks = { navController.navigate(NavRoutes.BrowserBookmarks.route) },
            onNavigateToBrowserHistory = { navController.navigate(NavRoutes.BrowserHistory.route) },
            onOpenBrowserUrl = { url ->
                navController.getBackStackEntry(NavRoutes.Browser.route)
                    .savedStateHandle["open_url"] = url
                navController.navigate(NavRoutes.Browser.route) {
                    launchSingleTop = true
                    popUpTo(NavRoutes.Browser.route) { inclusive = false }
                }
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
