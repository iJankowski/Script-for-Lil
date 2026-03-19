package com.scriptforlil.kindroidhealthsync.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Scheduled sync stays as a stub until Huawei Health Kit and Kindroid API are wired.
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "kindroid-health-sync"
    }
}
