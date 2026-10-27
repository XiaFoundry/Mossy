package com.savings.buddy.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.AchievementState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AchievementsViewModel(container: AppContainer) : ViewModel() {

    val states = container.achievementManager.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
