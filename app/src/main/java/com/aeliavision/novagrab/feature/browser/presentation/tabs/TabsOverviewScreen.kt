package com.aeliavision.novagrab.feature.browser.presentation.tabs


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import com.aeliavision.novagrab.ui.ObsidianCard
import com.aeliavision.novagrab.ui.ObsidianIconButton
import com.aeliavision.novagrab.ui.ObsidianTopBar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close

@Composable
fun TabsOverviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: BrowserTabsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                BrowserTabsEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
        ObsidianTopBar(
            title = "Tabs",
            navigationIcon = {
                ObsidianIconButton(onClick = { viewModel.handleIntent(BrowserTabsIntent.Back) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            actions = {
                ObsidianIconButton(onClick = { viewModel.handleIntent(BrowserTabsIntent.AddTab) }) {
                    Icon(Icons.Default.Add, contentDescription = "New Tab", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        )

        if (state.tabs.isEmpty()) {
            Text(
                modifier = Modifier.padding(24.dp),
                text = "No tabs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.tabs, key = { it.id }) { tab ->
                ObsidianCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    ghostBorder = true,
                    onClick = { viewModel.handleIntent(BrowserTabsIntent.SelectTab(tab.id)) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tab.title ?: "New tab",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                )
                                if (!tab.url.isNullOrBlank()) {
                                    Text(
                                        text = tab.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.handleIntent(BrowserTabsIntent.CloseTab(tab.id)) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close tab", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
