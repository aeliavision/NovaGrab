package com.aeliavision.novagrab.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun ObsidianCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    ghostBorder: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState().value
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        label = "obsidian-card-scale",
    )

    val shape = RoundedCornerShape(cornerRadius)
    val base = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .ambientShadow(cornerRadius)
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .then(if (ghostBorder) Modifier.ghostBorder(cornerRadius) else Modifier)

    val clickable = if (onClick != null) {
        base.clickable(
            role = Role.Button,
            indication = null,
            interactionSource = interactionSource,
            onClick = onClick,
            onClickLabel = null,
        )
    } else {
        base
    }

    Box(
        modifier = clickable,
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}
