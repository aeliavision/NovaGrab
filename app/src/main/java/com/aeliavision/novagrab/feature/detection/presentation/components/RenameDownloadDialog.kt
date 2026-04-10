package com.aeliavision.novagrab.feature.detection.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.feature.downloader.domain.model.FileNameGenerator
import com.aeliavision.novagrab.ui.ObsidianButton
import com.aeliavision.novagrab.ui.ObsidianButtonStyle

@Composable
fun RenameDownloadDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    // Split into base name and extension for display
    val lastDot = initialName.lastIndexOf('.')
    val baseName = if (lastDot > 0) initialName.substring(0, lastDot) else initialName
    val extension = if (lastDot > 0) initialName.substring(lastDot) else ""

    var editableName by remember { mutableStateOf(baseName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save as") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editableName,
                    onValueChange = { editableName = FileNameGenerator.sanitize(it) },
                    label = { Text("File name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Text(
                            text = extension,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        },
        confirmButton = {
            ObsidianButton(
                text = "Download",
                onClick = { onConfirm("$editableName$extension") },
                style = ObsidianButtonStyle.Primary,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
