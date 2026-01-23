package com.example.demoapplication.data

import kotlin.reflect.KProperty

fun main() {
    val p = Person(34)
    val p2 = Person(12)
    
    println("=== USING PLUS OPERATOR ===")
    val total = p + p2
    println("Total strength: $total")  // Output: Total strength: 46
    
    println("\n=== USING INVOKE OPERATOR ===")
    // INVOKE - call object like function with parentheses ()
    val strength1 = p()  // Calls invoke() - returns 34
    println("Invoke result: $strength1")  // Output: Invoke result: 34
    
    println("\n=== USING GET OPERATOR ===")
    // GET - access using square brackets []
    val value0 = p[0]  // Calls get(0) - returns 34
    val value1 = p[1]  // Calls get(1) - returns 68
    val value2 = p[2]  // Calls get(2) - returns 102
    println("Get[0]: $value0")  // Output: Get[0]: 34
    println("Get[1]: $value1")  // Output: Get[1]: 68
    println("Get[2]: $value2")  // Output: Get[2]: 102
    
    // GET with String parameter
    val name = p["name"]      // Returns "Person"
    val strength = p["strength"]  // Returns 34
    println("Get[\"name\"]: $name")  // Output: Get["name"]: Person
    println("Get[\"strength\"]: $strength")  // Output: Get["strength"]: 34
    
    println("\n=== KEY DIFFERENCES ===")
    println("INVOKE uses PARENTHESES: p()")
    println("GET uses SQUARE BRACKETS: p[0] or p[\"name\"]")
    println("PLUS uses PLUS SIGN: p + p2")
    
    println("\n=== PRACTICAL GET OPERATOR EXAMPLES ===")
    demonstrateGetOperator()
    
    println("\n=== REQUIRE RETURN TYPE DEMONSTRATION ===")
    demonstrateRequireReturnType()
    
    println("\n=== HOW REQUIRE WORKS WITH RETURN TYPES ===")
    demonstrateRequireWithReturnTypes()
    
    println("\n=== PRACTICAL USES OF NOTHING TYPE ===")
    demonstrateNothingType()
    
    println("\n=== COMPARETO AND OVERRIDE ===")
    demonstrateCompareToOverride()
    
    println("\n=== COMPARABLE vs NO COMPARABLE ===")
    demonstrateComparableVsNoComparable()
    
    println("\n=== YOUR CODE TEST - Does it work? ===")
    testUserCode()
    
    println("\n=== BACKING PROPERTY PATTERN ===")
    demonstrateBackingPropertyPattern()
}

fun testUserCode() {
    println("=== Testing Your Exact Code ===")
    
    // Your exact code
    class Money(val x: Int, val currency: String = "USD") {
        operator fun compareTo(other: Money): Int {
            return x.compareTo(other.x)
        }
    }
    
    val RajMoney = Money(100)
    val RamMoney = Money(20)
    
    // ✅ YES! This DOES work!
    if (RajMoney < RamMoney) {
        println("Raj has less money than Ram")
    } else {
        println("Raj has more or equal money than Ram")
    }
    // Output: "Raj has more or equal money than Ram"
    
    println("\n=== CORRECTED UNDERSTANDING ===")
    println("✅ Comparison operators (<, >, <=, >=) WORK without Comparable!")
    println("✅ They only need operator fun compareTo()")
    println()
    println("❌ BUT sorted(), min(), max() require Comparable")
    
    val list = listOf(RamMoney, RajMoney, Money(50))
    // val sorted = list.sorted()  // ERROR: No Comparable implementation
    
    println("\n=== So When Do You Need Comparable? ===")
    println("You need Comparable IF you want to use:")
    println("1. list.sorted()")
    println("2. list.min()")
    println("3. list.max()")
    println("4. list.sortedDescending()")
    println()
    println("For just comparison operators (<, >, etc.), Comparable is OPTIONAL!")
    println("But it's still RECOMMENDED for:")
    println("- Type safety")
    println("- Standard convention")
    println("- Future-proofing (if you need sorted() later)")
}

