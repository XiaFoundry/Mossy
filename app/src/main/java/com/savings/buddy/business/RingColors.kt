package com.savings.buddy.business

import kotlin.math.cbrt
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * 圆环颜色：随完成比例在「柔和红橙 → 金黄 → 青绿 → 完成绿」之间连续插值。
 * 插值在 OKLab（感知均匀）空间进行，避免亮度突变。
 * 本文件为纯 Kotlin 实现（无 Android 依赖），可单元测试。
 */
object RingColors {

    /** gamma 编码后的 sRGB，分量 0..1 */
    data class Rgb(val r: Double, val g: Double, val b: Double)

    // 颜色关键帧：0 起(柔和红橙) → 1/3 积累(金黄) → 2/3 接近(青绿) → 1 完成(绿)
    val STOPS: List<Pair<Double, Rgb>> = listOf(
        0.00 to Rgb(0xE4 / 255.0, 0x71 / 255.0, 0x5C / 255.0), // #E4715C
        0.33 to Rgb(0xE0 / 255.0, 0xA6 / 255.0, 0x3A / 255.0), // #E0A63A
        0.66 to Rgb(0x35 / 255.0, 0xA0 / 255.0, 0x8F / 255.0), // #35A08F
        1.00 to Rgb(0x2E / 255.0, 0x9E / 255.0, 0x4F / 255.0), // #2E9E4F
    )

    private val stopOkLab: List<Triple<Double, DoubleArray, DoubleArray>> =
        STOPS.map { (p, rgb) -> Triple(p, rgbToOkLab(rgb), srgbToLinear(rgb)) }

    /** 取进度 0..1 对应的颜色（进度会被收敛到 0..1） */
    fun colorAt(progress: Double): Rgb {
        val p = min(1.0, max(0.0, progress))
        for (i in 0 until stopOkLab.size - 1) {
            val (p0, lab0, lin0) = stopOkLab[i]
            val (p1, lab1, lin1) = stopOkLab[i + 1]
            if (p <= p1) {
                val t = if (p1 <= p0) 1.0 else (p - p0) / (p1 - p0)
                val lab = DoubleArray(3) { lab0[it] + (lab1[it] - lab0[it]) * t }
                val lin = DoubleArray(3) { lin0[it] + (lin1[it] - lin0[it]) * t }
                return linearToSrgb(lin)
            }
        }
        return STOPS.last().second
    }

    // ---------- sRGB <-> 线性光 ----------
    internal fun srgbToLinear(c: Double): Double =
        if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    internal fun linearToSrgb(c: Double): Double =
        if (c <= 0.0031308) c * 12.92 else 1.055 * c.pow(1.0 / 2.4) - 0.055

    internal fun srgbToLinear(rgb: Rgb): DoubleArray =
        doubleArrayOf(srgbToLinear(rgb.r), srgbToLinear(rgb.g), srgbToLinear(rgb.b))

    internal fun linearToSrgb(lin: DoubleArray): Rgb = Rgb(
        linearToSrgb(lin[0]).coerceIn(0.0, 1.0),
        linearToSrgb(lin[1]).coerceIn(0.0, 1.0),
        linearToSrgb(lin[2]).coerceIn(0.0, 1.0),
    )

    // ---------- 线性 sRGB <-> OKLab（Björn Ottosson 公式） ----------
    internal fun rgbToOkLab(rgb: Rgb): DoubleArray {
        val lin = srgbToLinear(rgb)
        val l = cbrt(0.4122214708 * lin[0] + 0.5363325363 * lin[1] + 0.0514459929 * lin[2])
        val m = cbrt(0.2119034982 * lin[0] + 0.6806995451 * lin[1] + 0.1073969566 * lin[2])
        val s = cbrt(0.0883024619 * lin[0] + 0.2817188376 * lin[1] + 0.6299787005 * lin[2])
        return doubleArrayOf(
            0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
            1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
            0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s,
        )
    }

    // ---------- ARGB 便捷转换 ----------
    fun argbAt(progress: Double, alpha: Int = 0xFF): Int {
        val c = colorAt(progress)
        val toInt = { v: Double -> (v * 255 + 0.5).toInt().coerceIn(0, 255) }
        return (alpha shl 24) or (toInt(c.r) shl 16) or (toInt(c.g) shl 8) or toInt(c.b)
    }

    /** 亮度（OKLab 的 L），用于验证插值平滑、无亮度突变 */
    fun lightnessAt(progress: Double): Double = rgbToOkLab(colorAt(progress))[0]
}
