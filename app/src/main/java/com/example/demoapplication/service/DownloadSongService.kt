package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.demoapplication.AppConstants.TAG

//started service
class PlayerService() : Service() {
    lateinit var thread : DownloadThread
    private var latestStartId = 0
    private val binder = MyBinderService()

    inner class MyBinderService : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }
    override fun onBind(p0: Intent?): IBinder? {
        val songName = p0?.getStringExtra("MUSIC_KEY")
        downloadSong(songName ?: "")
        return binder
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        thread = DownloadThread(this)
        thread.start()
        Thread.sleep(300)
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    private fun downloadSong(msg: String) {
        Log.d(TAG, "download started for ${msg}")
        Thread.sleep(1000)
        Log.d(TAG, "download Completed for ${msg}")
    }

}