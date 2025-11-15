package com.example.demoapplication.service

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.ResultReceiver
import android.security.identity.ResultData
import android.util.Log
import com.example.demoapplication.AppConstants.TAG

class DownloadHandler(looper: Looper) : Handler(looper) {
    private lateinit var service: DownloadSongService
    private var receiver : ResultReceiver? = null

    override fun handleMessage(msg: Message) {
        downloadSong(msg)
    }

    fun setResultReceiver(receiver: ResultReceiver?) {
        this.receiver = receiver
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
        receiver?.send(1, Bundle().apply {
            putString("downloadStatus", "${msg.obj} downloaded")
        })
    }
}