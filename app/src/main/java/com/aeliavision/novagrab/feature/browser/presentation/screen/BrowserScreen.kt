package com.aeliavision.novagrab.feature.browser.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import com.aeliavision.novagrab.feature.browser.presentation.BrowserEffect
import com.aeliavision.novagrab.feature.browser.presentation.BrowserIntent
import com.aeliavision.novagrab.feature.browser.presentation.BrowserViewModel
import com.aeliavision.novagrab.feature.browser.presentation.components.BrowserWebView
import com.aeliavision.novagrab.feature.detection.presentation.DetectionViewModel
import com.aeliavision.novagrab.feature.detection.presentation.components.DetectedVideoSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onOpenSettings: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    openUrlFromNav: String? = null,
    viewModel: BrowserViewModel = hiltViewModel(),
    detectionViewModel: DetectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val detectionState by detectionViewModel.state.collectAsState()

    var goBackSignal by remember { mutableIntStateOf(0) }
    var goForwardSignal by remember { mutableIntStateOf(0) }

    LaunchedEffect(openUrlFromNav) {
        if (!openUrlFromNav.isNullOrBlank()) {
            viewModel.handleIntent(BrowserIntent.LoadUrl(openUrlFromNav))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BrowserEffect.NavigateToUrl -> {
                    // WebView reacts via urlToLoad
                }
                BrowserEffect.GoBack -> goBackSignal++
                BrowserEffect.GoForward -> goForwardSignal++
                BrowserEffect.NavigateToTabs -> onOpenTabs()
                BrowserEffect.NavigateToBookmarks -> onOpenBookmarks()
                BrowserEffect.NavigateToHistory -> onOpenHistory()
                BrowserEffect.NavigateToDownloads -> onOpenDownloads()
                BrowserEffect.NavigateToSettings -> onOpenSettings()
            }
        }
    }

    val isHomePage = state.currentUrl.isBlank() || state.currentUrl == "about:blank"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        com.aeliavision.novagrab.ui.ObsidianTopBar(
            title = if (isHomePage) "NovaGrab" else (state.pageTitle ?: "NovaGrab"),
            navigationIcon = {
                Row {
                    IconButton(onClick = { viewModel.handleIntent(BrowserIntent.GoBack) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    IconButton(onClick = { viewModel.handleIntent(BrowserIntent.GoForward) }) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Forward")
                    }
                }
            },
            actions = {
                com.aeliavision.novagrab.ui.ObsidianChip(
                    text = "Detected ${detectionState.videos.size}",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            detectionViewModel.handleIntent(
                                com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.ToggleSheet
                            )
                        },
                )

                IconButton(onClick = { viewModel.handleIntent(BrowserIntent.AddBookmark) }) {
                    Icon(imageVector = Icons.Default.Bookmarks, contentDescription = "Bookmark")
                }
            },
        )

        if (!isHomePage && state.pageProgress in 0..99) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { state.pageProgress / 100f },
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding(),
        ) {
            if (isHomePage) {
                com.aeliavision.novagrab.feature.browser.presentation.components.BrowserHomePage(
                    urlInput = state.urlInput,
                    onUrlInputChange = { viewModel.handleIntent(BrowserIntent.UrlInputChanged(it)) },
                    onGo = { viewModel.handleIntent(BrowserIntent.LoadFromInput) },
                    onNavigateDownloads = onOpenDownloads,
                    onNavigateHistory = onOpenHistory,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                BrowserWebView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    tabId = state.tabId,
                    webViewPool = viewModel.webViewPool,
                    urlToLoad = state.currentUrl,
                    goBackSignal = goBackSignal,
                    goForwardSignal = goForwardSignal,
                )
            }
        }

        com.aeliavision.novagrab.ui.ObsidianBottomNav(
            items = listOf(
                com.aeliavision.novagrab.ui.ObsidianNavItem("tabs", Icons.Default.Tab, "Tabs"),
                com.aeliavision.novagrab.ui.ObsidianNavItem("bookmarks", Icons.Default.Bookmarks, "Bookmarks"),
                com.aeliavision.novagrab.ui.ObsidianNavItem("history", Icons.Default.History, "History"),
                com.aeliavision.novagrab.ui.ObsidianNavItem("downloads", Icons.Default.Download, "Downloads"),
                com.aeliavision.novagrab.ui.ObsidianNavItem("settings", Icons.Default.Settings, "Settings"),
            ),
            selectedId = "",
            onSelect = { id ->
                when (id) {
                    "tabs" -> viewModel.handleIntent(BrowserIntent.OpenTabs)
                    "bookmarks" -> viewModel.handleIntent(BrowserIntent.OpenBookmarks)
                    "history" -> viewModel.handleIntent(BrowserIntent.OpenHistory)
                    "downloads" -> viewModel.handleIntent(BrowserIntent.OpenDownloads)
                    "settings" -> viewModel.handleIntent(BrowserIntent.OpenSettings)
                }
            },
        )
    }

    if (detectionState.showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                detectionViewModel.handleIntent(
                    com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.ToggleSheet
                )
            },
        ) {
            DetectedVideoSheet(
                videos = detectionState.videos,
                selectedIds = detectionState.selectedIds,
                isMultiSelectMode = detectionState.isMultiSelectMode,
                onDownload = { video ->
                    detectionViewModel.handleIntent(
                        com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.DownloadVideo(video.videoUrl)
                    )
                },
                onToggleSelection = { id ->
                    detectionViewModel.handleIntent(
                        com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.ToggleSelection(id)
                    )
                },
                onDownloadSelected = {
                    detectionViewModel.handleIntent(
                        com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.DownloadSelected
                    )
                },
                modifier = Modifier.imePadding(),
            )
        }
    }

    val qualityMasterUrl = detectionState.qualityMasterUrl
    if (detectionState.showQualityPicker && qualityMasterUrl != null) {
        AlertDialog(
            onDismissRequest = {
                detectionViewModel.handleIntent(
                    com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.DismissQualityPicker
                )
            },
            title = { Text(text = "Select Quality") },
            text = {
                Column {
                    detectionState.qualityVariants.forEach { variant ->
                        TextButton(
                            onClick = {
                                detectionViewModel.handleIntent(
                                    com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.SelectHlsVariant(
                                        masterUrl = qualityMasterUrl,
                                        variantUrl = variant.url,
                                    )
                                )
                            }
                        ) {
                            Text(text = variant.label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        detectionViewModel.handleIntent(
                            com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.DismissQualityPicker
                        )
                    }
                ) {
                    Text(text = "Close")
                }
            },
        )
    }

    if (detectionState.showRenameDialog && detectionState.renameVideoUrl != null) {
        com.aeliavision.novagrab.feature.detection.presentation.components.RenameDownloadDialog(
            initialName = detectionState.renameInitialName,
            onConfirm = { finalName ->
                detectionViewModel.handleIntent(
                    com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.DownloadVideoWithName(
                        videoUrl = detectionState.renameVideoUrl!!,
                        fileName = finalName,
                    )
                )
            },
            onDismiss = {
                detectionViewModel.handleIntent(com.aeliavision.novagrab.feature.detection.presentation.DetectionIntent.DismissRenameDialog)
            },
        )
    }
}
