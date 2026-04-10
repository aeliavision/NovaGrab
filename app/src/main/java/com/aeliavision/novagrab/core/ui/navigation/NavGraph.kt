package com.aeliavision.novagrab.core.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aeliavision.novagrab.feature.browser.presentation.screen.BrowserScreen
import com.aeliavision.novagrab.feature.browser.presentation.bookmarks.BookmarksScreen
import com.aeliavision.novagrab.feature.browser.presentation.history.BrowserHistoryScreen
import com.aeliavision.novagrab.feature.browser.presentation.tabs.TabsOverviewScreen
import com.aeliavision.novagrab.feature.downloader.presentation.screen.DownloadHistoryScreen
import com.aeliavision.novagrab.feature.downloader.presentation.screen.DownloadQueueScreen
import com.aeliavision.novagrab.feature.player.presentation.screen.PlayerScreen
import com.aeliavision.novagrab.feature.settings.presentation.screen.SettingsScreen

fun NavGraphBuilder.appNavGraph(
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToDownloadHistory: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToBrowserTabs: () -> Unit,
    onNavigateToBrowserBookmarks: () -> Unit,
    onNavigateToBrowserHistory: () -> Unit,
    onOpenBrowserUrl: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(NavRoutes.Browser.route) { backStackEntry ->
        val openUrl = backStackEntry.savedStateHandle.get<String>("open_url")
        if (openUrl != null) {
            backStackEntry.savedStateHandle.remove<String>("open_url")
        }
        BrowserScreen(
            onOpenSettings = onNavigateToSettings,
            onOpenDownloads = onNavigateToDownloads,
            onOpenTabs = onNavigateToBrowserTabs,
            onOpenBookmarks = onNavigateToBrowserBookmarks,
            onOpenHistory = onNavigateToBrowserHistory,
            openUrlFromNav = openUrl,
        )
    }

    composable(NavRoutes.BrowserTabs.route) {
        TabsOverviewScreen(onNavigateBack = onNavigateBack)
    }

    composable(NavRoutes.BrowserBookmarks.route) {
        BookmarksScreen(
            onNavigateBack = onNavigateBack,
            onOpenUrl = onOpenBrowserUrl,
        )
    }

    composable(NavRoutes.BrowserHistory.route) {
        BrowserHistoryScreen(
            onNavigateBack = onNavigateBack,
            onOpenUrl = onOpenBrowserUrl,
        )
    }

    composable(NavRoutes.Downloads.route) {
        DownloadQueueScreen(
            onNavigateBack = onNavigateBack,
            onOpenHistory = onNavigateToDownloadHistory,
        )
    }

    composable(NavRoutes.DownloadHistory.route) {
        DownloadHistoryScreen(
            onNavigateBack = onNavigateBack,
            onOpenPlayer = onNavigateToPlayer,
        )
    }

    composable(
        route = NavRoutes.Player.route,
        arguments = listOf(
            navArgument("uri") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { entry ->
        val uri = entry.arguments?.getString("uri")
        PlayerScreen(
            onNavigateBack = onNavigateBack,
            uriFromNav = uri,
        )
    }

    composable(NavRoutes.Settings.route) {
        SettingsScreen(onNavigateBack = onNavigateBack)
    }
}
