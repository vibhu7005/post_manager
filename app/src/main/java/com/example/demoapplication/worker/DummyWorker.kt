package com.example.demoapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class DummyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "DummyWorker"
    }
    
    override suspend fun doWork(): Result {
        inputData.keyValueMap.forEach { (key, value) ->
            Log.d(TAG, "Input Data - Key: $key, Value: $value")
        }
        for (i in 1..10) {
            Log.d(TAG, "Working on task $i/10")
            setProgress(workDataOf("PROGRESS" to i * 10))
            delay(1000)
        }
        return Result.success()
    }
}