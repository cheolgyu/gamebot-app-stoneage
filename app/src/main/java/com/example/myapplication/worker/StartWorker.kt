package com.example.myapplication.worker

import android.content.Context
import android.util.Log;
import androidx.work.*
import java.util.concurrent.TimeUnit

class StartWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams){
    override fun doWork(): Result {

        Log.d("StartWorker","doWork")

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

            Log.d("StartWorker","while")
            val uploadWorkRequest = OneTimeWorkRequestBuilder<RunWorker>()
                .build()
            WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)

        return Result.success()
    }
}