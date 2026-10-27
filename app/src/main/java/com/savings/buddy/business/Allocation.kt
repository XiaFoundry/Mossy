package com.savings.buddy.business

/** 攒钱分配的纯计算逻辑（可单元测试） */
object Allocation {

    /**
     * 把总金额平均分配到目标列表：
     * - 除不尽时，余数全部给第一个目标
     * - 目标列表为空返回空列表
     */
    fun distribute(totalCents: Long, goalIds: List<Long>): List<Pair<Long, Long>> {
        if (goalIds.isEmpty() || totalCents <= 0) return emptyList()
        val base = totalCents / goalIds.size
        val remainder = totalCents % goalIds.size
        return goalIds.mapIndexed { index, id ->
            id to (base + if (index == 0) remainder else 0L)
        }
    }
}
