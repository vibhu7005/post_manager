package com.example.demoapplication.data.general

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun main() {
    runBlocking {
        val job = CoroutineScope(Dispatchers.IO).launch {
            val childJob = launch {
                delay(2500)
                throw Exception("failed")
            }

            val child2Job = launch {
                println("hello")
            }
            println("main job")
        }

        job.join()
    }
}


