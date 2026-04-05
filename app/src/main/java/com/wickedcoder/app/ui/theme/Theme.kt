package com.wickedcoder.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary            = Indigo500,
    onPrimary          = Surface,
    primaryContainer   = Purple100,
    onPrimaryContainer = Indigo700,
    secondary          = Purple400,
    onSecondary        = Surface,
    background         = Background,
    onBackground       = TextPrimary,
    surface            = Surface,
    onSurface          = TextPrimary,
    surfaceVariant     = Background,
    onSurfaceVariant   = TextSecondary,
    outline            = Divider,
    error              = DebitRed,
)

@Composable
fun SpendSenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
