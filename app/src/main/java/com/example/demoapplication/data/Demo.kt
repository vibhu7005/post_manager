package com.example.demoapplication.data

import kotlinx.coroutines.*

// COROUTINE BUILDERS EXPLAINED

fun main() {
    val x:String? = null
    print(x?.length) //safe call
    print(x ?: "Default Value") //elvis operator
    println(x!!) //non
}