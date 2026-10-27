package com.savings.buddy.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.savings.buddy.business.GoalDisplay
import java.util.Locale

/** 应用语言偏好（存储值） */
const val LANG_SYSTEM = 0
const val LANG_EN = 1
const val LANG_ZH_CN = 2
const val LANG_ZH_TW = 3
const val LANG_JA = 4
const val LANG_KO = 5

/** 把语言偏好解析为有效的语言标签；SYSTEM 时跟随系统 UI 语言，不支持的语言回退英语 */
fun resolveLanguageTag(prefKey: Int, systemLocale: Locale): String {
    return when (prefKey) {
        LANG_EN -> "en"
        LANG_ZH_CN -> "zh-CN"
        LANG_ZH_TW -> "zh-TW"
        LANG_JA -> "ja"
        LANG_KO -> "ko"
        else -> {
            when (systemLocale.language) {
                "zh" -> if (
                    systemLocale.country == "TW" || systemLocale.country == "HK" ||
                    systemLocale.country == "MO" || systemLocale.script == "Hant"
                ) "zh-TW" else "zh-CN"
                "en" -> "en"
                "ja" -> "ja"
                "ko" -> "ko"
                else -> "en"
            }
        }
    }
}

fun stringsFor(tag: String): Strings = when (tag) {
    "en" -> EnStrings
    "zh-TW" -> ZhTwStrings
    "ja" -> JaStrings
    "ko" -> KoStrings
    else -> ZhCnStrings
}

fun localeFor(tag: String): Locale = when (tag) {
    "en" -> Locale.US
    "zh-TW" -> Locale.TRADITIONAL_CHINESE
    "zh-CN" -> Locale.SIMPLIFIED_CHINESE
    "ja" -> Locale.JAPANESE
    "ko" -> Locale.KOREAN
    else -> Locale.getDefault()
}

/** 静态场景（通知、数据层异常文案）按系统语言解析 */
object I18nStatic {
    fun systemStrings(): Strings = stringsFor(resolveLanguageTag(LANG_SYSTEM, Locale.getDefault()))
}

val LocalStrings = staticCompositionLocalOf<Strings> { ZhCnStrings }
val LocalAppLocale = staticCompositionLocalOf { Locale.getDefault() }

/** 成就标题/描述按 id 解析 */
fun Strings.achievementTitle(id: String): String = when (id) {
    "first_login" -> achFirstLoginT
    "saved_10" -> achSaved10T
    "saved_50" -> achSaved50T
    "saved_100" -> achSaved100T
    "saved_1000" -> achSaved1000T
    "saved_5000" -> achSaved5000T
    "streak_3" -> achStreak3T
    "streak_7" -> achStreak7T
    "streak_30" -> achStreak30T
    "goals_1" -> achGoals1T
    "goals_3" -> achGoals3T
    "goals_10" -> achGoals10T
    else -> id
}

fun Strings.achievementDesc(id: String): String = when (id) {
    "first_login" -> achFirstLoginD
    "saved_10" -> achSaved10D
    "saved_50" -> achSaved50D
    "saved_100" -> achSaved100D
    "saved_1000" -> achSaved1000D
    "saved_5000" -> achSaved5000D
    "streak_3" -> achStreak3D
    "streak_7" -> achStreak7D
    "streak_30" -> achStreak30D
    "goals_1" -> achGoals1D
    "goals_3" -> achGoals3D
    "goals_10" -> achGoals10D
    else -> id
}

/** 目标展示状态标签按语言解析 */
fun Strings.goalStatusLabel(display: GoalDisplay): String = when (display) {
    GoalDisplay.EMPTY -> statusEmpty
    GoalDisplay.RUNNING -> statusRunning
    GoalDisplay.COMPLETED -> statusCompleted
    GoalDisplay.OVERDUE -> statusOverdue
    GoalDisplay.PAUSED -> statusPaused
    GoalDisplay.ARCHIVED -> statusArchived
}

