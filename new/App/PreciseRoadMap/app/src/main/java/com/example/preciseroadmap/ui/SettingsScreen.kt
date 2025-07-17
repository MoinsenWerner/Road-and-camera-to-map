package com.example.preciseroadmap.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.example.preciseroadmap.data.local.SettingsStore
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(context: Context) {
    val store = remember { SettingsStore(context) }
    val coroutineScope = rememberCoroutineScope()

    val syncEnabled by store.syncEnabled.collectAsState(initial = true)
    val wifiOnly by store.wifiOnly.collectAsState(initial = false)
    val theme by store.theme.collectAsState(initial = "system")
    val detectionMode by store.detectionMode.collectAsState(initial = "camera")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Einstellungen", fontSize = 22.sp)

        Spacer(Modifier.height(16.dp))

        SettingSwitch("Synchronisation aktiv", syncEnabled) {
            coroutineScope.launch { store.setSyncEnabled(it) }
        }

        SettingSwitch("Nur über WLAN synchronisieren", wifiOnly) {
            coroutineScope.launch { store.setWifiOnly(it) }
        }

        SettingDropdown(
            label = "Design",
            options = listOf("system", "light", "dark"),
            selected = theme
        ) {
            coroutineScope.launch { store.setTheme(it) }
        }

        SettingDropdown(
            label = "Geschwindigkeits-Erkennung",
            options = listOf("camera", "online"),
            selected = detectionMode
        ) {
            coroutineScope.launch { store.setDetectionMode(it) }
        }
    }
}

@Composable
fun SettingSwitch(label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 16.sp)
        Switch(checked = value, onCheckedChange = onToggle)
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
fun SettingDropdown(label: String, options: List<String>, selected: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 16.sp)
        Box {
            Button(onClick = { expanded = true }) {
                Text(selected)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}