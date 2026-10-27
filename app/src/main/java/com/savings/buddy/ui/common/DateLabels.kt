package com.savings.buddy.ui.common

import java.time.LocalDate

/** 流水日期展示：今天/昨天/M月d日 */
fun dateLabel(epochDay: Long, todayEpochDay: Long): String = when (epochDay) {
    todayEpochDay -> "今天"
    todayEpochDay - 1 -> "昨天"
    else -> LocalDate.ofEpochDay(epochDay).let { "${it.monthValue}月${it.dayOfMonth}日" }
}
