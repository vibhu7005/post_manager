// I.A - Kotlin Fundamentals (30 MCQs) - Senior Developer Interview Prep
// Core Topics with Advanced Examples & Edge Cases

// 1. VAL vs VAR - Immutability Concepts
fun valVsVarExamples() {
    val immutableRef = mutableListOf<String>() // Reference immutable, content mutable
    immutableRef.add("item") // ✓ Works
    // immutableRef = mutableListOf() // ✗ Compilation error
    
    var mutableRef = listOf("a", "b") // Reference mutable, content immutable
    mutableRef = listOf("c", "d") // ✓ Works
    // mutableRef.add("e") // ✗ No add method on immutable list
    
    // Advanced: val with custom getter (computed property)
    val currentTimestamp: Long
        get() = System.currentTimeMillis() // Recalculated on each access
}


// 2. DATA TYPES & TYPE INFERENCE
fun dataTypesAndInference() {
    // Primitive types are objects in Kotlin
    val byte: Byte = 127
    val short: Short = 32767
    val int = 42 // Type inferred as Int
    val long = 42L // Type inferred as Long
    val float = 3.14f // Type inferred as Float
    val double = 3.14 // Type inferred as Double
    
    // Type inference limitations
    val list = listOf(1, 2, 3) // Inferred as List<Int>
    // val emptyList = listOf() // ✗ Cannot infer type
    val emptyList = listOf<String>() // ✓ Explicit type needed
    
    // Underscores in numeric literals
    val million = 1_000_000
    val binary = 0b11010010_01101001_10010100_10010010
    val hex = 0xFF_EC_DE_5E
}

// 3. NULL SAFETY - Advanced Patterns
fun nullSafetyAdvanced() {
    var nullableString: String? = "Hello"
    
    // Safe call operator
    println(nullableString?.length) // Prints 5
    nullableString = null
    println(nullableString?.length) // Prints null
    
    // Elvis operator with early return
    fun processString(input: String?): String {
        val processed = input?.trim() ?: return "Empty input"
        return processed.uppercase()
    }
    
    // Safe call chaining
    data class User(val profile: Profile?)
    data class Profile(val address: Address?)
    data class Address(val city: String?)
    
    val user: User? = User(Profile(Address("NYC")))
    val city = user?.profile?.address?.city ?: "Unknown"
    
    // Not-null assertion (use carefully!)
    val definitelyNotNull = nullableString!! // Throws KotlinNullPointerException if null
    
    // Safe cast
    val stringValue: Any = "Hello"
    val safeString = stringValue as? String // Returns String? (null if cast fails)
    val unsafeString = stringValue as String // Throws ClassCastException if cast fails
}

// 4. CONTROL FLOW - Advanced When & Loops
fun controlFlowAdvanced() {
    // When as expression
    fun describe(obj: Any) = when (obj) {
        1 -> "One"
        "Hello" -> "Greeting"
        is Long -> "Long number"
        in 1..10 -> "Small number"
        !is String -> "Not a string"
        else -> "Unknown"
    }
    
    // When without argument
    fun validateAge(age: Int) = when {
        age < 0 -> throw IllegalArgumentException("Age cannot be negative")
        age < 18 -> "Minor"
        age < 65 -> "Adult"
        else -> "Senior"
    }
    
    // Advanced for loops
    val items = listOf("a", "b", "c")
    for ((index, value) in items.withIndex()) {
        println("$index: $value")
    }
    
    // Ranges and progressions
    for (i in 1..5) println(i) // 1, 2, 3, 4, 5
    for (i in 1 until 5) println(i) // 1, 2, 3, 4
    for (i in 5 downTo 1) println(i) // 5, 4, 3, 2, 1
    for (i in 1..10 step 2) println(i) // 1, 3, 5, 7, 9
    
    // Labels and breaks
    outer@ for (i in 1..3) {
        for (j in 1..3) {
            if (i == 2 && j == 2) break@outer
            println("$i, $j")
        }
    }
}

// 5. FUNCTIONS - Advanced Features
class FunctionExamples {
    // Named arguments and default parameters
    fun createUser(
        name: String,
        email: String = "",
        age: Int = 0,
        isActive: Boolean = true
    ): User = User(name, email, age, isActive)
    
    // Single-expression functions
    fun double(x: Int): Int = x * 2
    fun isEven(n: Int) = n % 2 == 0 // Return type inferred
    
    // Function with receiver (extension function)
    fun String.addPrefix(prefix: String) = "$prefix$this"
    
