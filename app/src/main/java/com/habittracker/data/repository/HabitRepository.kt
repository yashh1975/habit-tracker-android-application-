package com.habittracker.data.repository
import android.util.Log
import com.habittracker.data.model.*
import com.habittracker.util.SessionManager
import com.habittracker.util.UserPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

class HabitRepository {
    private val sheetsRepository = GoogleSheetsRepository()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "HabitRepository"
    
    companion object {
        private val mutex = Mutex()
        private val habitsStore = mutableMapOf<String, MutableList<Habit>>()
        private val _currentMonthData = MutableStateFlow(MonthData(Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR), emptyList()))
        private val _selectedDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        private val _currentMonthAllHabits = MutableStateFlow<List<Habit>>(emptyList())
    }
    
    val currentMonthData: StateFlow<MonthData> = _currentMonthData.asStateFlow()
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()
    val currentMonthAllHabits: StateFlow<List<Habit>> = _currentMonthAllHabits.asStateFlow()
    
    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()
    
    init { loadCurrentMonth() }
    
    fun clearSyncStatus() { _syncStatus.value = null }
    
    fun loadCurrentMonth() {
        val current = _currentMonthData.value; val key = "${current.month}-${current.year}"
        scope.launch {
            mutex.withLock {
                val existingHabits = habitsStore[key] ?: emptyList()
                _currentMonthAllHabits.value = existingHabits
                _currentMonthData.value = current.copy(habits = filterHidden(existingHabits, _selectedDay.value))
            }
            
            try { 
                val context = SessionManager.getContext()
                if (context != null) {
                    val token = SessionManager.getAccessToken(context)
                    if (token != null) {
                        Log.d(TAG, "Fetching remote data for $key")
                        val master = sheetsRepository.fetchMasterHabits(token) ?: emptyList()
                        val monthDataMap = sheetsRepository.fetchMonthData(token, current.month.toString(), current.year.toString())
                        
                        val remoteHabits = master.map { masterHabit ->
                            val combinedDays = monthDataMap[masterHabit.name]?.toSet() ?: emptySet()
                            Habit(masterHabit.name, combinedDays, masterHabit.iconName)
                        }
                        
                        mutex.withLock {
                            habitsStore[key] = remoteHabits.toMutableList()
                            _currentMonthAllHabits.value = remoteHabits
                            _currentMonthData.value = current.copy(habits = filterHidden(remoteHabits, _selectedDay.value))
                            Log.d(TAG, "Successfully synced $key with ${remoteHabits.size} habits")
                        }
                    } 
                }
            } catch (e: Exception) { 
                Log.e(TAG, "Error loading month data", e)
            } 
        }
    }

    private fun filterHidden(habits: List<Habit>, day: Int): List<Habit> {
        val hiddenCode = 100 + day
        return habits.filter { !it.completedDays.contains(hiddenCode) }
    }

    fun toggleHabitCompletion(habitName: String) {
        val current = _currentMonthData.value; val key = "${current.month}-${current.year}"; val day = _selectedDay.value
        scope.launch {
            var wasCompleted = false
            mutex.withLock {
                val habits = habitsStore[key] ?: return@withLock
                val index = habits.indexOfFirst { it.name == habitName }
                if (index != -1) {
                    val h = habits[index]; val days = h.completedDays.toMutableSet()
                    wasCompleted = days.contains(day)
                    if (wasCompleted) days.remove(day) else days.add(day)
                    days.remove(100 + day)
                    habits[index] = h.copy(completedDays = days.toSet())
                    habitsStore[key] = habits
                    _currentMonthAllHabits.value = habits.toList()
                    _currentMonthData.value = current.copy(habits = filterHidden(habits, day))
                }
            }
            
            try {
                 val context = SessionManager.getContext()
                 val token = context?.let { SessionManager.getAccessToken(it) }
                 if (token != null) {
                     val status = if(!wasCompleted) "1" else ""
                     sheetsRepository.updateHabitStatus(token, current.month.toString(), current.year.toString(), habitName, day, status)
                     _syncStatus.value = "Saved"
                 }
            } catch (e: Exception) { 
                Log.e(TAG, "Failed to toggle habit completion", e)
                _syncStatus.value = "Sync Failed" 
            }
        }
    }

    fun addHabit(name: String, icon: String = "Check") {
        if (name.isBlank()) return; val current = _currentMonthData.value; val key = "${current.month}-${current.year}"
        scope.launch {
            mutex.withLock {
                val habits = habitsStore[key] ?: mutableListOf()
                if (habits.any { it.name.equals(name, true) }) return@withLock
                val h = Habit(name, iconName = icon)
                habits.add(h)
                habitsStore[key] = habits
                _currentMonthAllHabits.value = habits.toList()
                _currentMonthData.value = current.copy(habits = filterHidden(habits, _selectedDay.value))
            }
            
            try {
                  val context = SessionManager.getContext()
                  val token = context?.let { SessionManager.getAccessToken(it) }
                  if (token != null) {
                     sheetsRepository.addHabit(name, icon, token)
                     _syncStatus.value = "Added to Master List"
                     loadCurrentMonth()
                  }
            } catch (e: Exception) { 
                Log.e(TAG, "Failed to add habit", e)
                _syncStatus.value = "Failed" 
            }
        }
    }

    fun removeHabit(name: String) {
        val current = _currentMonthData.value; val key = "${current.month}-${current.year}"; val day = _selectedDay.value
        scope.launch {
            mutex.withLock {
                val habits = habitsStore[key] ?: return@withLock
                val index = habits.indexOfFirst { it.name == name }
                if (index != -1) {
                    val h = habits[index]; val days = h.completedDays.toMutableSet()
                    days.add(100 + day)
                    habits[index] = h.copy(completedDays = days.toSet())
                    habitsStore[key] = habits
                    _currentMonthAllHabits.value = habits.toList()
                    _currentMonthData.value = current.copy(habits = filterHidden(habits, day))
                }
            }
            
            try {
                 val context = SessionManager.getContext()
                 val token = context?.let { SessionManager.getAccessToken(it) }
                 if (token != null) {
                    sheetsRepository.updateHabitStatus(token, current.month.toString(), current.year.toString(), name, day, "2")
                    _syncStatus.value = "Hidden from Day $day"
                 }
            } catch (e: Exception) { 
                Log.e(TAG, "Failed to hide habit", e)
                _syncStatus.value = "Failed" 
            }
        }
    }

    fun deleteHabitPermanently(name: String) {
        val current = _currentMonthData.value; val key = "${current.month}-${current.year}"
        scope.launch {
            mutex.withLock {
                val habits = habitsStore[key] ?: return@withLock
                val index = habits.indexOfFirst { it.name == name }
                if (index != -1) {
                    habits.removeAt(index)
                    habitsStore[key] = habits
                    _currentMonthAllHabits.value = habits.toList()
                    _currentMonthData.value = current.copy(habits = filterHidden(habits, _selectedDay.value))
                }
            }
            
            try {
                val context = SessionManager.getContext()
                val token = context?.let { SessionManager.getAccessToken(it) }
                if (token != null) {
                    sheetsRepository.deleteHabitPermanently(token, name, current.month.toString(), current.year.toString())
                    _syncStatus.value = "Permanently Deleted"
                }
            } catch (e: Exception) { 
                Log.e(TAG, "Failed to delete habit permanently", e)
                _syncStatus.value = "Delete Failed" 
            }
        }
    }

    fun navigateToNextDay() { modifyDay(1) }
    fun navigateToPreviousDay() { modifyDay(-1) }
    private fun modifyDay(delta: Int) {
        val current = _currentMonthData.value; val cal = Calendar.getInstance(); cal.set(current.year, current.month - 1, _selectedDay.value); cal.add(Calendar.DAY_OF_MONTH, delta)
        val d = cal.get(Calendar.DAY_OF_MONTH); val m = cal.get(Calendar.MONTH) + 1; val y = cal.get(Calendar.YEAR)
        _selectedDay.value = d; 
         if (m != current.month || y != current.year) { 
             _currentMonthData.value = MonthData(m, y)
             loadCurrentMonth() 
         } else {
             val key = "${current.month}-${current.year}"
             scope.launch {
                 mutex.withLock {
                     val habits = habitsStore[key] ?: emptyList()
                     _currentMonthData.value = current.copy(habits = filterHidden(habits, d))
                 }
             }
         }
    }
    fun navigateToNextMonth() { modifyMonth(1) }
    fun navigateToPreviousMonth() { modifyMonth(-1) }
    private fun modifyMonth(delta: Int) {
        var (m, y) = _currentMonthData.value; m += delta; if (m > 12) { m = 1; y++ } else if (m < 1) { m = 12; y-- }
        _currentMonthData.value = MonthData(m, y); val cal = Calendar.getInstance(); cal.set(y, m - 1, 1); val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (_selectedDay.value > max) _selectedDay.value = max
        loadCurrentMonth()
    }
    fun resetCurrentMonth() {
        val current = _currentMonthData.value; val key = "${current.month}-${current.year}"
        scope.launch {
            mutex.withLock {
                habitsStore[key] = habitsStore[key]?.map { it.copy(completedDays = emptySet()) }?.toMutableList() ?: mutableListOf()
                _currentMonthData.value = current.copy(habits = habitsStore[key]?.toList() ?: emptyList())
            }
        }
    }
    fun calculateDailyProgress(habits: List<Habit>): Int { if (habits.isEmpty()) return 0; val day = _selectedDay.value; return (habits.count { it.completedDays.contains(day) } * 100) / habits.size }
    fun calculateMonthlyProgress(data: MonthData): Int {
        if (data.habits.isEmpty()) return 0; val cal = Calendar.getInstance(); cal.set(data.year, data.month - 1, 1); val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (data.habits.sumOf { it.completedDays.filter { it <= 31 }.size } * 100) / (data.habits.size * days)
    }
    fun calculateStreak(h: Habit): Int {
        var s = 0; val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH); var d = if (!h.completedDays.contains(today)) today - 1 else today
        while (d >= 1) { if (h.completedDays.contains(d)) s++ else break; d-- }
        return s
    }
}
