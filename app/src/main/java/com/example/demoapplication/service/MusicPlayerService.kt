package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.demoapplication.R

class MusicPlayerService : Service() {

    private var binder : IBinder? = MusicPlayerBinder()
    private var mediaPlayer : MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.short_test)
    }

    fun playMusic() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                Log.d("MusicPlayerService", "Music started")
            }
        } ?: Log.e("MusicPlayerService", "MediaPlayer is null")
    }

    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                Log.d("MusicPlayerService", "Music paused")
            }
        } ?: Log.e("MusicPlayerService", "MediaPlayer is null")
    }


    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}