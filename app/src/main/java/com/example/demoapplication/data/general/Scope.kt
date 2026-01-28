package com.example.demoapplication.data.general

fun main() {
    val x = "123".toInt()
    println(x)
   val str : String? = "hello"
    str?.apply {
        println(length)
    }
    run {
        println("help")
    }

    with(str) {

    }
    
    // ========================================================================
    // ?.let vs ?.run - Which one to prefer?
    // ========================================================================
    
    // ✅ PREFERRED: Use ?.let for null safety
    // 'it' is explicit and makes it clear you're working with nullable value
    val length1 = str?.let { it.length } ?: 0
    
    // ⚠️ WORKS but less preferred: ?.run for null safety
    // 'this' is implicit and less clear with nullable values
    val length2 = str?.run { this.length } ?: 0
    
    // ✅ PREFERRED: let for transformations
    val upper1 = str?.let { it.uppercase() } ?: ""
    
    // ⚠️ WORKS but less preferred: run for transformations
    val upper2 = str?.run { this.uppercase() } ?: ""
    
    println("Length (let): $length1")
    println("Length (run): $length2")
    println("Upper (let): $upper1")
    println("Upper (run): $upper2")
    
    /*
     * SUMMARY:
     * 
     * ✅ ALWAYS prefer ?.let for null safety because:
     *    1. 'it' is explicit - clearer intent
     *    2. Standard Kotlin convention
     *    3. More readable for null checks
     *    4. Better for chaining
     * 
     * ⚠️ ?.run works but is less idiomatic:
     *    - 'this' is implicit (less clear with nullable values)
     *    - Not the standard pattern
     *    - Can be confusing
     * 
     * Both return lambda result, but ?.let is the preferred choice!
     */
}

fun <T, E> T.letSelf(block : (T) -> E) = block(this)

// ============================================================================
// NON-EXTENSION RUN Examples
// ============================================================================

/**
 * Non-extension run: run { } - No receiver object
 * 
 * Use cases:
 * 1. Execute multiple statements and return a result
 * 2. Create local scope to avoid temporary variables
 * 3. Compute complex values
 * 4. Early returns in expressions
 */
fun nonExtensionRunDemo() {
    // Example 1: Multiple statements returning a result
    val sum = run {
        val a = 10
        val b = 20
        val c = 30
        a + b + c  // Returns 60
    }
    println("Sum: $sum")
    
    // Example 2: Avoid temporary variables
    val processed = run {
        val input = "  hello world  "
        val trimmed = input.trim()
        val uppercased = trimmed.uppercase()
        uppercased.replace(" ", "_")  // Returns "HELLO_WORLD"
    }
    println("Processed: $processed")
    
    // Example 3: Complex calculation
    val result = run {
        val numbers = listOf(1, 2, 3, 4, 5)
        val sum = numbers.sum()
        val average = sum.toDouble() / numbers.size
        "Sum: $sum, Average: $average"
    }
    println("Result: $result")
    
    // Example 4: Early return
    val value = run {
        val condition = false
        if (condition) return@run "Early return"
        "Normal execution"
    }
    println("Value: $value")
    
    /*
     * KEY DIFFERENCES:
     * 
     * Extension run: object.run { } - Has receiver object
     *   - Uses 'this' to refer to the object
     *   - Example: "Hello".run { this.length }
     * 
     * Non-extension run: run { } - No receiver object
     *   - Just executes a block of code
     *   - Returns the last expression
     *   - Example: run { val a = 5; val b = 10; a + b }
     * 
     * When to use non-extension run:
     * ✅ Multiple statements that compute a single result
     * ✅ Avoid temporary variables
     * ✅ Complex calculations
     * ✅ Early returns in expressions
     * 
     * When NOT to use:
     * ❌ Single expression (just use the expression directly)
     * ❌ Working with a specific object (use extension run or with)
     */
}

// ============================================================================
// EAGER vs LAZY Evaluation
// ============================================================================

