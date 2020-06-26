package com.example.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class RunWorker (appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams){
    override fun doWork(): Result {

        Log.d("RunWorker","doWork")
        return Result.success()
    }


}
