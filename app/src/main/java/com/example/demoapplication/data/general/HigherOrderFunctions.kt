package com.example.demoapplication.data.general


   fun<T> List<T>.filter(condition : (T) -> Boolean) : List<T> {
       val list = mutableListOf<T>()
       for (item in this) {
           if (condition(item)) list.add(item)
       }
       return list
    }

    inline fun evaluate(numA : Int, numB: Int, operation : (Int, Int) -> Int) = operation(numA, numB)

fun main() {
    val list = listOf(1,2,3,4,5,6,7,8,9)
    val evenNumbers = list.filter {a -> a % 2 == 0 }
    println(evenNumbers)
    println(evaluate(3,4,  {a, b -> a + b}))
}