/**
 * IMPORTANT: run executes IMMEDIATELY (Eager Evaluation)
 * 
 * The run block executes when the line is encountered, NOT when the variable is used.
 */
fun eagerVsLazyEvaluation() {
    println("=== EAGER EVALUATION (run) ===")
    
    // ❌ This executes IMMEDIATELY when this line is reached
    val result = run {
        println("1. calculateValue() called NOW")
        val temp = calculateValue()
        println("2. process() called NOW")
        val processed = process(temp)
        println("3. format() called NOW")
        format(processed)
    }
    // All three functions have already been called above!
    
    println("4. Now using result: $result")
    // The run block already executed, we're just using the stored result
    
    println("\n=== LAZY EVALUATION (lazy delegate) ===")
    
    // ✅ This executes ONLY when result2 is first accessed
    val result2 by lazy {
        println("1. calculateValue() called NOW (first access)")
        val temp = calculateValue()
        println("2. process() called NOW (first access)")
        val processed = process(temp)
        println("3. format() called NOW (first access)")
        format(processed)
    }
    // Nothing executed yet!
    
    println("4. About to use result2...")
    println("5. Using result2: $result2")  // NOW it executes!
    println("6. Using result2 again: $result2")  // Already computed, no execution
    
    println("\n=== LAZY EVALUATION (lambda stored) ===")
    
    // ✅ Lambda stored but not executed
    val result3: () -> String = {
        println("1. Lambda executed NOW (when invoked)")
        val temp = calculateValue()
        val processed = process(temp)
        format(processed)
    }
    // Nothing executed yet!
    
    println("2. About to invoke lambda...")
    val value = result3()  // NOW it executes!
    println("3. Result: $value")
}

fun calculateValue(): String {
    println("  → calculateValue() executing")
    return "calculated"
}

fun process(value: String): String {
    println("  → process() executing with: $value")
    return "processed_$value"
}

fun format(value: String): String {
    println("  → format() executing with: $value")
    return "formatted_$value"
}

/**
 * COMPARISON TABLE:
 * 
 * | Method          | When Executes        | Use Case                    |
 * |-----------------|----------------------|-----------------------------|
 * | run { }         | IMMEDIATELY          | Eager evaluation            |
 * | lazy { }        | First access         | Expensive computation       |
 * | () -> T         | When invoked         | Deferred execution          |
 * | Sequence        | When terminal op     | Collection processing       |
 */

// ============================================================================
// HOW run SAVES TEMPORARY VARIABLES - Variable Scope Explanation
// ============================================================================

/**
 * HOW run "saves" temporary variables:
 * 
 * It doesn't actually "save" them - it creates a LOCAL SCOPE where temporary
 * variables are scoped INSIDE the lambda and NOT accessible outside.
 * 
 * This prevents temporary variables from polluting the outer scope.
 */
