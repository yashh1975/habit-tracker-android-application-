package com.habittracker.ui.screens

import java.util.Calendar
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.data.model.*
import com.habittracker.viewmodel.HabitTrackerViewModel
import com.habittracker.ui.components.VibrancyHeatmap
import com.habittracker.util.HabitIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onNavigateBack: () -> Unit, viewModel: HabitTrackerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background))
    )) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { 
                TopAppBar(
                    title = { Text("Progress Insights", style = MaterialTheme.typography.headlineMedium) }, 
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                ) 
            }
        ) { pv ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(pv).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { 
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(32.dp)),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Whatshot, null, tint = Color(0xFFFF5722), modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("MONTHLY CONSISTENCY", style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(Modifier.height(20.dp))
                            VibrancyHeatmap(monthData = uiState.monthData, habits = uiState.allHabits)
                        }
                    }
                }
                
                item { 
                    Text("STREAK ANALYSIS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp, start = 4.dp)) 
                }
                
                uiState.monthData.habits.forEachIndexed { idx, h ->
                    item(key = h.name) {
                        AnimatedVisibility(
                            visible = visible, 
                            enter = fadeIn(tween(600, idx * 100)) + slideInVertically(tween(600, idx * 100)) { it / 3 }
                        ) {
                            StatCard(habit = h, viewModel = viewModel, month = uiState.monthData.month, year = uiState.monthData.year)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(habit: Habit, viewModel: HabitTrackerViewModel, month: Int, year: Int) {
    val streak = viewModel.calculateStreak(habit)
    val maxDays = remember(month, year) { Calendar.getInstance().apply { set(year, month - 1, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH).toFloat() }
    
    val completedCount = habit.completedDays.count { it <= 31 }
    val target = completedCount.toFloat() / maxDays
    
    var trigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { trigger = true }
    val progress by animateFloatAsState(if (trigger) target else 0f, tween(1800, easing = FastOutSlowInEasing), label = "anim")
    
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(28.dp)), 
        shape = RoundedCornerShape(28.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp), 
                    shape = RoundedCornerShape(18.dp), 
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) { 
                    Box(contentAlignment = Alignment.Center) { 
                        Icon(HabitIcons.getIcon(habit.iconName), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) 
                    } 
                }
                Spacer(Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) { 
                    Text(habit.name, style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (streak > 0) "$streak DAY STREAK" else "START TODAY!", 
                            style = MaterialTheme.typography.labelSmall,
                            color = if (streak > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (streak > 0) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Rounded.Whatshot, null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    CircularProgressIndicator(
                        progress = progress, 
                        modifier = Modifier.fillMaxSize(), 
                        strokeWidth = 6.dp, 
                        strokeCap = StrokeCap.Round,
                        color = if (target > 0.8f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    Text("${(target * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
