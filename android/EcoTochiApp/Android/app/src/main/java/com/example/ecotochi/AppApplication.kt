package com.example.ecotochi

import android.app.Application
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ecotochi.worker.MonitoringWorker

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("AppApplication", "onCreate: encolando MonitoringWorker")

        val workRequest = OneTimeWorkRequestBuilder<MonitoringWorker>()
            .build()

        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }
}
