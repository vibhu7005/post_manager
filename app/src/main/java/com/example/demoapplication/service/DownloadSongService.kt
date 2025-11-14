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

    override fun onCreate() {
        super.onCreate()
        thread = DownloadThread()
        thread.start()
        Thread.sleep(300)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songName = intent?.getStringExtra("MUSIC_KEY")
        val msg = Message.obtain()
        msg.obj = songName
        thread.mHandler.sendMessage(msg)
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}