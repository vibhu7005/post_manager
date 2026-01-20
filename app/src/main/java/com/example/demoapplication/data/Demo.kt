package com.example.demoapplication.data

import kotlin.reflect.KProperty

fun main() {
    val result : Result<Person> = Result.Error("dfdfdf")
}


class Person
sealed class Result<out T> {
    class Success <T>(val data : T) : Result<T>()
    class Error (val message : String) : Result<Nothing>()
}