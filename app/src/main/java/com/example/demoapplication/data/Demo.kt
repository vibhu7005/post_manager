package com.example.demoapplication.data

import kotlinx.coroutines.delay
import kotlin.reflect.typeOf

class Demo {
    val message = System.currentTimeMillis()
        get() = field + 1
}

suspend fun main() {
    val demo = Demo()
    println(demo.message)
    delay(4000)
    println(demo.message)
    val short: Boolean? = null
    println(short?.javaClass)

    fun dataTypesAndInference() {
        // Primitive types are objects in Kotlin
        val byte: Byte = 127
        val short: Short = 32767
        val int = 42 // Type inferred as Int
        val long = 42L // Type inferred as Long
        val float = 3.14f // Type inferred as Float
        val double = 3.14 // Type inferred as Double

        // Type inference limitations
        val list = listOf(1, 2, 3) // Inferred as List<Int>
        // val emptyList = listOf() // ✗ Cannot infer type
        val emptyList = listOf<String>() // ✓ Explicit type needed

        // Underscores in numeric literals
        val million = 1_000_000
        val binary = 0b11010010_01101001_10010100_10010010
        val hex = 0xFF_EC_DE_5E
    }
}

