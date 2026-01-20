package com.example.demoapplication.data

import kotlin.reflect.KProperty

fun main() {
    val result : Result<Person> = Result.Error("dfdfdf")
}

sealed interface Person {
    class Student(val name: String) : Person
    class Teacher(val name: String) : Person
}

sealed interface EmailValidation : Person {
    class Valid()
}



sealed class Result<out T> {
    class Success <T>(val data : T) : Result<T>()
    class Error (val message : String) : Result<Nothing>()
}