package com.example.demoapplication.service

import android.os.Looper

class DownloadThread : Thread() {
    public lateinit var mHandler : DownloadHandler

    override fun run() {
        Looper.prepare()
        mHandler = DownloadHandler(Looper.myLooper()!!)
        Looper.loop()
    }
}