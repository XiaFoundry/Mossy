package com.savings.buddy.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.savings.buddy.business.AchievementDef
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/** 成就图标映射 */
fun achievementIcon(id: String): ImageVector = when {
    id == "first_login" -> Icons.Filled.WavingHand
    id.startsWith("saved") -> Icons.Filled.Savings
    id.startsWith("streak") -> Icons.Filled.LocalFireDepartment
    else -> Icons.Filled.EmojiEvents
}

private val CONFETTI_COLORS = listOf(
    Color(0xFFE4715C), Color(0xFFE0A63A), Color(0xFF35A08F),
    Color(0xFF5B8DEF), Color(0xFF9C6ADE), Color(0xFF2E9E4F),
)

private data class Particle(
    val xRatio: Float,      // 0..1 屏幕宽度位置
    val delayRatio: Float,  // 0..0.4 出发延迟
    val fallRatio: Float,   // 0.8..1.2 下落速度系数
    val sizePx: Float,
    val color: Color,
    val isBar: Boolean,
    val spin: Float,        // 总旋转角度
)

/**
 * 成就解锁弹窗：彩条 + 粒子下落动画，2.2 秒后自动可关。
 * 同时可解锁多个成就时逐个展示。
 */
@Composable
fun AchievementPopup(
    def: AchievementDef,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(def.id) {
        delay(3_500)
        onDismiss()
    }

    val confetti = remember { Animatable(0f) }
    LaunchedEffect(def.id) {
        confetti.animateTo(1f, animationSpec = tween(2_200, easing = LinearEasing))
    }
    val iconScale = remember { Animatable(0f) }
    LaunchedEffect(def.id) {
        iconScale.animateTo(
            1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        )
    }

    val particles = remember(def.id) {
        val random = Random(def.id.hashCode())
        List(64) {
            Particle(
                xRatio = random.nextFloat(),
                delayRatio = random.nextFloat() * 0.35f,
                fallRatio = 0.85f + random.nextFloat() * 0.35f,
                sizePx = 6f + random.nextFloat() * 10f,
                color = CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.size)],
                isBar = random.nextBoolean(),
                spin = (random.nextFloat() - 0.5f) * 720f,
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(color = Color.Transparent) {
            Box(Modifier.fillMaxSize()) {
                // 全屏彩带粒子层
                Canvas(Modifier.fillMaxSize()) {
                    val h = size.height
                    val w = size.width
                    val p = confetti.value
                    particles.forEach { particle ->
                        val t = ((p - particle.delayRatio) / (1f - particle.delayRatio)).coerceIn(0f, 1f)
                        if (t <= 0f) return@forEach
                        val y = -40f + t * (h + 80f) * particle.fallRatio
                        if (y > h + 40f) return@forEach
                        val x = particle.xRatio * w +
                            (sin(t * 6f + particle.xRatio * 10f) * 24f)
                        val alpha = (1f - t * 0.35f).coerceIn(0f, 1f)
                        val color = particle.color.copy(alpha = alpha)
                        if (particle.isBar) {
                            withTransform({
                                rotate(degrees = particle.spin * t, pivot = Offset(x, y))
                            }) {
                                drawRect(
                                    color = color,
                                    topLeft = Offset(x - particle.sizePx / 4f, y - particle.sizePx),
                                    size = androidx.compose.ui.geometry.Size(
                                        particle.sizePx / 2f,
                                        particle.sizePx * 2f,
                                    ),
                                )
                            }
                        } else {
                            drawCircle(
                                color = color,
                                radius = particle.sizePx / 2f,
                                center = Offset(x, y),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        tonalElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                achievementIcon(def.id),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(72.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale.value
                                        scaleY = iconScale.value
                                        alpha = iconScale.value.coerceIn(0f, 1f)
                                    },
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "成就解锁！",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                def.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                def.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = onDismiss) { Text("太好了") }
                        }
                    }
                }
            }
        }
    }
}

