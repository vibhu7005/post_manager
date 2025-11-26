package com.example.demoapplication.service

import android.os.Looper

class DownloadThread(val service: MusicPlayerService) : Thread() {
    public lateinit var mHandler : DownloadHandler

    override fun run() {
        Looper.prepare()
        mHandler = DownloadHandler(Looper.myLooper()!!)
        mHandler.setService(service)
        Looper.loop()
    }
}