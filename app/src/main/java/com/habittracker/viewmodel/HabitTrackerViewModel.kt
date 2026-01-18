package com.habittracker.viewmodel
import androidx.lifecycle.*
import com.habittracker.data.model.*
import com.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val monthData: MonthData = MonthData(1, 2026), 
    val selectedDay: Int = 1, 
    val dailyProgress: Int = 0, 
    val monthlyProgress: Int = 0, 
    val isLoading: Boolean = true, 
    val error: String? = null,
    val allHabits: List<Habit> = emptyList()
)

class HabitTrackerViewModel : ViewModel() {
    private val repository = HabitRepository()
    private val _uiState = MutableStateFlow(HomeUiState()); val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val syncStatus = repository.syncStatus
    
    init {
        viewModelScope.launch { repository.currentMonthData.collect { d -> updateState { it.copy(monthData = d) } } }
        viewModelScope.launch { repository.selectedDay.collect { d -> updateState { it.copy(selectedDay = d) } } }
        viewModelScope.launch { repository.currentMonthAllHabits.collect { h -> updateState { it.copy(allHabits = h) } } }
    }
    
    private fun updateState(update: (HomeUiState) -> HomeUiState) {
        val next = update(_uiState.value)
        _uiState.value = next.copy(
            dailyProgress = repository.calculateDailyProgress(next.monthData.habits), 
            monthlyProgress = repository.calculateMonthlyProgress(next.monthData), 
            isLoading = false
        )
    }
    
    fun toggleHabitCompletion(name: String) = repository.toggleHabitCompletion(name)
    fun addHabit(name: String, icon: String) = repository.addHabit(name, icon)
    fun removeHabit(name: String) = repository.removeHabit(name)
    fun deleteHabitPermanently(name: String) = repository.deleteHabitPermanently(name)
    fun clearSyncStatus() = repository.clearSyncStatus()
    fun calculateStreak(h: Habit) = repository.calculateStreak(h)
    fun onNextDay() = repository.navigateToNextDay()
    fun onPreviousDay() = repository.navigateToPreviousDay()
    fun onNextMonth() = repository.navigateToNextMonth()
    fun onPreviousMonth() = repository.navigateToPreviousMonth()
    fun onResetMonth() = repository.resetCurrentMonth()
}
