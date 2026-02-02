package com.example.demoapplication.coroutines


import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

fun main() {
    runBlocking {
        val flow : MutableSharedFlow<Int> = MutableSharedFlow()
        flow.emit(45)
        flow.emit(79)

        launch {
            for (i in 1..10) {
                delay(100)
                flow.emit(i)
            }
        }



    }
}