import com.example.preciseroadmap.data.remote.ApiClient
import com.example.preciseroadmap.data.remote.SegmentUploadRequest
import kotlinx.coroutines.launch

fun uploadTestSegment() {
    viewModelScope.launch {
        try {
            val segment = SegmentUploadRequest(
                coordinates = listOf(
                    listOf(52.5200, 13.4050),
                    listOf(52.5205, 13.4060)
                ),
                speed_limit = 50,
                source = "camera"
            )
            val res = ApiClient.api.uploadSegment(segment)
            println("Upload Response: ${res.status}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private val tracker = LiveTracker(application)
val trackedPoints = tracker.points

fun startTracking() {
    tracker.reset()
    tracker.start()
}

fun stopTrackingAndUpload() {
    tracker.stop()
    val coordinates = tracker.points.value
    if (coordinates.size >= 2) {
        viewModelScope.launch {
            // lokal speichern
            val id = coordinates.joinToString().hashCode().toString()
            val db = MapDatabase.get(getApplication())
            db.dao().insertSegment(
                SegmentEntity(
                    id = id,
                    coordinates = coordinates,
                    speedLimit = 0, // optional initial
                    source = "camera"
                )
            )

            // an Server senden
            try {
                val req = SegmentUploadRequest(
                    coordinates = coordinates,
                    speed_limit = 0,
                    source = "camera"
                )
                val res = ApiClient.api.uploadSegment(req)
                println("Upload erfolgreich: ${res.status}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

val viewModel: MainViewModel = viewModel()

Button(onClick = { viewModel.startTracking() }) {
    Text("Start Tracking")
}

Spacer(modifier = Modifier.height(8.dp))

Button(onClick = { viewModel.stopTrackingAndUpload() }) {
    Text("Stop + Upload")
}

MainViewModel.kt        ← Abfrage + Import von Segmenten bei Start
ui/MapScreen.kt         ← Overlay für Speed-Limits pro Straße

fun syncFromServer(currentLat: Double, currentLon: Double) {
    viewModelScope.launch {
        try {
            val latMin = currentLat - 0.005
            val latMax = currentLat + 0.005
            val lonMin = currentLon - 0.005
            val lonMax = currentLon + 0.005

            val segments = ApiClient.api.downloadArea(latMin, latMax, lonMin, lonMax)

            val dao = MapDatabase.get(getApplication()).dao()

            for (seg in segments) {
                val geoJson = kotlinx.serialization.json.Json.parseToJsonElement(seg.geometry)
                val coords = geoJson.jsonObject["coordinates"]
                    ?.jsonArray?.firstOrNull()
                    ?.jsonArray
                    ?.map { point ->
                        val p = point.jsonArray
                        listOf(p[1].toString().toDouble(), p[0].toString().toDouble())
                    } ?: continue

                val id = coords.joinToString().hashCode().toString()
                dao.insertSegment(
                    SegmentEntity(
                        id = id,
                        coordinates = coords,
                        speedLimit = seg.speed_limit,
                        source = seg.source
                    )
                )
            }

            println("Sync abgeschlossen: ${segments.size} neue Segmente")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun setDetectedSpeedLimit(limit: Int) {
    viewModelScope.launch {
        val db = MapDatabase.get(getApplication())
        val lastSegment = trackedPoints.value
        if (lastSegment.size < 2) return@launch

        val id = lastSegment.joinToString().hashCode().toString()

        db.dao().insertSegment(
            SegmentEntity(
                id = id,
                coordinates = lastSegment,
                speedLimit = limit,
                source = "camera"
            )
        )

        try {
            ApiClient.api.uploadSegment(
                SegmentUploadRequest(
                    coordinates = lastSegment,
                    speed_limit = limit,
                    source = "camera"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

val context = LocalContext.current
val settings = remember { SettingsStore(context) }
val detectionMode by settings.detectionMode.collectAsState(initial = "camera")

if (detectionMode == "camera") {
    CameraPreview { limit ->
        viewModel.setDetectedSpeedLimit(limit)
    }
} else {
    // Online-Erkennung (optional implementieren)
}

val store = SettingsStore(getApplication())
val wifiOnly = store.wifiOnly.first()
val syncEnabled = store.syncEnabled.first()

if (!syncEnabled) return@launch
if (wifiOnly && !isWifiConnected(getApplication())) return@launch