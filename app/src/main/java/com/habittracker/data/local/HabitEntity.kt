package com.habittracker.data.local
import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters
@Entity(tableName = "habits", primaryKeys = ["name", "month", "year"])
data class HabitEntity(
    val name: String,
    val month: Int,
    val year: Int,
    val completedDays: Set<Int>,
    val iconName: String = "Check",
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false
)
class Converters {
    @TypeConverter
    fun fromSet(days: Set<Int>): String = days.joinToString(",")
    @TypeConverter
    fun toSet(data: String): Set<Int> {
        if (data.isEmpty()) return emptySet()
        return data.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }
}
