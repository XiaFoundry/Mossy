package com.savings.buddy.business

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

/** 金额工具：内部一律使用人民币分（Long），展示转人民币元 */
object Money {

    private val grouping = DecimalFormat("#,###")

    /** 分 → 展示字符串，如 ¥1,234.56 / ¥350 */
    fun format(cents: Long): String {
        val negative = cents < 0
        val abs = kotlin.math.abs(cents)
        val yuan = abs / 100
        val fen = abs % 100
        val sign = if (negative) "¥-" else "¥"
        val yuanStr = grouping.format(yuan)
        return if (fen == 0L) "$sign$yuanStr" else "$sign$yuanStr." + fen.toString().padStart(2, '0')
    }

    /**
     * 用户输入的元字符串 → 分。
     * 只接受非负数字、最多两位小数；非法输入返回 null。
     */
    fun parseToCents(input: String): Long? {
        val t = input.trim()
        if (!Regex("^\\d{1,9}(\\.\\d{1,2})?$").matches(t)) return null
        return try {
            BigDecimal(t).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
        } catch (e: Exception) {
            null
        }
    }

    /** 分 → 输入框字符串（元） */
    fun toYuanInput(cents: Long): String {
        if (cents <= 0) return ""
        val yuan = cents / 100
        val fen = cents % 100
        return if (fen == 0L) yuan.toString() else "$yuan." + fen.toString().padStart(2, '0')
    }
}
