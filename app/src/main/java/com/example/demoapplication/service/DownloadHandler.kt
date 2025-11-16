package com.example.demoapplication.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.ResultReceiver
import android.security.identity.ResultData
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.demoapplication.AppConstants.TAG

class DownloadHandler(looper: Looper) : Handler(looper) {
    private lateinit var service: DownloadSongService
    private var mContext : Context? = null

    override fun handleMessage(msg: Message) {
        downloadSong(msg)
    }


    fun setService(downloadService: DownloadSongService) {
        service = downloadService
    }

    fun setContext(context: Context) {
        mContext = context
    }

    private fun downloadSong(msg: Message) {
        Log.d(TAG, "download started for ${msg.obj}")
        Thread.sleep(3000)
        Log.d(TAG, "download Completed for ${msg.obj}")
        val stopped = service.stopSelfResult(msg.arg1)
        Log.d(TAG, "stopSelfResult(${msg.arg1}) returned: $stopped")
        val intent = Intent("DOWNLOAD_SERVICE")
        intent.putExtra("MESSAGE", msg.obj.toString())
        mContext?.let {
            LocalBroadcastManager.getInstance(it).sendBroadcast(intent)
        }
    }
}