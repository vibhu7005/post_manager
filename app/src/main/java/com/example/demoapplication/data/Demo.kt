package com.example.demoapplication.data

import androidx.compose.runtime.structuralEqualityPolicy
import kotlinx.coroutines.*
import kotlinx.serialization.descriptors.PrimitiveKind

// COROUTINE BUILDERS EXPLAINED

fun main() {
    val res = Result<String>()

}


sealed class Result<T> {
    class Success<T> : Result<T>()
    class Error : Result<Nothing>()
}

class Car private constructor(
    val color : String,
    val config: String,
    val type : String
) {
    class Builder {
        var color : String = ""
        var config: String = ""
        var type : String = ""

        fun color(color: String) : Builder {
            require(value = color.isNotBlank())
            this.color = color
            return this
        }

        fun config(config: String) : Builder {
            this.config = config
            return this
        }

        fun type(type: String) : Builder {
            this.type = type
            return this
        }

        fun build() : Car {
            return Car(color, config, type)
        }
    }
}


class Animal
class Student private constructor() {
    var instance: Student? = null
    fun getInstance(): Student {
        return instance ?: synchronized(this) {
            instance ?: Student()
        }
    }


    lateinit var fg: String
    lateinit var sd: Animal
    var marks: Int = 0
        set(value) {
            require(value > 100) { "Marks should not be greater than 100" }
            field = value
        }

    var interestRate: Double = 0.0
        private set
    var isPassed: Boolean = marks > 33

    companion object


}