fun demonstrateBackingPropertyPattern() {
    println("\n=== BACKING PROPERTY PATTERN ===")
    
    println("=== Why Use: private var _value + public var value? ===")
    
    // Example 1: Validation
    class User {
        private var _age: Int = 0
        var age: Int
            get() = _age
            set(newValue) {
                require(newValue >= 0) { "Age cannot be negative" }
                require(newValue <= 150) { "Age cannot exceed 150" }
                _age = newValue
            }
    }
    
    val user = User()
    user.age = 25
    println("Age: ${user.age}")  // 25
    
    try {
        user.age = -5  // ❌ Throws exception
    } catch (e: IllegalArgumentException) {
        println("Validation: ${e.message}")  // "Age cannot be negative"
    }
    
    println("\n=== Example 2: Read-Only Public API ===")
    
    class DataManager {
        private var _data: List<String> = emptyList()
        
        // Public can READ but not WRITE
        val data: List<String>
            get() = _data.toList()  // Return copy
        
        fun loadData() {
            _data = listOf("Item1", "Item2", "Item3")
        }
    }
    
    val manager = DataManager()
    manager.loadData()
    println("Data: ${manager.data}")  // Can read
    
    // manager.data.add("New")  // ❌ ERROR: data is read-only
    
    println("\n=== Example 3: Android ViewModel Pattern ===")
    
    class CounterViewModel {
        private var _count: Int = 0
        private val observers = mutableListOf<(Int) -> Unit>()
        
        val count: Int
            get() = _count
        
        fun increment() {
            _count++
            notifyObservers()
        }
        
        private fun notifyObservers() {
            observers.forEach { it(_count) }
        }
        
        fun observe(observer: (Int) -> Unit) {
            observers.add(observer)
            observer(_count)
        }
    }
    
    val viewModel = CounterViewModel()
    viewModel.observe { count ->
        println("Count changed to: $count")
    }
    
    viewModel.increment()  // Triggers observer
    viewModel.increment()  // Triggers observer
    
    println("\n=== KEY REASONS TO USE THIS PATTERN ===")
    println("1. ✅ VALIDATION - Check values before setting")
    println("2. ✅ READ-ONLY API - Public read, private write")
    println("3. ✅ OBSERVABILITY - Trigger callbacks on changes")
    println("4. ✅ LOGGING - Track value changes")
    println("5. ✅ ENCAPSULATION - Hide internal implementation")
    println("6. ✅ CONTROL - Full control over property access")
    println()
    println("Most common use: Android ViewModels for state management!")
}

fun demonstrateGetOperator() {
    // Example 1: Matrix with GET operator
    val matrix = Matrix(3, 3)
    matrix[0, 0] = 1
    matrix[0, 1] = 2
    matrix[1, 0] = 3
    matrix[1, 1] = 4
    
    println("Matrix[0,0] = ${matrix[0, 0]}")  // Output: 1
    println("Matrix[1,1] = ${matrix[1, 1]}")  // Output: 4
    
    // Example 2: Map-like access
    val student = StudentInfo("Alice", 85, 20)
    println("Student[\"name\"] = ${student["name"]}")   // Output: Alice
    println("Student[\"marks\"] = ${student["marks"]}") // Output: 85
    println("Student[\"grade\"] = ${student["grade"]}")  // Output: B
}

fun demonstrateRequireReturnType() {
    println("=== REQUIRE() Return Type ===")
    
    // require() returns Unit, NOT Nothing!
    val result: Unit = require(true)  // Returns Unit
    println("require(true) returns: ${result::class.simpleName}")  // Output: Unit
    println("require() return type is: Unit")
    
    // We can capture it (though usually we don't need to)
    val unitValue: Unit = require(5 > 0) { "This won't throw" }
    println("Captured Unit value: $unitValue")
    
    // requireNotNull() returns the non-null type (not Unit!)
    val nullableValue: Int? = 42
    val nonNullValue: Int = requireNotNull(nullableValue)  // Returns Int!
    println("requireNotNull() returns: ${nonNullValue::class.simpleName}")  // Output: Int
    println("requireNotNull() return type is: Int (the non-null type)")
    
    println("\nKey Points:")
    println("1. require() returns Unit (not Nothing!)")
    println("2. requireNotNull() returns the non-null type")
    println("3. Unit means 'no meaningful value' - function completes normally")
    println("4. Nothing means 'never returns' - always throws")
}

