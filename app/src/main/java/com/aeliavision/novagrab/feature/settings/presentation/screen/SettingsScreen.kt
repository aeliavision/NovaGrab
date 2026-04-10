package com.aeliavision.novagrab.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeliavision.novagrab.feature.settings.presentation.SettingsIntent
import com.aeliavision.novagrab.feature.settings.presentation.SettingsViewModel
import com.aeliavision.novagrab.ui.ObsidianCard
import com.aeliavision.novagrab.ui.ObsidianIconButton
import com.aeliavision.novagrab.ui.ObsidianTopBar
import com.aeliavision.novagrab.ui.theme.PrimaryCyan

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ObsidianTopBar(
            title = "Settings",
            navigationIcon = {
                ObsidianIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "DOWNLOADS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ObsidianCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                ghostBorder = true,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wi‑Fi only",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Only download when connected to Wi‑Fi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Switch(
                            checked = state.wifiOnly,
                            onCheckedChange = { checked ->
                                viewModel.handleIntent(SettingsIntent.SetWifiOnly(checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryCyan,
                                checkedTrackColor = PrimaryCyan.copy(alpha = 0.35f),
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Download connect timeout",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "How long to wait while establishing a connection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ObsidianIconButton(
                                onClick = {
                                    val next = (state.downloadConnectTimeoutSeconds - 5).coerceAtLeast(5)
                                    viewModel.handleIntent(SettingsIntent.SetDownloadConnectTimeoutSeconds(next))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease connect timeout",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            Text(
                                text = "${state.downloadConnectTimeoutSeconds}s",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 10.dp),
                            )

                            ObsidianIconButton(
                                onClick = {
                                    val next = (state.downloadConnectTimeoutSeconds + 5).coerceAtMost(120)
                                    viewModel.handleIntent(SettingsIntent.SetDownloadConnectTimeoutSeconds(next))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase connect timeout",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Download read timeout",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "How long downloads can stall before failing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ObsidianIconButton(
                                onClick = {
                                    val next = (state.downloadReadTimeoutMinutes - 5).coerceAtLeast(5)
                                    viewModel.handleIntent(SettingsIntent.SetDownloadReadTimeoutMinutes(next))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease timeout",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            Text(
                                text = "${state.downloadReadTimeoutMinutes}m",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 10.dp),
                            )

                            ObsidianIconButton(
                                onClick = {
                                    val next = (state.downloadReadTimeoutMinutes + 5).coerceAtMost(180)
                                    viewModel.handleIntent(SettingsIntent.SetDownloadReadTimeoutMinutes(next))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase timeout",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
