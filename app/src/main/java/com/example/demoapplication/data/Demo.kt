package com.example.demoapplication.data

import kotlin.reflect.KProperty

// COROUTINE BUILDERS EXPLAINED

fun main() {
//    println("=== Using getValue and setValue (Delegated Properties) ===")
    println("Adi".lambai())
    
//    // Using 'by' keyword - getValue is called when reading
//    var box by Box(10)  // Note: must be 'var' to use setValue
//    println("Initial value: $box")  // Calls getValue() -> prints: 10
//
//    // setValue is called when assigning
//    box = 20  // Calls setValue() -> updates internal value
//    println("After assignment: $box")  // Calls getValue() -> prints: 20
//
//    box = 30
//    println("After another assignment: $box")  // Prints: 30
//
//    println("\n=== Using plus operator ===")
//
//    // Using the + operator - calls plus() function
//    val box1 = Box(5)
//    val box2 = box1 + 10  // Calls box1.plus(10)
//    println("box1.value = ${box1.value}")  // Still 5 (immutable operation)
//    println("box2.value = ${box2.value}")  // 15 (new Box instance)
//
//    val box3 = Box(100) + 50 + 25  // Can chain operations
//    println("box3.value = ${box3.value}")  // 175
//
//    println("\n=== Combining both ===")
//    var mutableBox by Box(1)
//    println("mutableBox = $mutableBox")  // 1
//
//    // You can't directly use + on delegated property, but you can do:
//    val newBox = Box(mutableBox) + 5
//    println("newBox.value = ${newBox.value}")  // 6
//
//    // Or update the delegated property
//    mutableBox = mutableBox + 5  // Reads (getValue), adds, then writes (setValue)
//    println("mutableBox after += 5: $mutableBox")  // 6
}

class DemoClass {
    val name by SimpleLazy<String> { "Demo" }
}

class Box(var value: Int) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Int) {
        value = newValue
    }

    operator fun plus(other: Int): Box {
        return Box(this.value + other)
    }
}


fun String.lambai() : Int {
    return this.length
}

class SimpleLazy<T>(val initializer: () -> T) {
    @Volatile
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        // thisRef: The object instance where the property is accessed
        //          - null if accessed from top-level (like in main())
        //          - The object instance if accessed from a class (like DemoClass)
        // property: Contains metadata about the property
        //           - property.name: "x", "name", etc.
        //           - property.returnType: The type of the property
        
        // For lazy initialization, we don't actually need these parameters
        // but they're required by Kotlin's delegation protocol
        
        if (value == null) {
            synchronized(this) {
                if (value == null) {
                    value = initializer.invoke()
                }
            }
        }
        return value!!
    }
}




