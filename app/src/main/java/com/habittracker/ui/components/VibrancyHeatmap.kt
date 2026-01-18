package com.habittracker.ui.components
import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.data.model.MonthData
import com.habittracker.data.model.Habit

@Composable
fun VibrancyHeatmap(monthData: MonthData, habits: List<Habit>, modifier: Modifier = Modifier) {
    val maxDays = remember(monthData) { 
        Calendar.getInstance().apply { 
            set(Calendar.YEAR, monthData.year)
            set(Calendar.MONTH, monthData.month - 1)
            set(Calendar.DAY_OF_MONTH, 1) 
        }.getActualMaximum(Calendar.DAY_OF_MONTH) 
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        (1..maxDays).toList().chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    val effectiveHabits = habits.filter { !it.completedDays.contains(100 + day) }
                    val effectiveTotal = effectiveHabits.size
                    val count = effectiveHabits.count { it.completedDays.contains(day) }
                    
                    val intensity = if (effectiveTotal > 0) count.toFloat() / effectiveTotal else 0f
                    
                    val color = when {
                        intensity == 0f -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        intensity < 0.3f -> Color(0xFF0E4429)
                        intensity < 0.6f -> Color(0xFF006D32)
                        intensity < 0.9f -> Color(0xFF26A641)
                        else -> Color(0xFF39D353) // Neon Green
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                            .then(if(intensity > 0) Modifier.border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day.toString(), 
                            fontSize = 8.sp, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = if (intensity > 0.4f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f).aspectRatio(1f)) }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
