package com.example.demoapplication.data.general


fun <T> List<T>.filter(condition: (T) -> Boolean): List<T> {
    val list = mutableListOf<T>()
    for (item in this) {
        if (condition(item)) list.add(item)
    }
    return list
}

fun getOperation(name: String): (Int, Int) -> Int {
    return when (name) {
        "add" -> { a, b -> a + b }
        "subtract" -> { a, b -> a - b }
        else -> { x, y -> 0 }
    }
}

fun multiply(): (Int) -> (Int) -> Int {
    return { a -> { b -> a * b } }
}

inline fun evaluate(numA: Int, numB: Int, operation: (Int, Int) -> Int) = operation(numA, numB)

fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val evenNumbers = list.filter { a -> a % 2 == 0 }
    println(evenNumbers)
    println(evaluate(3, 4, { a, b -> a + b }))
    val x = multiply()
    val z = x(4)
    println(z(3))

    val lambda = { a: Int -> a * a }
    println(operate(3, 4, { a, b -> a + b }))

    println(operate(4,7, ::add))
}

fun add(a: Int, b: Int): Int {
    return a + b
}

fun operate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}