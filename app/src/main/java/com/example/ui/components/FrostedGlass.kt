package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS 风格毛玻璃磨砂（Frosted Glass）容器
 * 在 Android 12 (API 31+) 上应用硬件高斯模糊，在全版本提供半透明透光与超细微光泽边框。
 */
@Composable
fun FrostedGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    tintColor: Color = MaterialTheme.colorScheme.surface,
    tintAlpha: Float = 0.82f,
    blurRadius: Dp = 20.dp,
    borderWidth: Dp = 0.8.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
    content: @Composable BoxScope.() -> Unit
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val effectiveTintAlpha = if (isBlurSupported) tintAlpha else (tintAlpha + 0.12f).coerceAtMost(0.98f)

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isBlurSupported && blurRadius > 0.dp) {
                    Modifier.blur(blurRadius)
                } else {
                    Modifier
                }
            )
            .background(tintColor.copy(alpha = effectiveTintAlpha))
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = borderColor.alpha * 1.5f),
                        borderColor.copy(alpha = borderColor.alpha * 0.5f)
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * 专门用于顶部 TopAppBar 或底部 NavigationBar 的超通透磨砂底栏修饰
 */
@Composable
fun FrostedGlassBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    blurRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val alpha = if (isBlurSupported) 0.85f else 0.95f

    Box(
        modifier = modifier
            .then(
                if (isBlurSupported) Modifier.blur(blurRadius) else Modifier
            )
            .background(backgroundColor.copy(alpha = alpha))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )
    ) {
        content()
    }
}
