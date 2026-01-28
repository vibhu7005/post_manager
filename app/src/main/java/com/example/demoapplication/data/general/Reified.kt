package com.example.demoapplication.data.general

class Reified {

     inline fun <reified T>getTypeName(param : Any) {
        if (param is T) {
            println("valid")
        } else {
            println("not valid")
        }
    }
}

fun main() {
    val reified = Reified()
    val typeName1 = reified.getTypeName<String>("Hello")
    val typeName2 = reified.getTypeName<Int>(123)
    val typeName3 = reified.getTypeName<Double>(45.67)
    println("Type of 'Hello' is: $typeName1")
    println("Type of 123 is: $typeName2")
    println("Type of 45.67 is: $typeName3")
}