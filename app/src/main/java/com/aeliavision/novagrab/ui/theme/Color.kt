package com.aeliavision.novagrab.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val ObsidianBackground = Color(0xFF0A0E14)
val ObsidianSurfaceLow = Color(0xFF0F141A)
val ObsidianSurfaceHigh = Color(0xFF1B2028)
val ObsidianSurfaceHighest = Color(0xFF20262F)

val ObsidianSurfaceContainer = Color(0xFF151A21)
val ObsidianSurfaceBright = Color(0xFF262C36)
val ObsidianSurfaceContainerLowest = Color(0xFF000000)

val PrimaryCyan = Color(0xFF9D1010)
val PrimaryCyanLight = Color(0xFFA1FAFF)
val PrimaryDim = Color(0xFF00E5EE)

val OnPrimaryFixed = Color(0xFF004346)
val OnPrimaryFixedVariant = Color(0xFF006266)

val OnSurfaceBase = Color(0xFFF1F3FC)
val OnSurfaceVariant = Color(0xFFA8ABB3)

val OutlineGhost = Color(0xFF44484F)

val Tertiary = Color(0xFFB8FFE9)
val TertiaryDim = Color(0xFF53E6C3)

val ErrorRed = Color(0xFFFF716C)
val ErrorDim = Color(0xFFD7383B)

val SuccessGreen = TertiaryDim

val PrimaryGradientBrush = Brush.linearGradient(
    colors = listOf(PrimaryCyanLight, PrimaryCyan)
)

val TextPrimary = OnSurfaceBase
val TextSecondary = OnSurfaceVariant
val TextTertiary = Color(0xFF8891A3)

val DarkPrimary = PrimaryCyan
val DarkOnPrimary = Color.White
val DarkPrimaryContainer = ObsidianSurfaceHighest
val DarkOnPrimaryContainer = OnSurfaceBase

val DarkSecondary = PrimaryCyanLight
val DarkOnSecondary = ObsidianBackground
val DarkSecondaryContainer = ObsidianSurfaceHigh
val DarkOnSecondaryContainer = OnSurfaceBase

val DarkBackground = ObsidianBackground
val DarkOnBackground = TextPrimary
val DarkSurface = ObsidianSurfaceLow
val DarkOnSurface = TextPrimary
val DarkSurfaceVariant = ObsidianSurfaceHigh
val DarkOnSurfaceVariant = TextSecondary

val DarkError = ErrorRed
val DarkOnError = Color.White
val DarkOutline = OutlineGhost
