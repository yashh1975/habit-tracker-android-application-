package com.habittracker.data.model
data class Habit(
    val name: String,
    val completedDays: Set<Int> = emptySet(),
    val iconName: String = "Check"
) {
    fun isCompletedOn(day: Int): Boolean = completedDays.contains(day)
}
