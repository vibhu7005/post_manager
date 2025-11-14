package com.example.demoapplication.service

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.demoapplication.AppConstants.TAG

class DownloadHandler(looper: Looper) : Handler(looper) {
    override fun handleMessage(msg: Message) {
        downloadSong(msg)
    }

    private fun downloadSong(msg: Message) {
        Log.d(TAG, "download started for ${msg.obj}")
        Thread.sleep(3000)
        Log.d(TAG, "download Completed for ${msg.obj}")
    }
}