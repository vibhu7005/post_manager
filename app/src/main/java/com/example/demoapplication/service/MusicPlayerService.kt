package com.example.demoapplication.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MusicPlayerService : Service() {

    private var binder : IBinder? = MusicPlayerBinder()


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