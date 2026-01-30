package com.example.demoapplication.data.general

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Default+ parentJob)
        val job1 = scope.launch {
            launch { delay(1000)
            println("child 1")}
        }

        val job2 = scope.launch {
            delay(2000)
            println("child2")
        }

        parentJob.cancel()

        delay(4000)
    }


}
