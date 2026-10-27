package com.savings.buddy

import com.savings.buddy.business.RecordBatches
import com.savings.buddy.data.RecordWithGoal
import com.savings.buddy.data.SavingRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordBatchesTest {

    private fun record(id: Long, batchId: String, cents: Long = 1_000, createdAt: Long = id) =
        SavingRecord(
            id = id, goalId = 1, amountCents = cents, saveEpochDay = 0,
            note = "", createdAtMillis = createdAt, updatedAtMillis = createdAt, batchId = batchId,
        )

    private fun withGoal(r: SavingRecord) = RecordWithGoal(r, "目标")

    @Test
    fun `同批次多条流水分为一组`() {
        val batches = RecordBatches.group(
            listOf(
                withGoal(record(1, "b1")),
                withGoal(record(2, "b1")),
                withGoal(record(3, "b2")),
            )
        )
        assertEquals(2, batches.size)
        val b1 = batches.find { it.batchId == "b1" }!!
        assertEquals(2, b1.items.size)
        assertTrue(b1.isMulti)
        assertEquals(2_000, b1.totalCents)
    }

    @Test
    fun `单条批次不是多笔`() {
        val batches = RecordBatches.group(listOf(withGoal(record(1, "s1"))))
        assertFalse(batches.first().isMulti)
    }

    @Test
    fun `批次按创建时间倒序`() {
        val batches = RecordBatches.group(
            listOf(
                withGoal(record(1, "old", createdAt = 1_000)),
                withGoal(record(2, "new", createdAt = 2_000)),
            )
        )
        assertEquals("new", batches.first().batchId)
    }

    @Test
    fun `空批次ID回退为按记录独立分组`() {
        val batches = RecordBatches.group(
            listOf(withGoal(record(1, "")), withGoal(record(2, "")))
        )
        assertEquals(2, batches.size)
    }

    @Test
    fun `组内按记录ID升序`() {
        val batches = RecordBatches.group(
            listOf(
                withGoal(record(2, "b1")),
                withGoal(record(1, "b1")),
            )
        )
        assertEquals(listOf(1L, 2L), batches.first().items.map { it.record.id })
    }
}
