package com.aeliavision.novagrab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.aeliavision.novagrab.ui.theme.PrimaryGradientBrush
import com.aeliavision.novagrab.ui.theme.ObsidianBackground

sealed class ObsidianButtonStyle {
    data object Primary : ObsidianButtonStyle()
    data object Secondary : ObsidianButtonStyle()
}

@Composable
fun ObsidianButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ObsidianButtonStyle = ObsidianButtonStyle.Primary,
    leading: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
) {
    val shape = RoundedCornerShape(999.dp)

    val background: Brush? = when (style) {
        ObsidianButtonStyle.Primary -> PrimaryGradientBrush

        ObsidianButtonStyle.Secondary -> null
    }

    val containerColor = when (style) {
        ObsidianButtonStyle.Primary -> Color.Transparent
        ObsidianButtonStyle.Secondary -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (style) {
        ObsidianButtonStyle.Primary -> ObsidianBackground
        ObsidianButtonStyle.Secondary -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                if (style is ObsidianButtonStyle.Primary) {
                    shadowElevation = 1.dp.toPx()
                    ambientShadowColor = com.aeliavision.novagrab.ui.theme.PrimaryCyan.copy(alpha = 0.20f)
                    spotShadowColor = com.aeliavision.novagrab.ui.theme.PrimaryCyan.copy(alpha = 0.20f)
                }
            }
            .clip(shape)
            .background(containerColor)
            .then(if (background != null) Modifier.background(background, shape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button) { onClick() }
            .padding(contentPadding)
            .defaultMinSize(minHeight = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}

@Composable
fun ObsidianIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                shadowElevation = 1.dp.toPx()
                ambientShadowColor = com.aeliavision.novagrab.ui.theme.PrimaryCyan.copy(alpha = 0.20f)
                spotShadowColor = com.aeliavision.novagrab.ui.theme.PrimaryCyan.copy(alpha = 0.20f)
            }
            .clip(shape)
            .background(PrimaryGradientBrush, shape)
            .clickable(enabled = enabled, role = Role.Button) { onClick() }
            .padding(12.dp)
            .defaultMinSize(minHeight = 44.dp, minWidth = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
