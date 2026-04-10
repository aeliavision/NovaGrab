package com.aeliavision.novagrab.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.ui.theme.ObsidianSurfaceContainerLowest
import com.aeliavision.novagrab.ui.theme.PrimaryGradientBrush

@Composable
fun ObsidianProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    trackColor: Color = ObsidianSurfaceContainerLowest,
    fillBrush: Brush = PrimaryGradientBrush,
    cornerRadius: Dp = 24.dp,
) {
    val p = progress.coerceIn(0f, 1f)
    val animated = animateFloatAsState(
        targetValue = p,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "obsidian-progress",
    ).value

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(height)
                .background(fillBrush)
        )
    }
}
