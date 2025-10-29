package com.example.demoapplication.service

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log

class DownlodService(looper: Looper) : Handler(looper) {

    override fun handleMessage(msg: Message) {
        downloadSong(msg.obj.toString())
    }

    fun downloadSong(name: String) {
        Log.d("Demo", "downloadingSong: $name")
        try {
            Thread.sleep(4000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        Log.d("Demo", "downloadedSong: $name")
    }
}