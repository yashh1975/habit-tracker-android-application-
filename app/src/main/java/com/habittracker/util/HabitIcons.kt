package com.habittracker.util
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
object HabitIcons {
    val IconsMap = mapOf(
        "Check" to Icons.Rounded.CheckCircle,
        "Fitness" to Icons.Rounded.FitnessCenter,
        "Book" to Icons.Rounded.Book,
        "Water" to Icons.Rounded.LocalDrink,
        "Meditation" to Icons.Rounded.SelfImprovement,
        "Sleep" to Icons.Rounded.Bedtime,
        "Food" to Icons.Rounded.Restaurant,
        "Work" to Icons.Rounded.Work,
        "Code" to Icons.Rounded.Code,
        "Health" to Icons.Rounded.Favorite,
        "Music" to Icons.Rounded.MusicNote,
        "Walk" to Icons.Rounded.DirectionsWalk,
        "Bike" to Icons.Rounded.DirectionsBike,
        "Sun" to Icons.Rounded.LightMode,
        "Moon" to Icons.Rounded.DarkMode
    )
    fun getIcon(name: String): ImageVector = IconsMap[name] ?: Icons.Rounded.CheckCircle
}
