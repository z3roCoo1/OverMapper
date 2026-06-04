package xyz.northline.overmapper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Bone,
    primaryContainer = Linen,
    onPrimaryContainer = Ink,
    secondary = Sage,
    onSecondary = Bone,
    secondaryContainer = Linen,
    onSecondaryContainer = Ink,
    background = Bone,
    onBackground = Ink,
    surface = Bone,
    onSurface = Ink,
    surfaceVariant = Linen,
    onSurfaceVariant = MutedText,
    outline = TrailGrey
)

@Composable
fun OverMapperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
