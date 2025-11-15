package com.example.demoapplication.service

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.demoapplication.AppConstants.TAG

class DownloadHandler(looper: Looper) : Handler(looper) {
    private lateinit var service: DownloadSongService
    override fun handleMessage(msg: Message) {
        downloadSong(msg)
    }

    fun setService(downloadService: DownloadSongService) {
        service = downloadService
    }

    private fun downloadSong(msg: Message) {
        Log.d(TAG, "download started for ${msg.obj}")
        Thread.sleep(3000)
        Log.d(TAG, "download Completed for ${msg.obj}")
        val stopped = service.stopSelfResult(msg.arg1)
        Log.d(TAG, "stopSelfResult(${msg.arg1}) returned: $stopped")
    }
}