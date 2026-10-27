package com.savings.buddy.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val fullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

/** 流水日期展示：今天/昨天/M月d日 */
fun dateLabel(epochDay: Long, todayEpochDay: Long): String = when (epochDay) {
    todayEpochDay -> "今天"
    todayEpochDay - 1 -> "昨天"
    else -> LocalDate.ofEpochDay(epochDay).let { "${it.monthValue}月${it.dayOfMonth}日" }
}

/** 完整年月日：yyyy年M月d日 */
fun fullDateLabel(epochDay: Long): String =
    fullDateFormatter.format(LocalDate.ofEpochDay(epochDay))

/** 时分：HH:mm（按记录创建时间） */
fun timeLabel(createdAtMillis: Long): String =
    timeFormatter.format(Instant.ofEpochMilli(createdAtMillis).atZone(ZoneId.systemDefault()))