fun demonstrateRequireWithReturnTypes() {
    println("=== How require() Works in String-Returning Function ===")
    
    // Function returns String, but require() returns Unit
    fun getStringValue(value: Int): String {
        // require() returns Unit, but if condition is false, it throws
        require(value > 0) { "Value must be positive" }
        // If we reach here, require() returned Unit and execution continues
        
        // Function returns String
        return "Value is: $value"
    }
    
    // Case 1: require() condition is TRUE
    // - require() returns Unit
    // - Execution continues
    // - Function returns String
    val result1 = getStringValue(5)
    println("Case 1 (value > 0): $result1")  // Output: "Value is: 5"
    println("  → require() returned Unit, function returned String ✅")
    
    // Case 2: require() condition is FALSE
    // - require() throws IllegalArgumentException
    // - Execution STOPS immediately
    // - Function NEVER returns String (exception propagates)
    try {
        val result2 = getStringValue(-1)  // require() throws, function never returns
        println("This never executes")
    } catch (e: IllegalArgumentException) {
        println("Case 2 (value <= 0): Exception caught: ${e.message}")
        println("  → require() threw exception, function never returned String ✅")
        println("  → Return type doesn't matter when exception is thrown!")
    }
    
    println("\n=== Key Concept ===")
    println("When require() condition is TRUE:")
    println("  - require() returns Unit")
    println("  - Execution continues normally")
    println("  - Function can return its declared type (String)")
    println()
    println("When require() condition is FALSE:")
    println("  - require() throws IllegalArgumentException")
    println("  - Execution STOPS immediately")
    println("  - Function NEVER returns normally")
    println("  - Return type is IRRELEVANT (exception propagates up)")
    println("  - Exceptions are CONTROL FLOW, not return values!")
}

fun demonstrateNothingType() {
    println("=== 1. Functions That Always Throw ===")
    
    // error() returns Nothing
    fun getConfig(): String {
        return error("Config not found")  // Returns Nothing, never returns String
    }
    
    try {
        val config = getConfig()  // Never returns, always throws
    } catch (e: Exception) {
        println("Caught: ${e.message}")  // "Config not found"
    }
    
    println("\n=== 2. Exhaustive When Expressions (MOST IMPORTANT USE!) ===")
    
    // Using sealed class defined outside (with Nothing for error states)
    fun handleResult(result: ResultType<String>): String {
        return when (result) {
            is ResultType.Success -> result.data
            is ResultType.Error -> "Error: ${result.message}"
            is ResultType.Loading -> "Loading..."
            // ✅ No else needed! Compiler knows it's exhaustive because Error/Loading use Nothing
        }
    }
    
    val success = ResultType.Success("Data loaded")
    val error = ResultType.Error("Network failed")
    val loading = ResultType.Loading
    
    println(handleResult(success))   // "Data loaded"
    println(handleResult(error))      // "Error: Network failed"
    println(handleResult(loading))    // "Loading..."
    
    println("\n=== 3. Type-Safe Null Handling ===")
    
    fun <T> fail(message: String): T {
        throw IllegalStateException(message)
    }
    
    fun getValueOrFail(key: String): String {
        val value: String? = getValueFromCache(key)
        return value ?: fail("Value not found for key: $key")
        // fail() returns Nothing, which is compatible with String
        // ✅ Type-safe! No need for !! operator
    }
    
    try {
        val result = getValueOrFail("missing")  // Throws
    } catch (e: Exception) {
        println("Caught: ${e.message}")
    }
    
    println("\n=== 4. Empty Collections ===")
    
    // emptyList() returns List<Nothing>
    val empty: List<Nothing> = emptyList()
    
    fun processList(list: List<String>): Int = list.size
    
    val size = processList(emptyList())  // ✅ Works! Nothing is compatible
    println("Empty list size: $size")  // 0
    
    println("\n=== KEY TAKEAWAYS ===")
    println("1. Nothing = 'never returns normally'")
    println("2. Most important use: Exhaustive when expressions")
    println("3. Enables type safety - compiler ensures all cases handled")
    println("4. Used in: error(), TODO(), sealed class error states")
    println("5. Empty collections: emptyList<Nothing>()")
}

