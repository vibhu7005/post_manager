package com.example.demoapplication.data.general

import java.util.Collections

class Money(val x : Int, val currency: String = "USD"): Comparable<Money> {
    override fun compareTo(other: Money): Int {
        return x.compareTo(other.x)
    }
}

fun main() {
    val RajMoney = Money(100)
    val RamMoney = Money(20)
    if (RajMoney < RamMoney) {
        println("Raj has less money than Ram")
    } else {
        println("Raj has more or equal money than Ram")
    }
    val listMoney = listOf(RajMoney, RamMoney)
    val sortedList = Collections.sort(listMoney)
}