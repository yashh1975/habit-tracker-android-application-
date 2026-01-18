package com.habittracker.ui.screens
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ui.components.*
import com.habittracker.viewmodel.HabitTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitTrackerScreen(onNavigateBack: () -> Unit, onNavigateToStats: () -> Unit, viewModel: HabitTrackerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAdd by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(syncStatus) { syncStatus?.let { snackbarHostState.showSnackbar(it); viewModel.clearSyncStatus() } }
    if (showAdd) { AddHabitDialog(onDismiss = { showAdd = false }, onConfirm = { n, i -> viewModel.addHabit(n, i); showAdd = false }) }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete Habit") },
            text = { Text("Do you want to delete '${habitToDelete}' permanently from your master list, or just hide it for today?") },
            confirmButton = {
                Button(onClick = { 
                    habitToDelete?.let { viewModel.deleteHabitPermanently(it) }
                    habitToDelete = null 
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Permanent Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    habitToDelete?.let { viewModel.removeHabit(it) }
                    habitToDelete = null 
                }) {
                    Text("Hide For Today")
                }
            }
        )
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(), 
        snackbarHost = { SnackbarHost(snackbarHostState) }, 
        topBar = { 
            TopAppBar(
                title = { 
                    Column { 
                        Text("Habit Tracker", fontWeight = FontWeight.Black)
                        val fullDate = remember(uiState.monthData, uiState.selectedDay) {
                            val c = Calendar.getInstance()
                            c.set(uiState.monthData.year, uiState.monthData.month - 1, uiState.selectedDay)
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(c.time)
                        }
                        Text(fullDate, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) 
                    } 
                }, 
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Rounded.ArrowBack, null) } }, 
                actions = { IconButton(onClick = onNavigateToStats) { Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary) } }
            ) 
        }, 
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Rounded.Add, null) } }
    ) { pv ->
        Box(Modifier.fillMaxSize().padding(pv)) {
            AnimatedContent(
                targetState = uiState.isLoading,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label = "loading"
            ) { loading ->
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (uiState.monthData.habits.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No habits yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(), 
                        contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp), 
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) { 
                        items(uiState.monthData.habits, key = { it.name }) { h -> 
                            HabitItem(
                                habit = h, 
                                selectedDay = uiState.selectedDay, 
                                onCheckedChange = { viewModel.toggleHabitCompletion(h.name) }, 
                                onDelete = { habitToDelete = h.name },
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            ) 
                        } 
                    }
                }
            }
        }
    }
}
