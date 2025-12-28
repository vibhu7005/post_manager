package com.example.demoapplication.data

import kotlinx.coroutines.delay
import kotlin.reflect.typeOf

class Demo {
    val message = System.currentTimeMillis()
        get() = field + 1
}

fun main() {
    val list = mutableListOf(1, 2, 3, 4, 5, 6)
    list.customFilter { it % 2 == 0 }
    println(list)
    "super" concat "man"

    println(Car("orange").cyclinders)
    val s = 4;
//    println(s.isInstanceOf(Int))

}


inline fun<reified T> isInstanceOf(obj : Any) : Boolean = obj is T


fun<T> isInstanceOf (obj : T) : Boolean {
    return obj is String
}


fun addaa(op: (a: Int, b: Int) -> Int): Int {
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
    val map = {1 to 2; 3 to 4; 5 to 4}

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

    abstract val color : String
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






