package com.example.demoapplication.data

import kotlin.reflect.KProperty

fun main() {
    val result : Result<Person> = Result.Error("dfdfdf")

    val person = Person.Student("fdf")
    processPerson(person)
}

fun processPerson(person: Person) {
    //exaustive
    when (person) {
        is Person.Student -> println("Student Name: ${person.name}")
        is Person.Teacher -> println("Teacher Name: ${person.name}")
    }
}



sealed interface Person {
    class Student(val name: String) : Person
    class Teacher(val name: String) : Person
}




sealed class Result<out T> {
    class Success<T>(val data: T) : Result<T>()
    class Error(val message: String) : Result<Nothing>()
}