package com.example.demoapplication.data.general

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock



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

// ============================================================================
// WHY PROPERTY DELEGATION IS POWERFUL
// ============================================================================
// 
// Your simple Delegate example doesn't show the real benefit because:
// - It just returns a constant value (not useful)
// - It doesn't add any cross-cutting behavior
//
// REAL BENEFITS of delegation vs simple getters/setters:
// 1. REUSABILITY: Write logic once, use for many properties
// 2. SEPARATION: Business logic separated from class definition
// 3. CROSS-CUTTING: Add logging, validation, caching, etc. automatically
// 4. COMPOSABILITY: Mix and match behaviors (e.g., Logging + Validation)
// 5. LESS BOILERPLATE: No need to write getters/setters for each property
//
// ============================================================================
// SIMPLE DELEGATE (Your current example - not very useful)
// ============================================================================
class Delegate {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String {
        return "Delegated Value"
    }

    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: String) {
        println("Value set to $value")
    }
}

class Example {
    var value: String by Delegate()
}

// ============================================================================
// REAL-WORLD BENEFITS OF PROPERTY DELEGATION
// ============================================================================

// BENEFIT 1: REUSABILITY - Same delegate logic for multiple properties
class LoggingDelegate<T>(private var value: T, private val propertyName: String) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
        println("📖 Reading $propertyName: $value")
        return value
    }

    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, newValue: T) {
        println("✏️ Writing $propertyName: $value -> $newValue")
        value = newValue
    }
}

class User {
    // Reuse the same logging logic for multiple properties!
    var name: String by LoggingDelegate("", "name")
    var email: String by LoggingDelegate("", "email")
    var age: Int by LoggingDelegate(0, "age")
    
    // Without delegation, you'd need to write getters/setters for EACH property:
    // private var _name = ""
    // var name: String
    //     get() { println("Reading name: $_name"); return _name }
    //     set(value) { println("Writing name: $_name -> $value"); _name = value }
    // ... repeat for email, age, etc. - lots of boilerplate!
}

// BENEFIT 2: VALIDATION - Enforce business rules automatically
class ValidatedStringDelegate(
    private var value: String,
    private val minLength: Int = 0,
    private val maxLength: Int = Int.MAX_VALUE
) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String {
        return value
    }

    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, newValue: String) {
        require(newValue.length >= minLength) {
            "$property must be at least $minLength characters"
        }
        require(newValue.length <= maxLength) {
            "$property must be at most $maxLength characters"
        }
        value = newValue
    }
}

class Product {
    // Automatic validation for every assignment!
    var name: String by ValidatedStringDelegate("", minLength = 3, maxLength = 50)
    var sku: String by ValidatedStringDelegate("", minLength = 5, maxLength = 20)
    
    // Without delegation: repetitive validation code in each setter
}

// BENEFIT 3: CACHING & PERFORMANCE - Expensive operations cached
class CachedDelegate<T>(
    private val initializer: () -> T
) {
    private var cachedValue: T? = null
    private var isInitialized = false

    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
        if (!isInitialized) {
            println("💾 Computing expensive value for ${property.name}...")
            cachedValue = initializer()
            isInitialized = true
        }
        return cachedValue!!
    }
}

class DataProcessor {
    // Expensive computation happens only once, then cached
    val expensiveResult: String by CachedDelegate {
        // Simulate expensive operation
        Thread.sleep(1000)
        "Computed Result"
    }
}

// BENEFIT 4: OBSERVABLE PATTERN - React to changes automatically
class ObservableDelegate<T>(
    private var value: T,
    private val onChange: (oldValue: T, newValue: T) -> Unit
) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, newValue: T) {
        val oldValue = value
        value = newValue
        onChange(oldValue, newValue)
    }


// BENEFIT 5: THREAD-SAFETY - Automatic synchronization

    class ThreadSafeDelegate<T>(private var value: T) {
        private val lock = ReentrantLock()

        operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
            return lock.withLock { value }
        }

        operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, newValue: T) {
            lock.withLock { value = newValue }
        }
    }

    class SharedCounter {
        // Automatically thread-safe without writing synchronized blocks everywhere
        var count: Int by ThreadSafeDelegate(0)
    }

    // BENEFIT 6: FORMATTING/TRANSFORMATION - Automatic data transformation
    class UppercaseDelegate(private var value: String) {
        operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String {
            return value
        }

        operator fun setValue(
            thisRef: Any?,
            property: kotlin.reflect.KProperty<*>,
            newValue: String
        ) {
            value = newValue.uppercase().trim()
        }
    }

    class FormData {
        // Automatically converts to uppercase and trims whitespace
        var code: String by UppercaseDelegate("")
    }

    // BENEFIT 7: LAZY INITIALIZATION - Only compute when needed
    class LazyDelegate<T>(private val initializer: () -> T) {
        private var value: T? = null

        operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
            if (value == null) {
                value = initializer()
            }
            return value!!
        }
    }

    class DatabaseConnection {
        // Connection only created when first accessed
        val connection: String by LazyDelegate {
            println("🔌 Establishing database connection...")
            "Connected to DB"
        }
    }


    fun main() {
        println("=".repeat(60))
        println("1. SIMPLE DELEGATE (Not very useful)")
        println("=".repeat(60))
        val example = Example()
        println(example.value) // Calls getValue()
        example.value = "New Value" // Calls setValue()
        println(example.value) // Calls getValue() again

        println("\n" + "=".repeat(60))
        println("2. REUSABILITY - Same logic for multiple properties")
        println("=".repeat(60))
        val user = User()
        user.name = "John"
        user.email = "john@example.com"
        user.age = 30
        println("User: ${user.name}, ${user.email}, ${user.age}")

        println("\n" + "=".repeat(60))
        println("3. VALIDATION - Automatic business rule enforcement")
        println("=".repeat(60))
        val product = Product()
        try {
            product.name = "AB" // Too short - will throw exception
        } catch (e: IllegalArgumentException) {
            println("❌ Validation error: ${e.message}")
        }
        product.name = "Valid Product Name"
        println("✅ Product name set: ${product.name}")

        println("\n" + "=".repeat(60))
        println("4. CACHING - Expensive operations cached")
        println("=".repeat(60))
        val processor = DataProcessor()
        println("First access (will compute):")
        val start1 = System.currentTimeMillis()
        println(processor.expensiveResult)
        println("Time: ${System.currentTimeMillis() - start1}ms")

        println("\nSecond access (cached, instant):")
        val start2 = System.currentTimeMillis()
        println(processor.expensiveResult)
        println("Time: ${System.currentTimeMillis() - start2}ms")

        println("\n" + "=".repeat(60))
        println("5. OBSERVABLE - React to changes automatically")
        println("=".repeat(60))
        println("\n" + "=".repeat(60))
        println("6. FORMATTING - Automatic data transformation")
        println("=".repeat(60))
        val form = FormData()
        form.code = "  hello world  "
        println("Code stored as: '${form.code}'") // Automatically uppercased and trimmed

        println("\n" + "=".repeat(60))
        println("7. LAZY INITIALIZATION - Only compute when needed")
        println("=".repeat(60))
        val db = DatabaseConnection()
        println("Database object created, but connection not yet established...")
        println("Accessing connection now:")
        println(db.connection)
    }
}

