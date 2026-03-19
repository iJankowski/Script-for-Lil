package com.scriptforlil.kindroidhealthsync

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.scriptforlil.kindroidhealthsync.sync.SyncWorker
import java.util.concurrent.TimeUnit

class KindroidHealthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDefaultSync()
    }

    private fun scheduleDefaultSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
