package com.habittracker.data.model

/**
 * Data class representing a month's data including habits.
 * 
 * @param month Month number (1-12, where 1 = January)
 * @param year Year (e.g., 2026)
 * @param habits List of habits for this month
 */
data class MonthData(
    val month: Int,
    val year: Int,
    val habits: List<Habit> = emptyList()
) {
    /**
     * Returns a human-readable month name (e.g., "March 2026")
     */
    fun getFormattedMonth(): String {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${monthNames[month - 1]} $year"
    }
}