fun getValueFromCache(key: String): String? = null

// ============================================================================
// COMPARETO AND OVERRIDE EXPLANATION
// ============================================================================

fun demonstrateCompareToOverride() {
    println("\n=== COMPARETO: Why Override is Required ===")
    
    // ❌ WRONG: Without implementing Comparable
    class MoneyWrong(val amount: Int) {
        // This won't enable comparison operators!
        operator fun compareTo(other: MoneyWrong): Int {
            return amount.compareTo(other.amount)
        }
    }
    
    val m1Wrong = MoneyWrong(100)
    val m2Wrong = MoneyWrong(50)
    
    // ❌ This won't compile - comparison operators don't work!
    // val result = m1Wrong < m2Wrong  // Error!
    
    // ✅ This works (direct function call only)
    val comparison = m1Wrong.compareTo(m2Wrong)
    println("Direct compareTo call: $comparison")
    
    // ✅ CORRECT: Implementing Comparable interface
    class Money(val amount: Int) : Comparable<Money> {
        // ✅ MUST use 'override' - compareTo is from Comparable interface
        override operator fun compareTo(other: Money): Int {
            return amount.compareTo(other.amount)
        }
    }
    
    val m1 = Money(100)
    val m2 = Money(50)
    val m3 = Money(150)
    
    // ✅ Now comparison operators work!
    println("m1 > m2: ${m1 > m2}")      // true
    println("m1 < m2: ${m1 < m2}")      // false
    println("m1 >= m2: ${m1 >= m2}")    // true
    println("m1 <= m2: ${m1 <= m2}")    // false
    
    // ✅ Works with sorting
    val moneyList = listOf(m2, m1, m3)
    val sorted = moneyList.sorted()
    println("Sorted: ${sorted.map { it.amount }}")  // [50, 100, 150]
    
    println("\n=== Why Override is Required ===")
    println("1. compareTo() is defined in Comparable<T> interface")
    println("2. When implementing Comparable<Money>, you override compareTo()")
    println("3. Kotlin REQUIRES 'override' keyword for interface methods")
    println("4. Without Comparable, comparison operators (<, >, <=, >=) don't work")
    println("5. Only direct compareTo() call works without Comparable")
}

