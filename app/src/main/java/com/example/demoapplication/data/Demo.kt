package com.example.demoapplication.data

import kotlin.reflect.KProperty

fun main() {


}


//builder pattern
class Person private constructor(val name: String, val email: String, val age: Int) {

    class Builder(val name: String, val age: Int) {
        private var email: String = ""

        init {
            // Validate required fields immediately
            require(name.isNotBlank()) { "Name cannot be blank" }
            require(age > 0) { "Age must be positive" }
        }

        fun setEmail(email: String) = apply { 
            require(email.isNotBlank()) { "Email cannot be blank" }
            require(email.contains("@")) { "Invalid email format" }
            this.email = email 
        }

        fun build(): Person {
            require(email.isNotBlank()) { "Email is required" }
            return Person(name, email, age)
        }
    }
    
    override fun toString(): String = "Person(name=$name, email=$email, age=$age)"
}



