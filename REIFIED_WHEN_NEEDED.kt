// ============================================================================
// WHEN REIFIED IS NEEDED vs NOT NEEDED - Key Distinction
// ============================================================================

// ============================================================================
// YOUR EXAMPLE - WORKS WITHOUT REIFIED ✅
// ============================================================================

fun <T> getTypeName(param: T) {
    if (param is String) {
        println("hello string")
    } else {
        println("not a string")
    }
}

// ✅ This works because:
// - 'param' is a RUNTIME VALUE
// - We're checking the actual object's type at runtime
// - Type erasure doesn't affect checking runtime values

fun example1() {
    getTypeName("hello")      // Works: "hello string"
    getTypeName(123)          // Works: "not a string"
    getTypeName(true)         // Works: "not a string"
}

// ============================================================================
// WHEN REIFIED IS ACTUALLY NEEDED ❌
// ============================================================================

// ❌ PROBLEM: Checking against TYPE PARAMETER T (not runtime value)
fun <T> checkType(obj: Any) {
    // if (obj is T) { }  // ERROR: Cannot check for instance of erased type: T
    // T is erased at runtime, so we can't check against it
}

// ✅ SOLUTION: Use reified
inline fun <reified T> checkType(obj: Any) {
    if (obj is T) {  // ✅ Works! T is available at runtime
        println("Object is of type ${T::class.simpleName}")
    }
}

fun example2() {
    checkType<String>("hello")  // Works with reified
    checkType<Int>("hello")      // Works with reified
}

// ============================================================================
// KEY DISTINCTION
// ============================================================================

/*
 * WITHOUT REIFIED:
 * - Can check: param is String ✅ (checking runtime value)
 * - Can check: obj is ConcreteType ✅ (checking runtime value)
 * - Cannot check: obj is T ❌ (T is erased)
 * 
 * WITH REIFIED:
 * - Can check: obj is T ✅ (T is available at runtime)
 * - Can access: T::class ✅ (type info available)
 * - Can use: T::class.java ✅ (Java class available)
 */

// ============================================================================
// EXAMPLES: WHEN REIFIED IS NEEDED
// ============================================================================

// Example 1: Checking against type parameter
// ❌ WITHOUT REIFIED - Doesn't work
fun <T> isInstanceOf(obj: Any): Boolean {
    // return obj is T  // ERROR: Cannot check for instance of erased type: T
    return false
}

// ✅ WITH REIFIED - Works
inline fun <reified T> isInstanceOf(obj: Any): Boolean {
    return obj is T  // ✅ Works!
}

fun example3() {
    val isString = isInstanceOf<String>("hello")  // true
    val isInt = isInstanceOf<Int>("hello")        // false
}

// Example 2: Getting class of type parameter
// ❌ WITHOUT REIFIED - Need to pass Class<T>
fun <T> getClassName(clazz: Class<T>): String {
    return clazz.simpleName
}

// ✅ WITH REIFIED - No Class parameter needed
inline fun <reified T> getClassName(): String {
    return T::class.simpleName ?: "Unknown"
}

fun example4() {
    // Without reified: Verbose
    val name1 = getClassName(String::class.java)  // Need to pass Class
    
    // With reified: Clean
    val name2 = getClassName<String>()  // No Class parameter needed
}

// Example 3: Creating instance of type parameter
// ❌ WITHOUT REIFIED - Need to pass Class<T>
fun <T> createInstance(clazz: Class<T>): T {
    return clazz.newInstance()
}

// ✅ WITH REIFIED - No Class parameter needed
inline fun <reified T> createInstance(): T {
    return T::class.java.newInstance()
}

fun example5() {
    // Without reified: Verbose
    val string1 = createInstance(String::class.java)
    
    // With reified: Clean
    val string2 = createInstance<String>()
}

// ============================================================================
// YOUR FUNCTION - WHY IT WORKS
// ============================================================================

fun <T> getTypeName(param: T) {
    // ✅ This works because 'param' is a runtime value
    // We're checking the actual object's type, not the type parameter T
    if (param is String) {  // Checking runtime value
        println("hello string")
    } else {
        println("not a string")
    }
}

// What happens at runtime:
// - T is erased: getTypeName(param: Any)  // T becomes Any
// - But param still holds the actual object
// - We can check: param is String (checking the object's type)

// ============================================================================
// WHEN YOU WOULD NEED REIFIED IN YOUR FUNCTION
// ============================================================================

// If you wanted to check against T itself:
inline fun <reified T> getTypeNameReified(param: Any) {
    // Now we can check against T
    if (param is T) {  // ✅ Works with reified
        println("Object is of type ${T::class.simpleName}")
    } else {
        println("Object is NOT of type ${T::class.simpleName}")
    }
}

fun example6() {
    getTypeNameReified<String>("hello")  // "Object is of type String"
    getTypeNameReified<Int>("hello")     // "Object is NOT of type Int"
}

