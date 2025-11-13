package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.demoapplication.AppConstants
import java.security.Provider

class DownloadSongService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songName = intent?.getStringExtra("MUSIC_KEY")
        downloadSong(songName.orEmpty())
        return START_REDELIVER_INTENT
    }


    private fun downloadSong(songName : String) {
        Thread.sleep(6000)
        Log.d(AppConstants.TAG, "downloadedSong: $songName")
    }



    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}