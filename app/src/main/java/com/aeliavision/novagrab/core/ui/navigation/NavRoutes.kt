package com.aeliavision.novagrab.core.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Browser : NavRoutes("browser")
    data object BrowserTabs : NavRoutes("browser_tabs")
    data object BrowserBookmarks : NavRoutes("browser_bookmarks")
    data object BrowserHistory : NavRoutes("browser_history")
    data object Downloads : NavRoutes("downloads")
    data object DownloadHistory : NavRoutes("download_history")
    data object Player : NavRoutes("player?uri={uri}") {
        fun createRoute(uri: String) = "player?uri=${android.net.Uri.encode(uri)}"
    }
    data object Settings : NavRoutes("settings")
}
