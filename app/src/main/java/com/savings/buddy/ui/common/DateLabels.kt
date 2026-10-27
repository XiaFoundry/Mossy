package com.savings.buddy.ui.common

import androidx.compose.runtime.Composable
import com.savings.buddy.ui.i18n.LocalAppLocale
import com.savings.buddy.ui.i18n.LocalStrings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** 流水日期展示：今天/昨天/本地化短日期（随应用语言） */
@Composable
fun dateLabel(epochDay: Long, todayEpochDay: Long): String {
    val t = LocalStrings.current
    return when (epochDay) {
        todayEpochDay -> t.dateToday
        todayEpochDay - 1 -> t.dateYesterday
        else -> shortDate(epochDay)
    }
}

/** 短日期：按应用语言本地化（如 2026/10/27） */
@Composable
fun shortDate(epochDay: Long): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(LocalAppLocale.current)
    return formatter.format(LocalDate.ofEpochDay(epochDay))
}

/** 完整年月日：按应用语言本地化（如 2026年10月27日 / Oct 27, 2026） */
@Composable
fun fullDateLabel(epochDay: Long): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(LocalAppLocale.current)
    return formatter.format(LocalDate.ofEpochDay(epochDay))
}

/** 时分：HH:mm（按记录创建时间） */
fun timeLabel(createdAtMillis: Long): String =
    DateTimeFormatter.ofPattern("HH:mm")
        .format(Instant.ofEpochMilli(createdAtMillis).atZone(ZoneId.systemDefault()))
