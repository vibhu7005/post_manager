package com.example.demoapplication.data.general

import android.R

class Matrix<T>(val x: Int, val y: Int) {
    val list:MutableList<MutableList<T>> = mutableListOf<MutableList<T>>()


    init {
        for (i in 0 until x) {
            val row: MutableList<T> = mutableListOf()
            for (j in 0 until y) {

                row.add(null as T)
            }
            list.add(row)
        }
    }


    operator fun get(i: Int, j: Int): T {
        return list[i][j]
    }

    operator fun set(i: Int, j: Int, value: T) {
        list[i][j] = value

    }

    operator fun plus(value: MutableList<T>) {
        list.add(value)
    }

    operator fun invoke(i: Int, j: Int): T {
        return list[i][j]
    }
}


fun main() {
    val matrix = Matrix<Int>(3,4)
    matrix + mutableListOf(12,3,4)

    matrix[0,0] = 10
}