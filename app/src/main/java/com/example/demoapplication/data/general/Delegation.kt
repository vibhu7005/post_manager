package com.example.demoapplication.data.general



// ============================================================================
// OPTION 1: Use Standard Delegates (Delegates - plural, with 's')
// ============================================================================

interface HuntingBehaviour {
    fun hunt()
}

interface EatingBehaviour {
    fun eat()
}

class Lion : HuntingBehaviour, EatingBehaviour {
    override fun hunt() {
        println("Lion is hunting")
    }

    override fun eat() {
        println("Lion is eating")
    }
}

class Cat : EatingBehaviour {
    override fun eat() {
        println("Cat is eating")
    }
}

class AnimalManager(hunting : HuntingBehaviour, eating : EatingBehaviour) : HuntingBehaviour by hunting,
    EatingBehaviour by eating


fun main() {
//    val manager = AnimalManager(Lion(), Cat())
//    manager.hunt()
//    manager.eat()
}

