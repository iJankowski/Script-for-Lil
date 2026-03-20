package com.scriptforlil.kindroidhealthsync

import android.app.Application
import com.scriptforlil.kindroidhealthsync.sync.SyncScheduler

class KindroidHealthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedule(this, 15)
    }
}

