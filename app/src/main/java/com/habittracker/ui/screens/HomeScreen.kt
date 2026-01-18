package com.habittracker.ui.screens
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.habittracker.ui.components.VibrancyHeatmap
import com.habittracker.viewmodel.HabitTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToHabits: () -> Unit, isDarkTheme: Boolean, onToggleTheme: () -> Unit, viewModel: HabitTrackerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val dpAnim by animateFloatAsState(uiState.dailyProgress / 100f, tween(1200, easing = FastOutSlowInEasing), label = "dp")
    val mpAnim by animateFloatAsState(uiState.monthlyProgress / 100f, tween(1500, easing = FastOutSlowInEasing), label = "mp")
    
    LaunchedEffect(syncStatus) { syncStatus?.let { snackbarHostState.showSnackbar(it); viewModel.clearSyncStatus() } }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background))
    )) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Habit Pulse", style = MaterialTheme.typography.headlineMedium) },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { pv ->
            Column(modifier = Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                
                val navShape = RoundedCornerShape(24.dp)
                val navBg = MaterialTheme.colorScheme.surface
                val navShadow = 4.dp
                val navBorder = 1.dp
                
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(navShadow, navShape),
                    shape = navShape,
                    colors = CardDefaults.cardColors(containerColor = navBg),
                    border = androidx.compose.foundation.BorderStroke(navBorder, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { viewModel.onPreviousMonth() }) { Icon(Icons.Rounded.ChevronLeft, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) }
                        Text(uiState.monthData.getFormattedMonth().uppercase(), style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        IconButton(onClick = { viewModel.onNextMonth() }) { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().shadow(navShadow, navShape),
                    shape = navShape,
                    colors = CardDefaults.cardColors(containerColor = navBg),
                    border = androidx.compose.foundation.BorderStroke(navBorder, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { viewModel.onPreviousDay() }) { Icon(Icons.Rounded.ChevronLeft, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp)) }
                        val dateText = remember(uiState.monthData, uiState.selectedDay) { 
                            val c = Calendar.getInstance(); c.set(uiState.monthData.year, uiState.monthData.month - 1, uiState.selectedDay)
                            SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(c.time).uppercase()
                        }
                        Text(dateText, style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                        IconButton(onClick = { viewModel.onNextDay() }) { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp)) }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().shadow(navShadow, navShape),
                    shape = navShape,
                    colors = CardDefaults.cardColors(containerColor = navBg),
                    border = androidx.compose.foundation.BorderStroke(navBorder, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DAILY FOCUS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(20.dp))
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                            CircularProgressIndicator(progress = dpAnim, modifier = Modifier.fillMaxSize(), strokeWidth = 14.dp, strokeCap = StrokeCap.Round, color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uiState.dailyProgress}%", style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp))
                                Text("DONE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text("MONTHLY TARGET", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text("${uiState.monthlyProgress}%", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(progress = mpAnim, modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)), strokeCap = StrokeCap.Round, color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        
                        Spacer(Modifier.height(32.dp))
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)).padding(16.dp)) {
                            Column {
                                Text("CONSISTENCY INDEX", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                VibrancyHeatmap(monthData = uiState.monthData, habits = uiState.allHabits)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onNavigateToHabits,
                    modifier = Modifier.fillMaxWidth().height(60.dp).shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Bolt, null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("OPEN HABIT STACK", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
