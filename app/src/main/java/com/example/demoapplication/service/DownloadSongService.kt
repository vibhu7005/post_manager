package com.example.demoapplication.service

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Message
import android.os.ResultReceiver
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.demoapplication.AppConstants.TAG
import com.example.demoapplication.MainActivity

//started service
class MusicPlayerService() : Service() {
    lateinit var thread : DownloadThread
    private var latestStartId = 0
    private val binder = MyBinderService()

    inner class MyBinderService : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
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