fun howRunSavesTemporaryVariables() {
    
    println("=== WITHOUT run (Temporary variables in outer scope) ===")
    
    // ❌ Problem: Temporary variables pollute outer scope
    fun processWithoutRun(input: String): String {
        val trimmed = input.trim()           // Temporary variable in outer scope
        val uppercased = trimmed.uppercase() // Temporary variable in outer scope
        val processed = uppercased.replace(" ", "_") // Temporary variable in outer scope
        
        // These variables are still accessible here and can cause:
        // 1. Name conflicts
        // 2. Memory (variables stay in scope longer)
        // 3. Clutter in outer scope
        
        return processed
    }
    
    // After function returns, trimmed, uppercased, processed are gone
    // But they were visible throughout the entire function body
    
    println("\n=== WITH run (Temporary variables scoped inside) ===")
    
    // ✅ Solution: Temporary variables scoped inside run block
    fun processWithRun(input: String): String {
        return run {
            val trimmed = input.trim()           // Scoped INSIDE run block
            val uppercased = trimmed.uppercase() // Scoped INSIDE run block
            uppercased.replace(" ", "_")         // Last expression returned
        }
        // trimmed, uppercased are NOT accessible here - they're scoped to run block
    }
    
    println("\n=== DETAILED COMPARISON ===")
    
    // Example 1: Multiple temporary variables
    fun example1() {
        // ❌ WITHOUT run - variables in outer scope
        val data = fetchData()
        val cleaned = data.trim()
        val parsed = cleaned.toIntOrNull()
        val result = parsed?.let { it * 2 } ?: 0
        // data, cleaned, parsed are all in outer scope
        
        // ✅ WITH run - variables scoped inside
        val result2 = run {
            val data = fetchData()        // Scoped to run block
            val cleaned = data.trim()     // Scoped to run block
            val parsed = cleaned.toIntOrNull() // Scoped to run block
            parsed?.let { it * 2 } ?: 0  // Returned from run block
        }
        // data, cleaned, parsed are NOT accessible here
    }
    
    // Example 2: Avoiding name conflicts
    fun example2() {
        val user = getUser()
        
        // ❌ WITHOUT run - potential name conflict
        val name = user.name
        val processedName = name.uppercase()
        // What if we need 'name' again? Conflict!
        
        // ✅ WITH run - no conflict, variables scoped
        val processedName2 = run {
            val name = user.name  // Scoped inside, won't conflict with outer 'name'
            name.uppercase()
        }
        // 'name' inside run doesn't affect outer scope
    }
    
    // Example 3: Complex calculation
    fun example3() {
        // ❌ WITHOUT run - many temporary variables
        val numbers = listOf(1, 2, 3, 4, 5)
        val sum = numbers.sum()
        val count = numbers.size
        val average = sum.toDouble() / count
        val max = numbers.maxOrNull() ?: 0
        val min = numbers.minOrNull() ?: 0
        val result = "Sum: $sum, Avg: $average, Max: $max, Min: $min"
        // All variables (numbers, sum, count, average, max, min) in outer scope
        
        // ✅ WITH run - all scoped inside
        val result2 = run {
            val numbers = listOf(1, 2, 3, 4, 5)
            val sum = numbers.sum()
            val count = numbers.size
            val average = sum.toDouble() / count
            val max = numbers.maxOrNull() ?: 0
            val min = numbers.minOrNull() ?: 0
            "Sum: $sum, Avg: $average, Max: $max, Min: $min"
        }
        // All variables scoped inside run block
    }
    
    println("\n=== KEY INSIGHT ===")
    println("""
        run doesn't 'save' variables - it SCOPES them!
        
        Variables inside run block:
        ✅ Exist only within the run block
        ✅ Cannot be accessed outside
        ✅ Don't pollute outer scope
        ✅ Automatically cleaned up after run completes
        
        This is about VARIABLE SCOPE, not memory optimization!
    """.trimIndent())
}

fun fetchData(): String = "  123  "
fun getUser(): User = User("John", "john@example.com")

/**
 * VISUAL REPRESENTATION:
 * 
 * WITHOUT run:
 * ┌─────────────────────────────────────┐
 * │  function scope                     │
 * │  ├─ val temp1 = ...                 │ ← Visible everywhere in function
 * │  ├─ val temp2 = ...                 │ ← Visible everywhere in function
 * │  ├─ val temp3 = ...                 │ ← Visible everywhere in function
 * │  └─ return temp3                    │
 * └─────────────────────────────────────┘
 * 
 * WITH run:
 * ┌─────────────────────────────────────┐
 * │  function scope                     │
 * │  ┌───────────────────────────────┐  │
 * │  │  run block scope              │  │
 * │  │  ├─ val temp1 = ...           │  │ ← Only visible inside run
 * │  │  ├─ val temp2 = ...           │  │ ← Only visible inside run
 * │  │  ├─ val temp3 = ...           │  │ ← Only visible inside run
 * │  │  └─ return temp3              │  │
 * │  └───────────────────────────────┘  │
 * │  └─ val result = ...                │ ← Only result accessible here
 * └─────────────────────────────────────┘
 */

