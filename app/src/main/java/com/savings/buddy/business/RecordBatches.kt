package com.savings.buddy.business

import com.savings.buddy.data.RecordWithGoal

/** 一个分配批次：一次「记录攒钱」产生的全部流水 */
data class RecordBatch(
    val batchId: String,
    val items: List<RecordWithGoal>,
) {
    val totalCents: Long get() = items.sumOf { it.record.amountCents }
    val createdAtMillis: Long get() = items.maxOf { it.record.createdAtMillis }
    val isMulti: Boolean get() = items.size > 1
}

/** 流水按批次分组（纯逻辑，可单元测试） */
object RecordBatches {

    fun group(records: List<RecordWithGoal>): List<RecordBatch> {
        return records
            .groupBy { it.record.batchId.ifEmpty { "legacy-${it.record.id}" } }
            .values
            .map { items ->
                RecordBatch(
                    batchId = items.first().record.batchId.ifEmpty { "legacy-${items.first().record.id}" },
                    items = items.sortedBy { it.record.id },
                )
            }
            .sortedByDescending { it.createdAtMillis }
    }
}
