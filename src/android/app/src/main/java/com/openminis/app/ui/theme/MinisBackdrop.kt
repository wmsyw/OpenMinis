package com.openminis.app.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Static mist/slate depth behind translucent chrome; no offscreen blur pass. */
@Composable
fun MinisBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = ChatColors.isDark
    val accent = ChatColors.sendButton
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val base = Brush.verticalGradient(
                    if (dark) listOf(Color(0xFF1D222C), Color(0xFF1B1C23))
                    else listOf(Color(0xFFEBF1FE), Color(0xFFF6F7FC)),
                )
                val upperTint = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = if (dark) 0.07f else 0.09f), Color.Transparent),
                    center = Offset(size.width * 0.9f, 0f),
                    radius = size.width * 1.25f,
                )
                val lowerTint = Brush.radialGradient(
                    colors = listOf(Color(0xFF9A88BA).copy(alpha = if (dark) 0.025f else 0.045f), Color.Transparent),
                    center = Offset(0f, size.height * 0.8f),
                    radius = size.width * 1.4f,
                )
                onDrawBehind {
                    drawRect(base)
                    drawRect(upperTint)
                    drawRect(lowerTint)
                }
            },
        content = content,
    )
}
