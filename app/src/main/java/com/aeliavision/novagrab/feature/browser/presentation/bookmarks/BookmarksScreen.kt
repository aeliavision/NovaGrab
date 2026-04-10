package com.aeliavision.novagrab.feature.browser.presentation.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BookmarksScreen(
    onNavigateBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    viewModel: BrowserBookmarksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                BrowserBookmarksEffect.NavigateBack -> onNavigateBack()
                is BrowserBookmarksEffect.OpenUrl -> onOpenUrl(effect.url)
            }
        }
    }

    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
        ObsidianTopBar(
            title = "Bookmarks",
            navigationIcon = {
                ObsidianIconButton(onClick = { viewModel.handleIntent(BrowserBookmarksIntent.Back) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                    onClick = { viewModel.handleIntent(BrowserBookmarksIntent.Open(item.url)) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title ?: item.url,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        ObsidianIconButton(onClick = { viewModel.handleIntent(BrowserBookmarksIntent.Remove(item.id)) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Remove bookmark",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
