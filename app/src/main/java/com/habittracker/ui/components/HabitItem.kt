package com.habittracker.ui.components
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habittracker.data.model.Habit
import com.habittracker.util.HabitIcons

@Composable
fun HabitItem(habit: Habit, selectedDay: Int, onCheckedChange: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val isDone = habit.completedDays.contains(selectedDay)
    val scale by animateFloatAsState(if (isDone) 1.02f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    val elevation by animateDpAsState(if (isDone) 8.dp else 2.dp, spring())
    
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).scale(scale).clickable { onCheckedChange() }.shadow(elevation, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isDone) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isDone) MaterialTheme.colorScheme.secondary else Color.Transparent).border(2.dp, if (isDone) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp), tint = Color.White)
                }
            }
            Spacer(Modifier.width(16.dp))
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isDone) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(HabitIcons.getIcon(habit.iconName), null, tint = if (isDone) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(habit.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface))
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)) }
        }
    }
}
