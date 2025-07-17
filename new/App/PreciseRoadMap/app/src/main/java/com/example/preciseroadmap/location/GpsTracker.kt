class GpsTracker(context: Context) {
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L).build()
    private val locationFlow = MutableStateFlow(0.0f)

    @SuppressLint("MissingPermission")
    fun start(context: Context): Flow<Float> {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    locationFlow.value = it.speed * 3.6f // m/s → km/h
                }
            }
        }
        fusedLocation.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        return locationFlow
    }
}