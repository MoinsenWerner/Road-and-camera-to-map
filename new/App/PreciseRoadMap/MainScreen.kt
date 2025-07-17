@Composable
fun MainScreen(viewModel: MainViewModel) {
    val speed by viewModel.currentSpeed.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Aktuelle Geschwindigkeit:", fontSize = 20.sp)
        Text("${speed.roundToInt()} km/h", fontSize = 40.sp, fontWeight = FontWeight.Bold)
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val gps = GpsTracker(application.applicationContext)
    val currentSpeed = gps.start(application.applicationContext).stateIn(
        viewModelScope, SharingStarted.Eagerly, 0f
    )
}

package com.example.preciseroadmap.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import kotlin.random.Random

@Composable
fun MainScreen() {
    var speed by remember { mutableStateOf(0f) }

    // Simuliere GPS (wird später ersetzt)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            speed = Random.nextFloat() * 100f
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Live-Geschwindigkeit", fontSize = 22.sp)
        Text("${speed.toInt()} km/h", fontSize = 40.sp, style = MaterialTheme.typography.displayMedium)
    }
}