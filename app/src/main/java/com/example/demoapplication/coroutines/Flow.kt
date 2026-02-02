package com.example.demoapplication.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

fun main() {
    runBlocking {
        val _sharedFlow = MutableSharedFlow<Int>(
            replay = 0,           // Keep last 2 values for new subscribers
            extraBufferCapacity = 10
        )
        val sharedFlow = _sharedFlow.asSharedFlow()

        launch {
            repeat(5) { i ->
                delay(1000)
                println("Emitting: $i")
                _sharedFlow.emit(i)
            }
        }

        delay(2500) // Let some emissions happen

        // Late subscriber still gets replay values
        sharedFlow.collect { value ->
            println("Late collector received: $value")
        }

    }
}


fun coldFlow(): Flow<Int> =
    flow {
        emit(45)
        delay(5000)
        emit(50)
    }