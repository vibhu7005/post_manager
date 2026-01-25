package com.example.demoapplication.data.general



// ============================================================================
// OPTION 1: Use Standard Delegates (Delegates - plural, with 's')
// ============================================================================

interface HuntingAnimal {
    fun hunt()
}

interface HuntingBehaviour {
    fun doHunt()
}

class Tiger : HuntingAnimal {
    private val huntingBehaviour = object : HuntingBehaviour {
        override fun doHunt() {
            println("Tiger is hunting")
        }

    }
    override fun hunt() {
        huntingBehaviour.doHunt()
    }
}


class TigerHunterMananger(val tiger : Tiger) : HuntingAnimal by tiger {
}

class Cat : Animal() {

}


open class Animal {
}




fun main() {
//    val manager = AnimalManager(Lion(), Cat())
//    manager.hunt()
//    manager.eat()
}

