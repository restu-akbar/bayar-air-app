package org.com.bayarair.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    speedDpPerSec: Float = 80f,
    gap: Dp = 48.dp,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val density = LocalDensity.current
    var boxWidthPx by remember { mutableFloatStateOf(0f) }
    var textWidthPx by remember { mutableFloatStateOf(0f) }
    val gapPx = with(density) { gap.toPx() }
    var offset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(text, speedDpPerSec, boxWidthPx, textWidthPx) {
        if (boxWidthPx <= 0f || textWidthPx <= 0f) return@LaunchedEffect
        val speedPxPerSec = with(density) { speedDpPerSec.dp.toPx() }
        val cycle = textWidthPx + gapPx
        offset = 0f
        var lastTime = 0L
        while (isActive) {
            val t = withFrameNanos { it }
            if (lastTime == 0L) {
                lastTime = t
                continue
            }
            val dtSec = (t - lastTime) / 1_000_000_000f
            lastTime = t

            offset -= speedPxPerSec * dtSec

            if (offset <= -cycle) {
                offset += cycle
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .onGloballyPositioned { boxWidthPx = it.size.width.toFloat() }
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(unbounded = true)
                .graphicsLayer { translationX = offset }
                .onGloballyPositioned {}
        ) {
            @Composable
            fun Unit() {
                Text(
                    text = text,
                    style = style,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .wrapContentWidth(unbounded = true)
                        .onGloballyPositioned {
                            val w = it.size.width.toFloat()
                            if (w > textWidthPx) textWidthPx = w
                        }
                )
                Spacer(Modifier.width(gap))
            }

            Unit()
            Unit()
            Unit()
        }
    }
}
