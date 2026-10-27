package com.savings.buddy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.savings.buddy.data.AppContainer
import com.savings.buddy.ui.achievements.AchievementsScreen
import com.savings.buddy.ui.components.AchievementPopup
import com.savings.buddy.ui.goals.GoalEditScreen
import com.savings.buddy.ui.goals.GoalsScreen
import com.savings.buddy.ui.home.HomeScreen
import com.savings.buddy.ui.i18n.LocalStrings
import com.savings.buddy.ui.records.RecordDetailScreen
import com.savings.buddy.ui.records.RecordsScreen
import com.savings.buddy.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val RECORDS = "records"
    const val GOALS = "goals"
    const val SETTINGS = "settings"
    const val ACHIEVEMENTS = "achievements"
    const val RECORD_DETAIL = "recordDetail/{recordId}"
    fun recordDetail(recordId: Long) = "recordDetail/$recordId"
    const val GOAL_EDIT = "goalEdit?goalId={goalId}"
    fun goalEdit(goalId: Long? = null) = "goalEdit?goalId=${goalId ?: -1L}"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun tabs(): List<Tab> {
    val t = LocalStrings.current
    return listOf(
        Tab(Routes.HOME, t.tabHome, Icons.Filled.Home),
        Tab(Routes.RECORDS, t.tabRecords, Icons.AutoMirrored.Filled.ReceiptLong),
        Tab(Routes.GOALS, t.tabGoals, Icons.Filled.Flag),
        Tab(Routes.SETTINGS, t.tabSettings, Icons.Filled.Settings),
    )
}

@Composable
fun AppRoot(container: AppContainer) {
    val navController: NavHostController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val tabList = tabs()
    val showBottomBar = currentRoute in tabList.map { it.route }

    // 应用启动：更新登录连击/首次启动并评估成就
    LaunchedEffect(Unit) {
        container.achievementManager.onAppOpen()
    }

    // 成就解锁弹窗队列（同时解锁多个时逐个展示）
    var pendingAchievements by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        container.achievementManager.unlockedEvents.collect { defs ->
            if (defs.isNotEmpty()) {
                pendingAchievements = pendingAchievements + defs.map { it.id }
            }
        }
    }
    val showingAchievement = pendingAchievements.firstOrNull()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabList.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    container = container,
                    onCreateGoal = { navController.navigate(Routes.goalEdit()) },
                )
            }
            composable(Routes.RECORDS) {
                RecordsScreen(
                    container = container,
                    onOpenDetail = { recordId -> navController.navigate(Routes.recordDetail(recordId)) },
                )
            }
            composable(
                route = Routes.RECORD_DETAIL,
                arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
            ) { entry ->
                val recordId = entry.arguments?.getLong("recordId") ?: -1L
                RecordDetailScreen(
                    container = container,
                    recordId = recordId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.GOALS) {
                GoalsScreen(
                    container = container,
                    onEditGoal = { goalId -> navController.navigate(Routes.goalEdit(goalId)) },
                    onCreateGoal = { navController.navigate(Routes.goalEdit()) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    container = container,
                    onGoAchievements = { navController.navigate(Routes.ACHIEVEMENTS) },
                )
            }
            composable(Routes.ACHIEVEMENTS) {
                AchievementsScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.GOAL_EDIT,
                arguments = listOf(navArgument("goalId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }),
            ) { entry ->
                val goalId = entry.arguments?.getLong("goalId") ?: -1L
                GoalEditScreen(
                    container = container,
                    goalId = if (goalId > 0) goalId else null,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }

    showingAchievement?.let { achievementId ->
        AchievementPopup(
            achievementId = achievementId,
            onDismiss = { pendingAchievements = pendingAchievements.drop(1) },
        )
    }
}
