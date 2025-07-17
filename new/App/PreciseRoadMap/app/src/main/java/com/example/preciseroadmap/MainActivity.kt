package com.example.preciseroadmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.preciseroadmap.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
    // ...
}

./gradlew assembleDebug

app/build/outputs/apk/debug/app-debug.apk

val viewModel: MainViewModel = viewModel()

LaunchedEffect(Unit) {
    // optional GPS holen
    val location = LocationServices
        .getFusedLocationProviderClient(this@MainActivity)
        .lastLocation.await()

    viewModel.syncFromServer(location.latitude, location.longitude)
}

setContent {
    val ctx = this
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(ctx)
    } else {
        MapScreen()
    }

    Button(onClick = { showSettings = !showSettings }) {
        Text(if (showSettings) "Zurück zur Karte" else "Einstellungen")
    }
}

setContent {
    DynamicTheme {
        if (showSettings) SettingsScreen(this) else MapScreen()
        Button(onClick = { showSettings = !showSettings }) {
            Text(if (showSettings) "Zurück zur Karte" else "Einstellungen")
        }
    }
}

val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.UNMETERED) // nur WLAN
    .build()

val request = PeriodicWorkRequestBuilder<UploadWorker>(6, java.util.concurrent.TimeUnit.HOURS)
    .setConstraints(constraints)
    .build()

WorkManager.getInstance(this).enqueueUniquePeriodicWork(
    "map_upload", ExistingPeriodicWorkPolicy.KEEP, request
)