fun demonstrateComparableVsNoComparable() {
    println("=== WITHOUT Comparable (NOT RECOMMENDED) ===")
    
    // ❌ Without Comparable
    class MoneyWithoutComparable(val amount: Int) {
        operator fun compareTo(other: MoneyWithoutComparable): Int {
            return amount.compareTo(other.amount)
        }
    }
    
    val m1No = MoneyWithoutComparable(100)
    val m2No = MoneyWithoutComparable(50)
    
    // ✅ Direct compareTo() works
    val comparison = m1No.compareTo(m2No)
    println("Direct compareTo(): $comparison")
    
    // ❌ Comparison operators DON'T work!
    // val result = m1No < m2No  // ERROR: Unresolved reference
    
    // ❌ sorted() doesn't work!
    val listNo = listOf(m2No, m1No)
    // val sorted = listNo.sorted()  // ERROR: No Comparable implementation
    
    println("❌ Limited - only direct compareTo() works")
    
    println("\n=== WITH Comparable (RECOMMENDED) ✅ ===")
    
    // ✅ With Comparable
    class MoneyWithComparable(val amount: Int) : Comparable<MoneyWithComparable> {
        override operator fun compareTo(other: MoneyWithComparable): Int {
            return amount.compareTo(other.amount)
        }
    }
    
    val m1Yes = MoneyWithComparable(100)
    val m2Yes = MoneyWithComparable(50)
    val m3Yes = MoneyWithComparable(150)
    
    // ✅ Direct compareTo() works
    println("Direct compareTo(): ${m1Yes.compareTo(m2Yes)}")
    
    // ✅ Comparison operators WORK!
    println("m1 > m2: ${m1Yes > m2Yes}")      // true
    println("m1 < m2: ${m1Yes < m2Yes}")      // false
    println("m1 >= m2: ${m1Yes >= m2Yes}")    // true
    
    // ✅ sorted() works!
    val listYes = listOf(m2Yes, m1Yes, m3Yes)
    val sorted = listYes.sorted()
    println("Sorted: ${sorted.map { it.amount }}")  // [50, 100, 150]
    
    // ✅ min() and max() work!
    val min = listYes.min()
    val max = listYes.max()
    println("Min: ${min.amount}, Max: ${max.amount}")
    
    println("✅ Full functionality - all comparison features work!")
    
    println("\n=== COMPARISON TABLE ===")
    println("┌─────────────────────┬──────────────┬──────────────┐")
    println("│ Feature              │ No Comparable│ With Comparable│")
    println("├─────────────────────┼──────────────┼──────────────┤")
    println("│ compareTo() call     │      ✅      │      ✅      │")
    println("│ <, >, <=, >= ops    │      ❌      │      ✅      │")
    println("│ sorted()             │      ❌      │      ✅      │")
    println("│ min(), max()        │      ❌      │      ✅      │")
    println("│ Type safety         │      ❌      │      ✅      │")
    println("└─────────────────────┴──────────────┴──────────────┘")
    
    println("\n=== ANSWER ===")
    println("YES! Always prefer implementing Comparable<T>")
    println("It enables comparison operators and standard library functions")
}

class Person(val strength: Int) {
    // PLUS operator - uses + sign
    operator fun plus(other: Person): Int {
        return this.strength + other.strength
    }

    // MINUS operator - uses - sign
    operator fun minus(other: Person) = strength - other.strength

    // INVOKE operator - call object like function with parentheses ()
    // Syntax: obj() or obj(arg1, arg2)
    operator fun invoke(): Int {
        return strength
    }
    
    // GET operator with Int parameter - access using square brackets []
    // Syntax: obj[index]
    operator fun get(index: Int): Int {
        return when (index) {
            0 -> strength
            1 -> strength * 2
            2 -> strength * 3
            else -> 0
        }
    }
    
    // GET operator with String parameter - map-like access
    // Syntax: obj["property"]
    operator fun get(property: String): Any? {
        return when (property.lowercase()) {
            "name" -> "Person"
            "strength" -> strength
            "health" -> strength * 10
            "defense" -> strength / 2
            else -> null
        }
    }

    // getValue is for delegated properties (used with 'by' keyword)
    // This is NOT the same as the 'get' operator!
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return strength + 45
    }
}

// Helper classes for demonstration
class Matrix(val rows: Int, val cols: Int) {
    private val data = Array(rows) { IntArray(cols) { 0 } }
    
    // GET with two parameters
    operator fun get(row: Int, col: Int): Int {
        require(row in 0 until rows && col in 0 until cols) {
            "Index out of bounds"
        }
        return data[row][col]
    }
    
    // SET operator
    operator fun set(row: Int, col: Int, value: Int) {
        require(row in 0 until rows && col in 0 until cols) {
            "Index out of bounds"
        }
        data[row][col] = value
    }
}

class StudentInfo(val name: String, val marks: Int, val age: Int) {
    operator fun get(property: String): Any? {
        return when (property.lowercase()) {
            "name" -> name
            "marks" -> marks
            "age" -> age
            "grade" -> when {
                marks >= 90 -> "A"
                marks >= 80 -> "B"
                marks >= 70 -> "C"
                else -> "D"
            }
            else -> null
        }
    }
}

// Sealed class for demonstrating Nothing type
sealed class ResultType<out T> {
    data class Success<T>(val data: T) : ResultType<T>()
    data class Error(val message: String) : ResultType<Nothing>()  // ← Nothing!
    object Loading : ResultType<Nothing>()                          // ← Nothing!
}

