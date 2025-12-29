package com.example.demoapplication.data

import kotlinx.coroutines.delay
import kotlin.reflect.typeOf

class Demo {
    val x = arrayOf(1,2)
    val f : Int? = null
    val message = System.currentTimeMillis()
        get() = field + 1
}

fun main() {
//    val list = mutableListOf(1, 2, 3, 4, 5, 6)
//    list.customFilter { it % 2 == 0 }
//    println(list)
//    "super" concat "man"
//
//    println(Car("orange").cyclinders)
//    val s = 4;
////    println(s.isInstanceOf(Int))
//
//    // Reified example
//    println(checkType<String>("hello"))  // true
//    println(checkType<String>(42))       // false
//    println(checkType<Int>(42))          // true
    
    // Thread safety demo
    println("\n--- Thread Safety Example ---")
    threadSafetyExample()
}

inline fun <reified T> emptyListOf(): Array<T> {
    return emptyArray<T>()
}


inline fun <reified T> checkType(obj: Any): Boolean = obj is T


fun <T> isInstanceOf(obj: T): Boolean {
    return obj is String
}


inline fun addaa(op: (a: Int, b: Int) -> Int): Int {
    return op(5, 6)
}

fun <T> MutableList<T>.customFilter(predicate: (T) -> Boolean) {
    var index = 0
    while (index in 0..this.size - 1) {
        if (predicate(this[index])) {
            removeAt(index)
        } else {
            index++;
        }
    }
}


infix fun String.concat(string: String): String {
    val map = { 1 to 2; 3 to 4; 5 to 4 }

    return this + string
}


open class Animal {
    open fun walk() {
        println("Animal is walking")
    }
}


class Dog : Animal() {
    override fun walk() {
        println("Dog is walking")
    }
}

class Car(override val color: String) : Vehicle() {
    internal val tyres = 4
    override fun horsepower() {
        println("Car horsepower is 150")
    }
}

abstract class Vehicle {
    val cyclinders = 2

    abstract val color: String
    abstract fun horsepower()

    fun startEngine() {
        println("Engine started")
    }
}

interface VehicleInerface {
    fun horsepower()
    fun startEngine() {
        println("Engine started from interface")
    }
}

// Thread safety example
class SharedCounter {
    val list: MutableList<Int> by lazy {mutableListOf()}  // val but NOT thread safe!
    var count: Int = 0  // var - definitely not thread safe
}

fun threadSafetyExample() {
    val sharedObject = SharedCounter()

    // Thread 1
    Thread {
        repeat(10000000) {
            sharedObject.list.add(it)  // Race condition!
            sharedObject.count++       // Race condition!
        }
        println("Thread 1 finished")
    }.start()
    
    // Thread 2  
    Thread {
       repeat(10000000) {
            sharedObject.list.add(it)  // Race condition!
            sharedObject.count++       // Race condition!
        }
        println("Thread 2 finished")
    }.start()
    
    // Wait and check results
    Thread.sleep(2000)
    println("List size: ${sharedObject.list.size}")  // Should be 2000, but might be less
    println("Count: ${sharedObject.count}")           // Should be 2000, but might be less
}


