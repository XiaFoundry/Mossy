package com.savings.buddy.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.savings.buddy.business.RingColors
import com.savings.buddy.business.RingColors.Rgb

/**
 * 环状进度图：可复用组件。
 * 输入仅包括进度比例（已由统一业务逻辑计算并 clamp 到 0..1），不访问数据库。
 * 颜色随比例在 OKLab 空间连续插值；进度变化使用短时平滑动画。
 */
@Composable
fun RingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 22.dp,
    content: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ringProgress",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringColor = rememberRingColor(animated)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val half = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(half, half)
            val capStyle = Stroke(width = stroke, cap = StrokeCap.Round)

            // 背景轨道：低对比度中性灰
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = capStyle,
            )
            // 进度圆环：圆角端点，从顶部顺时针
            if (animated > 0.001f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = capStyle,
                )
            }
            // 100% 时中心加一个细圆点收尾，轻量完成反馈
            if (animated >= 0.999f) {
                drawCircle(
                    color = surfaceColor,
                    radius = stroke * 0.14f,
                    center = Offset(size.width / 2f, half),
                )
            }
        }
        content()
    }
}

@Composable
private fun rememberRingColor(progress: Float): Color {
    val rgb: Rgb = RingColors.colorAt(progress.toDouble())
    return Color(rgb.r.toFloat(), rgb.g.toFloat(), rgb.b.toFloat(), 1f)
}

/** 目标卡片等处使用的迷你进度色 */
fun ringColorFor(progress: Float): Color {
    val rgb = RingColors.colorAt(progress.toDouble().coerceIn(0.0, 1.0))
    return Color(rgb.r.toFloat(), rgb.g.toFloat(), rgb.b.toFloat(), 1f)
}
