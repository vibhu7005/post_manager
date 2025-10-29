package com.example.demoapplication.service

import android.os.Looper

class DownloadThread : Thread() {
    var service : DownlodService ?= null
    override fun run() {
        Looper.prepare()
        service = DownlodService(Looper.myLooper()!!)
        Looper.loop()
    }

}