    // Higher-order functions
    fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
        val result = mutableListOf<T>()
        for (item in this) {
            if (predicate(item)) result.add(item)
        }
        return result
    }
    
    // Vararg parameters
    fun sum(vararg numbers: Int): Int = numbers.sum()
    fun callSum() {
        sum(1, 2, 3, 4) // Direct arguments
        val array = intArrayOf(1, 2, 3, 4)
        sum(*array) // Spread operator
    }

   // 1, 2, 3, 4, 5
    
    // Infix functions
    infix fun Int.times(str: String) = str.repeat(this)
    fun useInfix() {
        val result = 3 times "Hello " // Same as 3.times("Hello ")
    }
}

// 6. CLASSES AND OBJECTS - Advanced Concepts
data class User(val name: String, val email: String, val age: Int, val isActive: Boolean)

class UserManager {
    // Primary constructor with property declarations
    class Person(
        val firstName: String,
        val lastName: String,
        var age: Int = 0
    ) {
        // Secondary constructor
        constructor(fullName: String) : this(
            firstName = fullName.split(" ").first(),
            lastName = fullName.split(" ").last()
        )
        
        // Custom getter and setter
        val fullName: String
            get() = "$firstName $lastName"
            
        var email: String = ""
            set(value) {
                require(value.contains("@")) { "Invalid email" }
                field = value // 'field' refers to backing field
            }
            get() = field.lowercase()
        
        // Init block
        init {
            require(firstName.isNotBlank()) { "First name cannot be blank" }
        }
    }
    
    // Object declaration (Singleton)
    object DatabaseConfig {
        const val URL = "jdbc:postgresql://localhost/db"
        fun connect() = println("Connecting to $URL")
    }
    
    // Companion object
    companion object {
        private const val MAX_USERS = 1000
        
        @JvmStatic // For Java interop
        fun createDefault() = UserManager()
    }
}

// 7. INHERITANCE AND INTERFACES
// Open class (can be inherited)
open class Animal(val name: String) {
    open fun makeSound() = "Some sound"
    
    // Final method (cannot be overridden)
    final fun sleep() = println("$name is sleeping")
}

// Abstract class
abstract class Mammal(name: String) : Animal(name) {
    abstract fun giveBirth()
    
    // Abstract classes can have concrete methods
    fun breathe() = println("Breathing")
}

// Interface with default implementation
interface Flyable {
    val wingSpan: Double
    
    fun fly() = println("Flying with wingspan $wingSpan")
    
    // Abstract property
    val maxSpeed: Int
}

// Multiple inheritance from interface
class Bird(name: String, override val wingSpan: Double) : Animal(name), Flyable {
    override val maxSpeed: Int = 50
    
    override fun makeSound() = "Chirp"
    
    // Can override default implementation
    override fun fly() {
        println("$name is flying gracefully")
        super.fly() // Call default implementation
    }
}

// 8. VISIBILITY MODIFIERS
class VisibilityExample {
    public val publicProperty = "Visible everywhere" // Default is public
    private val privateProperty = "Only visible in this class"
    protected val protectedProperty = "Visible in subclasses"
    internal val internalProperty = "Visible in same module"
    
    private fun privateFunction() = "Private function"
    
    class NestedClass {
        // Cannot access outer class private members
        fun access() {
            // println(privateProperty) // ✗ Compilation error
        }
    }
    
    inner class InnerClass {
        // Can access outer class private members
        fun access() {
            println(privateProperty) // ✓ Works
        }
    }
}

// 9. STRING INTERPOLATION - Advanced
fun stringInterpolationAdvanced() {
    val name = "Alice"
    val age = 30
    val balance = 1234.56
    
    // Basic interpolation
    val basic = "Hello, $name!"
    
    // Expression interpolation
    val expression = "Next year $name will be ${age + 1}"
    
    // Function calls in interpolation
    val function = "Uppercase name: ${name.uppercase()}"
    
    // Complex expressions
    val complex = "Balance: ${if (balance > 1000) "High" else "Low"}"
    
    // Multi-line strings with interpolation
    val multiline = """
        Name: $name
        Age: $age
        Status: ${if (age >= 18) "Adult" else "Minor"}
    """.trimIndent()
    
    // Raw strings (no escaping needed)
    val regex = """[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"""
    val path = """C:\Users\$name\Documents"""
}

// SENIOR-LEVEL EDGE CASES & INSIGHTS

// 1. Type Erasure and Reified
inline fun <reified T> isInstanceOf(obj: Any): Boolean = obj is T

// 2. Platform Types (Java interop)
// String! means nullable unknown from Java

// 3. Nothing type
fun fail(message: String): Nothing = throw IllegalArgumentException(message)

// 4. Unit vs Void
fun returnsUnit(): Unit { } // Unit is a singleton object
fun returnsVoid() { } // Implicitly returns Unit

// 5. Backing Properties
class BackingPropertyExample {
    private var _items = mutableListOf<String>()
    val items: List<String>
        get() = _items
}