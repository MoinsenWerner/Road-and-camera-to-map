package com.example.preciseroadmap.data.remote

import android.content.Context
import androidx.work.*
import com.example.preciseroadmap.data.local.MapDatabase
import kotlinx.coroutines.coroutineScope

class UploadWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = coroutineScope {
        val db = MapDatabase.get(applicationContext)
        val unsynced = db.dao().getAllSegments().filter { it.speedLimit > 0 }

        unsynced.forEach {
            try {
                ApiClient.api.uploadSegment(
                    SegmentUploadRequest(it.coordinates, it.speedLimit, it.source)
                )
            } catch (e: Exception) {
                return@coroutineScope Result.retry()
            }
        }

        Result.success()
    }
}