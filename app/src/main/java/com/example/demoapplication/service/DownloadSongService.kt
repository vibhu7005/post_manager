package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.demoapplication.AppConstants.TAG
import kotlinx.coroutines.*

//started service
class DownloadSongService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private val downloadQueue = mutableListOf<String>()
        private var isDownloading = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songName = intent?.getStringExtra("MUSIC_KEY")
        songName?.let { 
            synchronized(downloadQueue) {
                downloadQueue.add(it)
            }
        }
        
        if (!isDownloading) {
            serviceScope.launch {
                processDownloads()
            }
        }

        return START_REDELIVER_INTENT
    }
    
    private suspend fun processDownloads() {
        isDownloading = true
        
        while (downloadQueue.isNotEmpty()) {
            val songName = synchronized(downloadQueue) {
                if (downloadQueue.isNotEmpty()) downloadQueue.removeAt(0) else null
            }
            
            songName?.let { downloadSong(it) }
        }
        
        isDownloading = false
        stopSelf()
    }
    
    private suspend fun downloadSong(songName: String) {
        Log.d(TAG, "downloadSong started: $songName")
        delay(6000)
        Log.d(TAG, "downloadedSong: $songName")
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}