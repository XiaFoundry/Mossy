package com.savings.buddy.data

import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

/**
 * JSON 备份导出与恢复。
 * 导入前校验：文件格式、数据版本、金额合法性、ID 唯一性、关联关系。
 * v2：Goal 不再包含攒钱方式字段；导入兼容 v1 旧备份（忽略 planType/dailyFixedCents）。
 */
class BackupManager(
    private val db: AppDatabase,
    private val settingsStore: SettingsStore,
) {

    private val t get() = com.savings.buddy.ui.i18n.I18nStatic.systemStrings()

    companion object {
        const val FORMAT_VERSION = 2
    }

    suspend fun exportJson(): String {
        val goals = db.goalDao().getAllOnce()
        val records = db.recordDao().getAllOnce()
        val root = JSONObject()
        root.put("version", FORMAT_VERSION)
        root.put("exportedAtMillis", System.currentTimeMillis())
        root.put(
            "goals",
            JSONArray().apply {
                goals.forEach { g ->
                    put(
                        JSONObject().apply {
                            put("id", g.id)
                            put("name", g.name)
                            put("targetCents", g.targetCents)
                            if (g.deadlineEpochDay != null) put("deadlineEpochDay", g.deadlineEpochDay)
                            put("note", g.note)
                            put("colorIndex", g.colorIndex)
                            put("status", g.status.name)
                            put("createdAtEpochDay", g.createdAtEpochDay)
                        }
                    )
                }
            },
        )
        root.put(
            "records",
            JSONArray().apply {
                records.forEach { r ->
                    put(
                        JSONObject().apply {
                            put("id", r.id)
                            put("goalId", r.goalId)
                            put("amountCents", r.amountCents)
                            put("saveEpochDay", r.saveEpochDay)
                            put("note", r.note)
                            put("createdAtMillis", r.createdAtMillis)
                            put("updatedAtMillis", r.updatedAtMillis)
                        }
                    )
                }
            },
        )
        return root.toString(2)
    }

    /** 校验并整体覆盖导入。校验失败抛 [IllegalArgumentException]，消息已按系统语言本地化。 */
    suspend fun importJson(json: String) {
        val root = try {
            JSONObject(json)
        } catch (e: Exception) {
            throw IllegalArgumentException(t.backupInvalidJson)
        }
        if (!root.has("version") || !root.has("goals") || !root.has("records")) {
            throw IllegalArgumentException(t.backupMissingFields)
        }
        val version = root.optInt("version", -1)
        if (version < 1 || version > FORMAT_VERSION) {
            throw IllegalArgumentException(t.backupUnsupportedVersion.format(version))
        }

        val goalsJson = root.getJSONArray("goals")
        val recordsJson = root.getJSONArray("records")
        val validStatus = GoalStatus.entries.map { it.name }.toSet()

        val goals = mutableListOf<Goal>()
        val goalIds = mutableSetOf<Long>()
        for (i in 0 until goalsJson.length()) {
            val o = goalsJson.getJSONObject(i)
            val id = o.getLong("id")
            if (!goalIds.add(id)) throw IllegalArgumentException(t.backupDuplicateGoal.format(id))
            val name = o.optString("name", "")
            if (name.isBlank()) throw IllegalArgumentException(t.backupGoalNameEmpty)
            val target = o.optLong("targetCents", -1)
            if (target <= 0) throw IllegalArgumentException(t.backupInvalidTarget.format(name))
            val status = o.optString("status", "")
            if (status !in validStatus) throw IllegalArgumentException(t.backupInvalidStatus.format(name))
            goals.add(
                Goal(
                    id = id,
                    name = name,
                    targetCents = target,
                    deadlineEpochDay = if (o.has("deadlineEpochDay")) o.getLong("deadlineEpochDay") else null,
                    note = o.optString("note", ""),
                    colorIndex = o.optInt("colorIndex", 0),
                    status = GoalStatus.valueOf(status),
                    createdAtEpochDay = o.optLong("createdAtEpochDay", 0),
                )
            )
        }

        val records = mutableListOf<SavingRecord>()
        val recordIds = mutableSetOf<Long>()
        for (i in 0 until recordsJson.length()) {
            val o = recordsJson.getJSONObject(i)
            val id = o.getLong("id")
            if (!recordIds.add(id)) throw IllegalArgumentException(t.backupDuplicateRecord.format(id))
            val goalId = o.getLong("goalId")
            if (goalId !in goalIds) throw IllegalArgumentException(t.backupOrphanRecord.format(id))
            val amount = o.optLong("amountCents", -1)
            if (amount <= 0) throw IllegalArgumentException(t.backupInvalidAmount.format(id))
            records.add(
                SavingRecord(
                    id = id,
                    goalId = goalId,
                    amountCents = amount,
                    saveEpochDay = o.optLong("saveEpochDay", 0),
                    note = o.optString("note", ""),
                    createdAtMillis = o.optLong("createdAtMillis", 0),
                    updatedAtMillis = o.optLong("updatedAtMillis", 0),
                )
            )
        }

        // 校验全部通过后，事务内覆盖导入
        db.withTransaction {
            db.recordDao().deleteAll()
            db.goalDao().deleteAll()
            goals.forEach { db.goalDao().insert(it) }
            records.forEach { db.recordDao().insert(it) }
        }
    }

    /** 清空全部数据 */
    suspend fun clearAllData() {
        db.withTransaction {
            db.recordDao().deleteAll()
            db.goalDao().deleteAll()
        }
    }
}
