package com.habittracker.ui.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.habittracker.util.HabitIcons
@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("Check") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Habit Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Select Icon", style = MaterialTheme.typography.labelLarge)
                LazyVerticalGrid(columns = GridCells.Adaptive(48.dp), modifier = Modifier.height(150.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HabitIcons.IconsMap.keys.toList()) { icon ->
                        val isSel = selectedIcon == icon
                        Surface(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).clickable { selectedIcon = icon }, color = if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, contentColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) {
                            Box(contentAlignment = Alignment.Center) { Icon(HabitIcons.getIcon(icon), null, modifier = Modifier.size(24.dp)) }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text, selectedIcon) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
