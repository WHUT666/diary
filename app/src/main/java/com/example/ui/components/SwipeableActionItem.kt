package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * iOS 交互风格的左右滑动手势动作容器
 * - 左滑：呼出/触发删除动作（红色背景 + 垃圾桶图标 + 触觉震感）
 * - 右滑：呼出/触发置顶或快捷标记（主题强调色 + 别针图标 + 触觉震感）
 */
@Composable
fun SwipeableActionItem(
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    onPin: (() -> Unit)? = null,
    deleteLabel: String = "删除",
    pinLabel: String = "置顶",
    isPinned: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val thresholdPx = with(density) { 60.dp.toPx() }
    val actionTriggerPx = with(density) { 96.dp.toPx() }
    val maxDragDistancePx = with(density) { 180.dp.toPx() }
    val exitOffsetPx = with(density) { 500.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    var hasHapticTriggered by remember { mutableStateOf(false) }
    var isItemVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isItemVisible,
        exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
        ) {
            // 背景操作层
            val currentOffset = offsetX.value
            val isSwipingLeft = currentOffset < 0
            val isSwipingRight = currentOffset > 0

            if (isSwipingLeft && onDelete != null) {
                // 左滑删除区域
                val isTriggered = abs(currentOffset) >= actionTriggerPx
                val bgColor = if (isTriggered) {
                    Color(0xFFE53935)
                } else {
                    Color(0xFFFF5252)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTriggered) "松手删除" else deleteLabel,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = deleteLabel,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else if (isSwipingRight && onPin != null) {
                // 右滑置顶/标记区域
                val isTriggered = abs(currentOffset) >= actionTriggerPx
                val bgColor = if (isTriggered) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = pinLabel,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPinned) "取消置顶" else pinLabel,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 前景卡片内容层
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(onDelete, onPin, actionTriggerPx, maxDragDistancePx) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                hasHapticTriggered = false
                            },
                            onDragCancel = {
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                    hasHapticTriggered = false
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val finalOffset = offsetX.value
                                    if (finalOffset <= -actionTriggerPx && onDelete != null) {
                                        // 触发删除动作并播放触感
                                        HapticFeedbackUtil.warningFeedback(context)
                                        offsetX.animateTo(
                                            targetValue = -exitOffsetPx,
                                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                        )
                                        isItemVisible = false
                                        onDelete()
                                    } else if (finalOffset >= actionTriggerPx && onPin != null) {
                                        // 触发置顶/标记动作并弹回
                                        HapticFeedbackUtil.successFeedback(context)
                                        onPin()
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    } else {
                                        // 未达到阈值，弹簧回弹归零
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    hasHapticTriggered = false
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = (offsetX.value + dragAmount)

                                // 限制基于 density 计算的阻尼滑动距离
                                val clampedOffset = when {
                                    newOffset < 0 && onDelete != null -> newOffset.coerceAtLeast(-maxDragDistancePx)
                                    newOffset > 0 && onPin != null -> newOffset.coerceAtMost(maxDragDistancePx)
                                    else -> newOffset * 0.2f
                                }

                                scope.launch {
                                    offsetX.snapTo(clampedOffset)

                                    // 达到动作阈值时，触发一次清脆震颤提示
                                    val isPastThreshold = abs(clampedOffset) >= actionTriggerPx
                                    if (isPastThreshold && !hasHapticTriggered) {
                                        HapticFeedbackUtil.mediumImpact(context)
                                        hasHapticTriggered = true
                                    } else if (!isPastThreshold && hasHapticTriggered) {
                                        hasHapticTriggered = false
                                    }
                                }
                            }
                        )
                    }
            ) {
                content()
            }
        }
    }
}
