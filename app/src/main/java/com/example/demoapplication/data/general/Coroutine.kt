package com.example.demoapplication.data.general

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val deferredA = async(Dispatchers.IO) { A() }  // Multi-threaded!
        val deferredB = async (Dispatchers.IO) { B() }   // Multi-threaded!

        deferredA.await()
        deferredB.await()
    }

}

suspend fun A() {
    for (i in 1..5) {
        println("A $i")
        Thread.sleep(100)
    }
}


suspend fun B() {
    for (i in 1..5) {
        println("B $i")
        Thread.sleep(100)
    }
}
