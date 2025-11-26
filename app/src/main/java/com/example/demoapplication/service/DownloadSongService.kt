package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import android.os.ResultReceiver
import android.util.Log
import com.example.demoapplication.AppConstants.TAG
//started service
class DownloadSongService : Service() {
    lateinit var thread : DownloadThread
    private var latestStartId = 0

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        thread = DownloadThread(this)
        thread.start()
        Thread.sleep(300)
    }




    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}