package com.aeliavision.novagrab.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.ghostBorder(
    cornerRadius: Dp = 24.dp,
): Modifier {
    val ghost = Color(0xFF44484F).copy(alpha = 0.15f)
    return this.border(
        border = BorderStroke(1.dp, ghost),
        shape = RoundedCornerShape(cornerRadius),
    )
}

@Composable
fun Modifier.glassCard(
    cornerRadius: Dp = 24.dp,
): Modifier {
    val surface = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f)
    val blurPx = 20.dp

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(surface)
}

fun Modifier.innerGlow(
    cornerRadius: Dp = 24.dp,
): Modifier {
    val glow = Color(0xFFF1F3FC).copy(alpha = 0.05f)
    return this.border(
        border = BorderStroke(1.dp, glow),
        shape = RoundedCornerShape(cornerRadius),
    )
}

fun Modifier.ambientShadow(
    cornerRadius: Dp = 24.dp,
): Modifier {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val shadowColor = Color(0xFFF1F3FC).copy(alpha = 0.05f)
    return this.graphicsLayer {
        this.shape = shape
        this.clip = false
        this.shadowElevation = 32.dp.toPx()
        this.ambientShadowColor = shadowColor
        this.spotShadowColor = shadowColor
    }
}