// ============================================================================
// COMPARISON TABLE
// ============================================================================

/*
 * | Scenario                          | Without Reified | With Reified |
 * |-----------------------------------|-----------------|--------------|
 * | Check: param is String            | ✅ Works        | ✅ Works     |
 * | Check: obj is T                    | ❌ Error        | ✅ Works     |
 * | Access: T::class                   | ❌ Error        | ✅ Works     |
 * | Pass Class<T> parameter           | ✅ Required      | ✅ Not needed|
 * | Runtime type checking             | ✅ Works        | ✅ Works     |
 * | Type parameter checking           | ❌ Doesn't work | ✅ Works     |
 */

// ============================================================================
// REAL-WORLD EXAMPLE: YOUR FUNCTION vs REIFIED VERSION
// ============================================================================

// Your version: Works fine, checks runtime value
fun <T> checkParamType(param: T) {
    when {
        param is String -> println("It's a String")
        param is Int -> println("It's an Int")
        param is Boolean -> println("It's a Boolean")
        else -> println("Unknown type")
    }
}

// Reified version: Can check against type parameter
inline fun <reified T> checkAgainstType(param: Any) {
    when {
        param is T -> println("Matches type ${T::class.simpleName}")
        else -> println("Doesn't match type ${T::class.simpleName}")
    }
}

fun example7() {
    // Your version: Checks what param actually is
    checkParamType("hello")    // "It's a String"
    checkParamType(123)        // "It's an Int"
    
    // Reified version: Checks if param matches T
    checkAgainstType<String>("hello")  // "Matches type String"
    checkAgainstType<Int>("hello")      // "Doesn't match type Int"
}

// ============================================================================
// SUMMARY
// ============================================================================

/*
 * YOUR FUNCTION WORKS WITHOUT REIFIED BECAUSE:
 * 
 * ✅ You're checking: param is String
 *    - 'param' is a runtime value
 *    - 'String' is a concrete type (not erased)
 *    - Type erasure doesn't affect checking runtime values
 * 
 * ❌ REIFIED WOULD BE NEEDED IF:
 * 
 *    - You wanted to check: obj is T
 *      (T is erased, need reified to access it)
 * 
 *    - You wanted to access: T::class
 *      (T is erased, need reified to access type info)
 * 
 *    - You wanted to avoid: passing Class<T> parameter
 *      (Reified eliminates the need for Class parameter)
 * 
 * KEY INSIGHT:
 * - Checking runtime values: Works without reified ✅
 * - Checking type parameters: Needs reified ❌→✅
 * - Accessing type info: Needs reified ❌→✅
 */

// ============================================================================
// PRACTICAL EXAMPLE: When You'd Actually Need Reified
// ============================================================================

// Scenario: Filter list by type parameter
// ❌ WITHOUT REIFIED - Doesn't work
fun <T> List<*>.filterByType(): List<T> {
    // return filter { it is T }  // ERROR: Cannot check for instance of erased type: T
    return emptyList()
}

// ✅ WITH REIFIED - Works
inline fun <reified T> List<*>.filterByType(): List<T> {
    return filterIsInstance<T>()  // ✅ Works! Can check against T
}

fun example8() {
    val mixed = listOf(1, "hello", 2, "world")
    
    // With reified: Clean API
    val numbers = mixed.filterByType<Int>()    // [1, 2]
    val strings = mixed.filterByType<String>() // ["hello", "world"]
}

// ============================================================================
// YOUR FUNCTION - ENHANCED VERSION WITH REIFIED
// ============================================================================

// Your original (works without reified)
fun <T> getTypeName(param: T) {
    if (param is String) {
        println("hello string")
    } else {
        println("not a string")
    }
}

// Enhanced version with reified (if you wanted to check against T)
inline fun <reified T> getTypeNameEnhanced(param: Any) {
    when {
        param is T -> println("Param matches type ${T::class.simpleName}")
        param is String -> println("Param is String (but not T)")
        else -> println("Param is neither T nor String")
    }
}

fun example9() {
    // Original: Checks what param is
    getTypeName("hello")  // "hello string"
    
    // Enhanced: Checks if param matches T
    getTypeNameEnhanced<String>("hello")  // "Param matches type String"
    getTypeNameEnhanced<Int>("hello")     // "Param is String (but not T)"
}

// ============================================================================
// CONCLUSION
// ============================================================================

/*
 * Your function works WITHOUT reified because:
 * 
 * 1. You're checking a runtime value (param) against a concrete type (String)
 * 2. Type erasure doesn't affect checking runtime values
 * 3. The actual object's type is available at runtime
 * 
 * Reified is needed when:
 * 
 * 1. You want to check against the type parameter T itself
 * 2. You want to access T::class or T::class.java
 * 3. You want to avoid passing Class<T> parameters
 * 4. You want type-safe APIs without verbose Class parameters
 * 
 * Your function is fine as-is! Reified would only be needed if you
 * wanted to check against T or access T's type information.
 */
