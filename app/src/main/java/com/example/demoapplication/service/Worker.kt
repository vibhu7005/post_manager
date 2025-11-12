package com.example.demoapplication.service

import android.util.Log
import com.example.demoapplication.AppConstants

class Worker(val threadId: Int) : Runnable {
    override fun run() {
        Log.d(AppConstants.TAG, "starting: thread $threadId")
        Thread.sleep(5000)
    }
}