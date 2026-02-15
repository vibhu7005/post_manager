package com.example.demoapplication.data.general

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun main() {
    runBlocking {


        CoroutineScope(Job()).launch {
            supervisorScope {
                for (i in 1..10000000000000) {
                    launch {
                        delay(2000)
                        println("A")
                    }
                }

                launch {
                    delay(500)
                    println("B")
                }
            }
            println("C")
        }
        delay(6000)
    }
}

suspend fun execute() {
    println("hello")
}