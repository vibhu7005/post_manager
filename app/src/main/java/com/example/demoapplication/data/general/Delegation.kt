package com.example.demoapplication.data.general

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// ============================================================================
// OPTION 1: Use Standard Delegates (Delegates - plural, with 's')
// ============================================================================

class Person {
    // ✅ CORRECT: Use Delegates (plural)
    var value: String by Delegates.notNull()
    
    // Other standard delegates:
    var name: String by Delegates.observable("") { prop, old, new ->
        println("$prop changed from $old to $new")
    }
    
    var age: Int by Delegates.vetoable(0) { _, old, new ->
        new >= 0  // Only allow non-negative
    }
}

// ============================================================================
// OPTION 2: Create Custom Delegate Class
// ============================================================================

class CustomDelegate(private var value: String = "") : ReadWriteProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        this.value = value
    }
}

class PersonWithCustomDelegate {
    // ✅ CORRECT: Use custom delegate class
    var value: String by CustomDelegate()
}

// ============================================================================
// COMMON FIXES FOR "Unresolved reference 'Delegate'"
// ============================================================================

/*
 * ERROR: Unresolved reference 'Delegate'
 * 
 * FIX 1: Use Delegates (plural) for standard delegates
 * 
 * WRONG:
 * var value: String by Delegate()  // ❌ Delegate (singular)
 * 
 * CORRECT:
 * import kotlin.properties.Delegates
 * var value: String by Delegates.notNull<String>()  // ✅ Delegates (plural)
 * 
 * ---
 * 
 * FIX 2: Create custom Delegate class
 * 
 * import kotlin.properties.ReadWriteProperty
 * import kotlin.reflect.KProperty
 * 
 * class Delegate<T> : ReadWriteProperty<Any?, T> {
 *     override fun getValue(...): T { ... }
 *     override fun setValue(...) { ... }
 * }
 * 
 * var value: String by Delegate()  // ✅ Now works
 */