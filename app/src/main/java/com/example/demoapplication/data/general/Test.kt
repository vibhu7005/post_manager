package com.example.demoapplication.data.general

import kotlin.properties.Delegates

class Delegate(var value : Any?) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Any? {
        if ((value as Int) < 5) return  "Value is less than 5"
        return value
    }
}

fun main () {
    val x by Delegate(4)
    var name by Delegates.notNull<Int>()
    name = 4
    println(name)
}