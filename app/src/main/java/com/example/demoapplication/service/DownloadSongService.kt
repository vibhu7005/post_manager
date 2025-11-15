package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
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


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        val songName = intent?.getStringExtra("MUSIC_KEY")
        val msg = Message.obtain()
        msg.obj = songName
        msg.arg1 = startId
        thread.mHandler.sendMessage(msg)
        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}