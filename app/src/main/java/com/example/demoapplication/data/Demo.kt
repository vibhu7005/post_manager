package com.example.demoapplication.data

import androidx.compose.runtime.structuralEqualityPolicy
import kotlinx.coroutines.*
import kotlinx.serialization.descriptors.PrimitiveKind

// COROUTINE BUILDERS EXPLAINED

fun main() {
    val res = Result.Success<Person>(Person(23))
    val res1 = Result.Error("Error occurred")

    println(res1.errorMsg)
    println(res.data.age)
    val hulk = Collection(Hulk(56))

    val organism : Collection<Organism> = hulk
    organism.item.strength = 45

}

class Hulk(override var strength: Int) : Organism(strength)

open class Organism(open var strength: Int)

class Collection<out T>(val item: T)

class Person(val age: Int)

 sealed class Result<out T> {
    class Success<T>(var data : T) : Result<T>()
    class Error(val errorMsg : String) : Result<Nothing>()
}




fun fetchPerson(dataParam : String) : Result<Person> {
    if (dataParam == "SUCCESS") {
        return Result.Success(Person(34))
    } else {
        return Result.Error("Failed to fetch person data")
    }
}


enum class Demo {
    ONE,
    TWO,
    THREE
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