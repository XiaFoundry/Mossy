package com.savings.buddy

import com.savings.buddy.business.Allocation
import org.junit.Assert.assertEquals
import org.junit.Test

class AllocationTest {

    @Test
    fun `整除时平均分配`() {
        assertEquals(
            listOf(1L to 500L, 2L to 500L),
            Allocation.distribute(1_000, listOf(1, 2)),
        )
    }

    @Test
    fun `除不尽时余数给第一个目标`() {
        assertEquals(
            listOf(1L to 501L, 2L to 500L),
            Allocation.distribute(1_001, listOf(1, 2)),
        )
        // 100 分分 3 份：333/333/333，余 1 分给第一个
        assertEquals(
            listOf(1L to 334L, 2L to 333L, 3L to 333L),
            Allocation.distribute(1_000, listOf(1, 2, 3)),
        )
    }

    @Test
    fun `单一目标拿走全部`() {
        assertEquals(listOf(1L to 1_000L), Allocation.distribute(1_000, listOf(1)))
    }

    @Test
    fun `空目标列表或非法总额返回空`() {
        assertEquals(emptyList<Pair<Long, Long>>(), Allocation.distribute(1_000, emptyList()))
        assertEquals(emptyList<Pair<Long, Long>>(), Allocation.distribute(0, listOf(1, 2)))
        assertEquals(emptyList<Pair<Long, Long>>(), Allocation.distribute(-5, listOf(1, 2)))
    }

    @Test
    fun `分配结果之和等于总额`() {
        val result = Allocation.distribute(9_999, listOf(1, 2, 3, 4))
        assertEquals(9_999L, result.sumOf { it.second })
    }
}
