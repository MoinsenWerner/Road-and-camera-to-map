package com.example.roadcamera

import android.content.Context
import androidx.work.*

class MapSyncManager(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleSync(wifiOnly: Boolean) {
        val networkType = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()
        val request = OneTimeWorkRequestBuilder<MapSyncWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueue(request)
    }
}

class MapSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // TODO upload/download map data from server and merge
        return Result.success()
    }
}
