package com.example.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Background companion sync: re-runs the full live telemetry evaluation while the app
 * is closed so Lumi's persisted body is fresh on next launch.
 *
 * Deliberately uses the same idempotent full re-read as the foreground path (rather than
 * Health Connect change tokens) for robustness; change-token deltas are a future
 * optimization. Never retry-loops: a failed period simply waits for the next one.
 */
class LumiBackgroundSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            SerenityRepository(applicationContext).syncAllRealChannels()
            Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }
}

object LumiBackgroundSync {

    private const val UNIQUE_WORK_NAME = "lumi_background_sync"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<LumiBackgroundSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
