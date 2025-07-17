import com.example.preciseroadmap.data.local.MapDatabase
import com.example.preciseroadmap.data.local.SegmentEntity
import org.maplibre.android.maps.MapboxMap
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.PolylineOptions

fun enableLocation(context: Context, mapboxMap: MapboxMap) {
    val db = MapDatabase.get(context)

    // Zeige gespeicherte Linienzüge
    CoroutineScope(Dispatchers.IO).launch {
        val segments = db.dao().getAllSegments()
        withContext(Dispatchers.Main) {
            for (seg in segments) {
                val latLngs = seg.coordinates.map { LatLng(it[0], it[1]) }
                mapboxMap.addPolyline(
                    PolylineOptions()
                        .addAll(latLngs)
                        .color(
                            if (seg.source == "camera") 0xFF00FF00.toInt() else 0xFFFFA500.toInt()
                        )
                        .width(4f)
                )
            }
        }
    }

    // Position wie zuvor...
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).build()
    locationClient.requestLocationUpdates(request, object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            val point = LatLng(loc.latitude, loc.longitude)
            mapboxMap.setCameraPosition(
                org.maplibre.android.camera.CameraPosition.Builder()
                    .target(point)
                    .zoom(17.0)
                    .build()
            )
        }
    }, ContextCompat.getMainExecutor(context))
}

fun saveSegmentLocally(coordinates: List<List<Double>>, speedLimit: Int, source: String) {
    viewModelScope.launch {
        val db = MapDatabase.get(getApplication())
        val id = coordinates.joinToString().hashCode().toString() // ggf. SHA256 als String
        db.dao().insertSegment(
            SegmentEntity(
                id = id,
                coordinates = coordinates,
                speedLimit = speedLimit,
                source = source
            )
        )
    }
}

location/
└── LiveTracker.kt         ← Sammelt GPS-Punkte in Session
MainViewModel.kt           ← Steuert Aufzeichnung + Upload

if (seg.speedLimit > 0) {
    val first = seg.coordinates.first()
    val latLng = LatLng(first[0], first[1])
    mapboxMap.addMarker(
        org.maplibre.android.annotations.MarkerOptions()
            .position(latLng)
            .title("${seg.speedLimit} km/h")
    )
}

data/local/
└── SettingsStore.kt     ← DataStore Wrapper
ui/
└── SettingsScreen.kt    ← UI für Einstellungen