/** 全部 UI 文案（类型安全，五种语言各一份实例） */
class Strings(
    // 导航
    val tabHome: String, val tabRecords: String, val tabGoals: String, val tabSettings: String,
    // 首页
    val homeAllGoals: String,
    val homeSavedPrefix: String,
    val homeRemaining: String,
    val homeExceeded: String,
    val homeRecordButton: String,
    val homeMonthTotal: String,
    val homeAllTotal: String,
    val homeEmptyTitle: String,
    val homeEmptyDesc: String,
    val homeCreateGoal: String,
    // 分配对话框
    val allocTitle: String,
    val allocTotalLabel: String,
    val allocNoGoals: String,
    val allocModeSingle: String,
    val allocModeMulti: String,
    val allocGoalPrefix: String,
    val allocSelectGoal: String,
    val allocAmountLabel: String,
    val allocExpandList: String,
    val allocCollapseList: String,
    val allocYuan: String,
    val allocSelectedSummary: String,
    val allocUndistributed: String,
    val allocOver: String,
    val allocFullyDistributed: String,
    val allocDatePrefix: String,
    val allocTodaySuffix: String,
    val allocNoteLabel: String,
    val allocSave: String,
    val allocCancel: String,
    // 通用错误
    val errNoGoals: String,
    val errTotalFormat: String,
    val errTotalPositive: String,
    val errFutureDate: String,
    val errGoalRequired: String,
    val errAmountPositive: String,
    val errAmountOverTotal: String,
    val errAmountFormat: String,
    val errAllocOverTotal: String,
    val errAllocAtLeast: String,
    // 单条记录对话框
    val rdEditTitle: String,
    val rdAddTitle: String,
    val rdDelete: String,
    // 目标页
    val goalsEmptyTitle: String,
    val goalsEmptyDesc: String,
    val goalsFab: String,
    val goalsArchived: String,
    val goalsArchivedCount: String,
    val cdExpand: String,
    val cdCollapse: String,
    val cdMore: String,
    val statusRunning: String,
    val statusCompleted: String,
    val statusOverdue: String,
    val statusPaused: String,
    val statusArchived: String,
    val statusEmpty: String,
    val goalsPause: String,
    val goalsResume: String,
    val goalsArchive: String,
    val goalsUnarchive: String,
    val goalsEdit: String,
    val goalsDelete: String,
    val goalsDeleteTitle: String,
    val goalsDeleteMsg: String,
    val goalDeadlinePrefix: String,
    // 目标编辑
    val geCreateTitle: String,
    val geEditTitle: String,
    val geBack: String,
    val geName: String,
    val geTargetAmount: String,
    val geDeadlineOptional: String,
    val geDeadlineNone: String,
    val geClearDeadline: String,
    val geThemeColor: String,
    val geNoteLabel: String,
    val geBtnCreate: String,
    val geBtnSave: String,
    val geErrNameEmpty: String,
    val geErrTargetFormat: String,
    val geErrTargetPositive: String,
    val geErrDeadlinePast: String,
    val geErrNotLoaded: String,
    // 记录页
    val recTitle: String,
    val recFilterAll: String,
    val recEmptyTitle: String,
    val recEmptyDesc: String,
    val recFab: String,
    val recBatchMulti: String,
    // 详情页
    val detailTitle: String,
    val detailBack: String,
    val detailEdit: String,
    val detailMissing: String,
    val detailDate: String,
    val detailTime: String,
    val detailGoal: String,
    val detailAmount: String,
    val detailNote: String,
    val detailNoNote: String,
    // 成就
    val achTitle: String,
    val achUnlockedProgress: String,
    val achLocked: String,
    val achFirstLoginT: String, val achFirstLoginD: String,
    val achSaved10T: String, val achSaved10D: String,
    val achSaved50T: String, val achSaved50D: String,
    val achSaved100T: String, val achSaved100D: String,
    val achSaved1000T: String, val achSaved1000D: String,
    val achSaved5000T: String, val achSaved5000D: String,
    val achStreak3T: String, val achStreak3D: String,
    val achStreak7T: String, val achStreak7D: String,
    val achStreak30T: String, val achStreak30D: String,
    val achGoals1T: String, val achGoals1D: String,
    val achGoals3T: String, val achGoals3D: String,
    val achGoals10T: String, val achGoals10D: String,
    val popupTitle: String,
    val popupOk: String,
    // 设置
    val setTitle: String,
    val setAppearance: String,
    val setThemeSystem: String,
    val setThemeLight: String,
    val setThemeDark: String,
    val setPalette: String,
    val setPaletteSystem: String,
    val setPaletteSystemSubModern: String,
    val setPaletteSystemSubLegacy: String,
    val setLanguage: String,
    val setLanguageSub: String,
    val langSystem: String,
    val setReminder: String,
    val setReminderDaily: String,
    val setReminderTime: String,
    val setReminderChange: String,
    val setAchievements: String,
    val setAchDesc: String,
    val setAchView: String,
    val setData: String,
    val setExport: String,
    val setImport: String,
    val setClear: String,
    val setAbout: String,
    val setPrivacy: String,
    val setVersion: String,
    val importConfirmTitle: String,
    val importConfirmMsg: String,
    val importConfirmYes: String,
    val clearConfirmTitle: String,
    val clearConfirmMsg: String,
    val clearConfirmYes: String,
    val commonCancel: String,
    val commonConfirm: String,
    // 消息
    val msgExportOk: String,
    val msgExportFail: String,
    val msgImportOk: String,
    val msgImportFail: String,
    val msgImportInvalid: String,
    val msgClearOk: String,
    val msgNotifTitle: String,
    val msgNotifText: String,
    val msgChannelName: String,
    val msgChannelDesc: String,
    // 备份校验错误
    val backupInvalidJson: String,
    val backupMissingFields: String,
    val backupUnsupportedVersion: String,
    val backupDuplicateGoal: String,
    val backupGoalNameEmpty: String,
    val backupInvalidTarget: String,
    val backupInvalidStatus: String,
    val backupDuplicateRecord: String,
    val backupOrphanRecord: String,
    val backupInvalidAmount: String,
    // 日期
    val dateToday: String,
    val dateYesterday: String,
)
