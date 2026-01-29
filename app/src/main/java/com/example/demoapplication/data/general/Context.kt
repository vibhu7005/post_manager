package com.example.demoapplication.data.general

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun main() {
    runBlocking {
        exampleCoroutineScope()
    }
}

    suspend fun exampleCoroutineScope() {

        coroutineScope {
            launch {
                delay(3000)
                println("Task 1")
            }
            launch {
                delay(3000)
                println("Task 2")
            }
        }
        println("After coroutineScope")
    }
