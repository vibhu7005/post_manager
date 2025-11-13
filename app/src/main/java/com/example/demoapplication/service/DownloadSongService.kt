package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.demoapplication.AppConstants
import com.example.demoapplication.AppConstants.TAG
import java.security.Provider
//started service
class DownloadSongService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songName = intent?.getStringExtra("MUSIC_KEY")
        downloadSong(songName.orEmpty())
        return START_REDELIVER_INTENT
    }


    private fun downloadSong(songName : String) {
        Thread {
            Log.d(AppConstants.TAG, "downloadSong started: $songName")
            Thread.sleep(6000)
            Log.d(AppConstants.TAG, "downloadedSong: $songName")
        }.start()
    }



    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}