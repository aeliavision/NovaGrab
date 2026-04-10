package com.aeliavision.novagrab.feature.browser.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.ui.ObsidianSearchBar

private data class TrendingItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun BrowserHomePage(
    urlInput: String,
    onUrlInputChange: (String) -> Unit,
    onGo: () -> Unit,
    onNavigateDownloads: () -> Unit,
    onNavigateHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trending = listOf(
        TrendingItem("Trending", "Popular videos right now", Icons.Outlined.TrendingUp) {
            onUrlInputChange("trending videos")
            onGo()
        },
        TrendingItem("Downloads", "Check your queue", Icons.Outlined.Download, onNavigateDownloads),
        TrendingItem("History", "Continue where you left off", Icons.Outlined.History, onNavigateHistory),
        TrendingItem("Explore", "Discover new sites", Icons.Outlined.Public) {
            onUrlInputChange("popular video sites")
            onGo()
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "NovaGrab",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "Digital Obsidian Browser",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ObsidianSearchBar(
            value = urlInput,
            onValueChange = onUrlInputChange,
            placeholder = "Search or enter URL",
            leadingIcon = Icons.Outlined.TrendingUp,
            trailingIcon = Icons.Outlined.Public,
            onTrailingClick = onGo,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "TRENDING NOW",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = trending,
                key = { it.title },
            ) { item ->
                TrendingCard(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    onClick = item.onClick,
                )
            }
        }
    }
}
