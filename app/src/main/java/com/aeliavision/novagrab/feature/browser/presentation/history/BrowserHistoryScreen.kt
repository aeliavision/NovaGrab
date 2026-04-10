package com.aeliavision.novagrab.feature.browser.presentation.history


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import com.aeliavision.novagrab.ui.ObsidianCard
import com.aeliavision.novagrab.ui.ObsidianIconButton
import com.aeliavision.novagrab.ui.ObsidianTopBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BrowserHistoryScreen(
    onNavigateBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    viewModel: BrowserHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                BrowserHistoryEffect.NavigateBack -> onNavigateBack()
                is BrowserHistoryEffect.OpenUrl -> onOpenUrl(effect.url)
            }
        }
    }

    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
        ObsidianTopBar(
            title = "History",
            navigationIcon = {
                ObsidianIconButton(onClick = { viewModel.handleIntent(BrowserHistoryIntent.Back) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            actions = {
                if (state.items.isNotEmpty()) {
                    ObsidianIconButton(onClick = { viewModel.handleIntent(BrowserHistoryIntent.ClearAll) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Clear all", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.items, key = { it.id }) { item ->
                ObsidianCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    ghostBorder = true,
                    onClick = { viewModel.handleIntent(BrowserHistoryIntent.Open(item.url)) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = item.title ?: item.url,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
