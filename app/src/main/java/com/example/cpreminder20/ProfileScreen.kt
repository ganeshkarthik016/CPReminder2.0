package com.example.cpreminder20

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferenceManager = remember { PreferenceManager(context) }

    val savedHandle by preferenceManager.getHandle.collectAsState(initial = null)
    val isContestAlarmOn by preferenceManager.isContestAlarmOn.collectAsState(initial = true)
    val isDailyCheckOn by preferenceManager.isDailyCheckOn.collectAsState(initial = true) // New State

    var userProfile by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }

    // Auto-fetch profile
    LaunchedEffect(savedHandle) {
        if (!savedHandle.isNullOrEmpty()) {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitInstance.api.getUserInfo(savedHandle!!)
                if (response.status == "OK" && response.result.isNotEmpty()) {
                    userProfile = response.result[0]
                } else {
                    errorMessage = "User not found"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CP REMINDER 2.0", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // --- STATS CARD ---
        if (savedHandle != null && savedHandle!!.isNotEmpty()) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (userProfile != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👤 ${userProfile!!.handle}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Rating", style = MaterialTheme.typography.bodySmall); Text("${userProfile!!.rating ?: "N/A"}", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                            Column { Text("Rank", style = MaterialTheme.typography.bodySmall); Text(userProfile!!.rank?.uppercase() ?: "UNRATED", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = getRankColor(userProfile!!.rating)) }
                            Column { Text("Max", style = MaterialTheme.typography.bodySmall); Text("${userProfile!!.maxRating ?: "N/A"}", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            } else {
                Text("⚠️ ${errorMessage ?: "Stats failed to load"}", color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = textInput, onValueChange = { textInput = it },
            label = { Text("Codeforces Handle") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { scope.launch { preferenceManager.saveHandle(textInput) } }, modifier = Modifier.fillMaxWidth()) {
            Text("Update Profile")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // --- SWITCH 1: CONTEST ALARMS ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Contest Alarms (30m before)", style = MaterialTheme.typography.titleMedium)
            Switch(checked = isContestAlarmOn, onCheckedChange = { isChecked -> scope.launch { preferenceManager.setContestAlarm(isChecked) } })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SWITCH 2: DAILY CHECK (NEW) ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Daily Streak Check", style = MaterialTheme.typography.titleMedium)
                Text("Checks at 10:30 PM", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(
                checked = isDailyCheckOn,
                onCheckedChange = { isChecked -> scope.launch { preferenceManager.setDailyCheck(isChecked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF5722)) // Orange to show it's strict!
            )
        }
    }
}

fun getRankColor(rating: Int?): Color {
    if (rating == null) return Color.Black
    return when {
        rating < 1200 -> Color.Gray
        rating < 1400 -> Color(0xFF008000)
        rating < 1600 -> Color(0xFF03A89E)
        rating < 1900 -> Color(0xFF0000FF)
        rating < 2100 -> Color(0xFFAA00AA)
        rating < 2400 -> Color(0xFFFF8C00)
        else -> Color.Red
    }
}