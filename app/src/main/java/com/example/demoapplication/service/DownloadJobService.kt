package com.example.demoapplication.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

class DownloadJobService : JobService() {
    
    companion object {
        private const val TAG = "DownloadJobService"
    }
    
    private var isJobRunning = false
    
    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.d(TAG, "Service started - onStartJob called")
        Log.d(TAG, "Thread name: ${Thread.currentThread().name}")
        Log.d(TAG, "Job ID: ${p0?.jobId}")
        
        isJobRunning = true
        Log.d(TAG, "Job is now running: $isJobRunning")

        Thread {
            Log.d(TAG, "Background thread started - Thread name: ${Thread.currentThread().name}")
            var i = 0
            while (i < 10 && isJobRunning) {
                Log.d(TAG, "Download progress: $i/10")
                i++
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread interrupted", e)
                    break
                }
            }
            Log.d(TAG, "Background work completed")
            jobFinished(p0, false)
        }.start()
        
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Log.d(TAG, "Service stopping - onStopJob called")
        Log.d(TAG, "Thread name: ${Thread.currentThread().name}")
        Log.d(TAG, "Job ID: ${p0?.jobId}")
        
        isJobRunning = false
        Log.d(TAG, "Job stopped, isJobRunning set to: $isJobRunning")
        
        return true
    }
}