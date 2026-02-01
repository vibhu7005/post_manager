package com.example.demoapplication.data.general

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

fun main() {
    runBlocking {
        // ⚠️ IMPORTANT: This SHOULD catch the exception, but there's a timing issue!
        // When you await() a failed Deferred, the exception IS propagated
        // BUT: You're only waiting 1000ms, while the second async takes 3000ms
        // So the exception might not have been thrown yet when delay(1000) completes
        
        println("=== Testing supervisorScope with async + await ===\n")
        
        try {
            supervisorScope {
                val deferred = async {
                    println("A - Starting async that will fail")
                    delay(100) // Simulate some work
                    throw RuntimeException("fdf")
                }
                async {
                    println("B - Starting async that takes 3 seconds")
                    delay(3000)
                    println("hello")
                }.await() // This completes first (takes 3 seconds)
                
                println("About to await deferred (this will throw)")
                deferred.await() // ⚠️ This WILL throw and propagate!
            }
        } catch (ex : Exception) {
            println("✅ Exception caught: ${ex.message}") // This SHOULD execute!
        }
        
        // ⚠️ ISSUE: delay(1000) is too short!
        // The second async takes 3000ms, so you need to wait longer
        // OR the exception is thrown but you're not waiting long enough to see it
        delay(5000) // Wait long enough for everything to complete
    }
    
    // To see examples explaining async + await behavior, run:
    // exampleAsyncAwait1_SupervisorScopeWithAwait()
    // exampleAsyncAwait2_WhyItWorks()
    // exampleAsyncAwait3_TimingIssue()
}

/**
 * LAUNCH EXCEPTION HANDLING - WHY TRY-CATCH DOESN'T WORK
 * 
 * CRITICAL CONCEPT: launch {} is NON-BLOCKING and returns immediately!
 * 
 * When you write:
 *   try {
 *       launch { throw Exception() }
 *   } catch (e: Exception) {
 *       // This will NOT catch the exception!
 *   }
 * 
 * Why it doesn't work:
 * 1. launch {} returns a Job immediately (non-blocking)
 * 2. The try-catch block completes BEFORE the exception is thrown
 * 3. The exception happens asynchronously, later
 * 4. Exceptions in root coroutines go to CoroutineExceptionHandler or crash the app
 * 
 * Exception Propagation Rules:
 * 1. launch {} - Non-blocking, exceptions don't propagate to caller
 * 2. async {} - Returns Deferred, exceptions propagate when await() is called
 * 3. Unhandled exceptions in root coroutines go to CoroutineExceptionHandler
 * 4. Unhandled exceptions in non-root coroutines propagate to parent
 * 
 * How to catch launch exceptions:
 * - Use CoroutineExceptionHandler
 * - Use join() to wait for completion, then check job state
 * - Use coroutineScope {} or supervisorScope {} for structured concurrency
 */

/**
 * ASYNC EXCEPTION HANDLING - WHEN EXCEPTIONS ARE CAUGHT VS HANDLED
 * 
 * Key Concept: async() returns a Deferred<T>, and exceptions in async are stored
 * in the Deferred until await() is called.
 * 
 * IMPORTANT DISTINCTION:
 * - "Caught" = caught by try-catch block
 * - "Handled" = handled by CoroutineExceptionHandler
 * 
 * launch { async { throw } } WITHOUT await():
 * - Exception goes to CoroutineExceptionHandler (NOT caught by try-catch)
 * - This is because launch completes before async throws, and unawaited Deferred exceptions
 *   propagate to the handler when the parent coroutine completes
 * 
 * launch { try { async { throw }.await() } catch { } } WITH await():
 * - Exception IS caught by try-catch (handler NOT called)
 * - This is because await() causes the exception to propagate immediately
 */

// ============================================================================
// LAUNCH EXCEPTION HANDLING EXAMPLES
// ============================================================================

// Example L1: Why try-catch doesn't work with launch
fun exampleL1_WhyTryCatchDoesntWorkWithLaunch() {
    runBlocking {
        println("=== Example L1: Why try-catch doesn't work with launch ===\n")
        
        println("Attempting to catch launch exception with try-catch:")
        try {
            CoroutineScope(Job()).launch {
                delay(100) // Simulate some work
                throw Exception("Exception in launch")
            }
            println("✓ launch() returned immediately (non-blocking)")
            println("✗ try-catch block completed BEFORE exception was thrown")
        } catch (ex: Exception) {
            println("✗ This catch block will NEVER execute: ${ex.message}")
        }
        
        delay(500)
        println("\nException was thrown AFTER try-catch completed!")
        println("Result: Exception goes to CoroutineExceptionHandler or crashes app\n")
    }
}

// Example L2: Using CoroutineExceptionHandler to catch launch exceptions
fun exampleL2_UsingExceptionHandler() {
    runBlocking {
        println("=== Example L2: Using CoroutineExceptionHandler ===\n")
        
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("✓ Exception caught by CoroutineExceptionHandler: ${throwable.message}")
        }
        
        val scope = CoroutineScope(Job() + exceptionHandler)
        
        // This exception WILL be handled by the handler
        scope.launch {
            delay(100)
            throw Exception("Exception in launch")
        }
        
        delay(500)
        println("Result: Exception was handled by CoroutineExceptionHandler\n")
    }
}

// Example L3: Using join() with exception handler
fun exampleL3_UsingJoinWithExceptionHandler() {
    runBlocking {
        println("=== Example L3: Using join() with exception handler ===\n")
        
        var caughtException: Throwable? = null
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            caughtException = throwable
            println("✓ Exception caught by handler: ${throwable.message}")
        }
        
        val scope = CoroutineScope(Job() + exceptionHandler)
        
        val job = scope.launch {
            delay(100)
            throw Exception("Exception in launch")
        }
        
        // Wait for job to complete
        job.join()
        
        if (caughtException != null) {
            println("✓ Job completed with exception: ${caughtException!!.message}")
        }
        
        delay(200)
    }
}

// Example L4: Using coroutineScope for structured concurrency (exceptions propagate)
fun exampleL4_UsingCoroutineScope() {
    runBlocking {
        println("=== Example L4: Using coroutineScope (structured concurrency) ===\n")
        
        try {
            // coroutineScope creates a structured scope where exceptions propagate
            coroutineScope {
                launch {
                    delay(100)
                    throw Exception("Exception in launch")
                }
                // coroutineScope waits for all children to complete
                // If any child throws, coroutineScope throws
            }
        } catch (ex: Exception) {
            println("✓ Exception caught by try-catch: ${ex.message}")
            println("Result: coroutineScope propagates exceptions to caller\n")
        }
    }
}

// Example L5: Using supervisorScope (exceptions don't propagate)
fun exampleL5_UsingSupervisorScope() {
    runBlocking {
        println("=== Example L5: Using supervisorScope ===\n")
        
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("✓ Exception handled by handler: ${throwable.message}")
        }
        
        try {
            // supervisorScope isolates failures - exceptions don't propagate
            supervisorScope {
                launch(exceptionHandler) {
                    delay(100)
                    throw Exception("Exception in launch")
                }
                delay(200)
                println("supervisorScope continues even after child exception")
            }
            println("✓ supervisorScope completed successfully")
        } catch (ex: Exception) {
            println("✗ This won't catch the exception: ${ex.message}")
        }
        
        delay(300)
    }
}

// Example SupervisorScope1: Why try-catch doesn't work with supervisorScope
fun exampleSupervisorScope1_WhyTryCatchDoesntWork() {
    runBlocking {
        println("=== SupervisorScope Example 1: Why try-catch doesn't work ===\n")
        
        println("❌ Try-catch with supervisorScope:")
        try {
            supervisorScope {
                launch {
                    delay(100)
                    throw RuntimeException("Exception in launch")
                }
            }
            println("✓ supervisorScope completed successfully")
            println("  (Exception was ISOLATED, not propagated)")
        } catch (ex: Exception) {
            println("✗ This catch block will NEVER execute: ${ex.message}")
        }
        
        delay(500)
        println("\nKey Point: supervisorScope ISOLATES failures - it doesn't throw exceptions!")
        println("The exception is isolated and doesn't propagate to the try-catch\n")
    }
}

// Example SupervisorScope2: Using exception handler with supervisorScope
fun exampleSupervisorScope2_UsingExceptionHandler() {
    runBlocking {
        println("=== SupervisorScope Example 2: Using Exception Handler ===\n")
        
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("✓ Exception caught by handler: ${throwable.message}")
        }
        
        // ✅ CORRECT: Use exception handler to catch exceptions in supervisorScope
        supervisorScope {
            launch(exceptionHandler) {
                delay(100)
                throw RuntimeException("Exception in launch")
            }
            delay(200)
            println("supervisorScope continues (exception was isolated)")
        }
        
        delay(300)
        println("Result: Exception was handled by CoroutineExceptionHandler\n")
    }
}

// Example SupervisorScope3: Using coroutineScope (exceptions DO propagate)
fun exampleSupervisorScope3_UsingCoroutineScope() {
    runBlocking {
        println("=== SupervisorScope Example 3: coroutineScope vs supervisorScope ===\n")
        
        println("✅ Using coroutineScope (exceptions PROPAGATE):")
        try {
            coroutineScope {
                launch {
                    delay(100)
                    throw RuntimeException("Exception in launch")
                }
            }
        } catch (ex: Exception) {
            println("✓ Exception caught by try-catch: ${ex.message}")
        }
        
        delay(200)
        
        println("\n❌ Using supervisorScope (exceptions DON'T propagate):")
        try {
            supervisorScope {
                launch {
                    delay(100)
                    throw RuntimeException("Exception in launch")
                }
            }
            println("✓ supervisorScope completed (exception was isolated)")
        } catch (ex: Exception) {
            println("✗ This won't catch: ${ex.message}")
        }
        
        delay(500)
        println("\nKey Difference:")
        println("  coroutineScope: Exceptions propagate → try-catch WORKS")
        println("  supervisorScope: Exceptions isolated → try-catch DOESN'T work\n")
    }
}

// ============================================================================
// ASYNC + AWAIT IN SUPERVISORSCOPE - IMPORTANT DISTINCTION!
// ============================================================================

/**
 * CRITICAL UNDERSTANDING: async + await() BEHAVIOR
 * 
 * Key Point: When you await() a failed Deferred, await() THROWS the exception!
 * This exception IS propagated, even in supervisorScope!
 * 
 * Difference:
 * - launch {} exceptions: Don't propagate (isolated)
 * - async {} exceptions WITHOUT await(): Don't propagate (stay in Deferred)
 * - async {} exceptions WITH await(): DO propagate (await() throws)
 */

// Example AsyncAwait1: supervisorScope with async + await (SHOULD catch!)
fun exampleAsyncAwait1_SupervisorScopeWithAwait() {
    runBlocking {
        println("=== AsyncAwait Example 1: supervisorScope + async + await ===\n")
        
        println("✅ This SHOULD catch the exception!")
        try {
            supervisorScope {
                val deferred = async {
                    delay(100)
                    throw RuntimeException("Exception in async")
                }
                deferred.await() // ⚠️ await() THROWS the exception!
            }
        } catch (ex: Exception) {
            println("✓ Exception caught: ${ex.message}")
        }
        
        println("\nKey Point: await() throws exceptions, so they propagate!\n")
    }
}

// Example AsyncAwait2: Why await() propagates exceptions
fun exampleAsyncAwait2_WhyItWorks() {
    runBlocking {
        println("=== AsyncAwait Example 2: Why await() propagates ===\n")
        
        println("When you call deferred.await():")
        println("  1. If Deferred succeeded → returns result")
        println("  2. If Deferred failed → THROWS the exception")
        println("  3. The exception is thrown from await(), not from async")
        println("  4. supervisorScope doesn't isolate exceptions thrown by await()")
        println("     (It only isolates exceptions from child coroutines)\n")
        
        try {
            supervisorScope {
                val deferred = async {
                    delay(100)
                    throw RuntimeException("Error")
                }
                // await() throws here, and this exception propagates!
                deferred.await()
            }
        } catch (e: Exception) {
            println("✓ Caught: ${e.message}")
        }
    }
}

// Example AsyncAwait3: Timing issue in your code
fun exampleAsyncAwait3_TimingIssue() {
    runBlocking {
        println("=== AsyncAwait Example 3: Timing Issue ===\n")
        
        println("Your code has a timing problem:")
        println("  - Second async takes 3000ms")
        println("  - You only wait 1000ms")
        println("  - Exception might not have been thrown yet!\n")
        
        try {
            supervisorScope {
                val deferred1 = async {
                    println("Deferred1: Starting (will fail in 100ms)")
                    delay(100)
                    throw RuntimeException("Error in deferred1")
                }
                
                val deferred2 = async {
                    println("Deferred2: Starting (takes 3000ms)")
                    delay(3000)
                    println("Deferred2: Completed")
                }
                
                println("Awaiting deferred2 first (takes 3 seconds)...")
                deferred2.await() // Takes 3 seconds
                
                println("Now awaiting deferred1 (will throw immediately)...")
                deferred1.await() // This throws!
            }
        } catch (ex: Exception) {
            println("✅ Exception caught: ${ex.message}")
        }
        
        println("\nIf you only wait 1000ms, deferred2 hasn't finished yet,")
        println("so deferred1.await() hasn't been called, so no exception yet!\n")
    }
}

// Example AsyncAwait4: async WITHOUT await (doesn't propagate)
fun exampleAsyncAwait4_AsyncWithoutAwait() {
    runBlocking {
        println("=== AsyncAwait Example 4: async WITHOUT await ===\n")
        
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            println("✓ Handler caught: ${e.message}")
        }
        
        println("❌ Without await(), exception doesn't propagate:")
        try {
            supervisorScope {
                async {
                    delay(100)
                    throw RuntimeException("Error")
                }
                // No await() called!
            }
        } catch (ex: Exception) {
            println("✗ This won't catch: ${ex.message}")
        }
        
        delay(500)
        println("Result: Exception stays in Deferred, doesn't propagate\n")
        
        println("✅ With await(), exception DOES propagate:")
        try {
            supervisorScope {
                val deferred = async {
                    delay(100)
                    throw RuntimeException("Error")
                }
                deferred.await() // await() throws!
            }
        } catch (ex: Exception) {
            println("✓ Caught: ${ex.message}")
        }
    }
}

// Example AsyncAwait5: Complete comparison
fun exampleAsyncAwait5_CompleteComparison() {
    runBlocking {
        println("=== AsyncAwait Example 5: Complete Comparison ===\n")
        
        println("""
        ┌──────────────────────────────────────────────────────────────┐
        │  SCENARIO                    │  EXCEPTION PROPAGATES?         │
        ├──────────────────────────────────────────────────────────────┤
        │  launch { throw }            │  ❌ NO (isolated)              │
        │  async { throw }             │  ❌ NO (stays in Deferred)     │
        │  async { throw }.await()     │  ✅ YES (await() throws)       │
        │                              │                                │
        │  supervisorScope {           │                                │
        │    launch { throw }          │  ❌ NO (isolated)              │
        │    async { throw }           │  ❌ NO (stays in Deferred)    │
        │    async { throw }.await()   │  ✅ YES (await() throws)       │
        │  }                           │                                │
        │                              │                                │
        │  coroutineScope {            │                                │
        │    launch { throw }          │  ✅ YES (propagates)          │
        │    async { throw }.await()   │  ✅ YES (await() throws)       │
        │  }                           │                                │
        └──────────────────────────────────────────────────────────────┘
        """.trimIndent())
    }
}

// Example SupervisorScope4: Side-by-side comparison
fun exampleSupervisorScope4_Comparison() {
    runBlocking {
        println("=== SupervisorScope Example 4: Complete Comparison ===\n")
        
        println("Scenario A: supervisorScope WITHOUT exception handler")
        supervisorScope {
            launch {
                delay(100)
                throw RuntimeException("Exception A")
            }
        }
        delay(200)
        println("Result: Exception is LOST (no handler, no propagation)\n")
        
        println("Scenario B: supervisorScope WITH exception handler")
        val handler = CoroutineExceptionHandler { _, e ->
            println("✓ Caught: ${e.message}")
        }
        supervisorScope {
            launch(handler) {
                delay(100)
                throw RuntimeException("Exception B")
            }
        }
        delay(200)
        println("Result: Exception handled by handler\n")
        
        println("Scenario C: coroutineScope (exceptions propagate)")
        try {
            coroutineScope {
                launch {
                    delay(100)
                    throw RuntimeException("Exception C")
                }
            }
        } catch (e: Exception) {
            println("✓ Caught by try-catch: ${e.message}")
        }
        delay(200)
        println("Result: Exception caught by try-catch\n")
        
        println("""
        ┌─────────────────────────────────────────────────────────────┐
        │  SCOPE TYPE          │  EXCEPTION PROPAGATION │  TRY-CATCH  │
        ├─────────────────────────────────────────────────────────────┤
        │  coroutineScope      │  ✅ YES (propagates)   │  ✅ WORKS   │
        │  supervisorScope    │  ❌ NO (isolates)      │  ❌ NO      │
        │  launch (root)      │  ❌ NO (goes to handler)│  ❌ NO      │
        └─────────────────────────────────────────────────────────────┘
        """.trimIndent())
    }
}

// Example L6: Comparison - All ways to handle launch exceptions
fun exampleL6_AllWaysToHandleLaunchExceptions() {
    runBlocking {
        println("=== Example L6: All Ways to Handle Launch Exceptions ===\n")
        
        println("Method 1: Try-catch (DOESN'T WORK)")
        try {
            CoroutineScope(Job()).launch {
                delay(50)
                throw Exception("Method 1 exception")
            }
        } catch (e: Exception) {
            println("✗ Not caught: ${e.message}")
        }
        delay(200)
        
        println("\nMethod 2: CoroutineExceptionHandler (WORKS)")
        val handler = CoroutineExceptionHandler { _, e ->
            println("✓ Caught by handler: ${e.message}")
        }
        CoroutineScope(Job() + handler).launch {
            delay(50)
            throw Exception("Method 2 exception")
        }
        delay(200)
        
        println("\nMethod 3: coroutineScope (WORKS)")
        try {
            coroutineScope {
                launch {
                    delay(50)
                    throw Exception("Method 3 exception")
                }
            }
        } catch (e: Exception) {
            println("✓ Caught by try-catch: ${e.message}")
        }
        delay(200)
        
        println("\nMethod 4: supervisorScope + handler (WORKS)")
        val handler2 = CoroutineExceptionHandler { _, e ->
            println("✓ Caught by handler: ${e.message}")
        }
        supervisorScope {
            launch(handler2) {
                delay(50)
                throw Exception("Method 4 exception")
            }
        }
        delay(200)
    }
}

// ============================================================================
// ASYNC EXCEPTION HANDLING EXAMPLES
// ============================================================================

// Example 1: async without await - Exception NOT handled
fun example1_AsyncWithoutAwait_ExceptionNotHandled() {
    runBlocking {
        println("=== Example 1: async without await ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("Exception handled: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        // This creates a Deferred that contains an exception, but since await() is never called,
        // the exception stays in the Deferred and never propagates to the exception handler
        scope.async {
            async {
                delay(100)
                println("A")
                throw Exception("Exception in inner async")
            }
            // await() is NOT called here, so exception stays in Deferred
        }
        
        delay(500)
        println("Main: No exception was handled because await() was never called")
    }
}

// Example 2: launch with nested async - Exception goes to handler (NOT caught by try-catch)
fun example2_LaunchWithNestedAsync_ExceptionHandled() {
    runBlocking {
        println("=== Example 2: launch with nested async (NO await) ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("✓ Exception handled by CoroutineExceptionHandler: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        // IMPORTANT: Exception goes to CoroutineExceptionHandler, NOT caught by try-catch
        scope.launch {
            try {
                async {
                    delay(100)
                    println("A")
                    throw Exception("Exception in async")
                }
                // await() is NOT called, so exception doesn't propagate to try-catch
                // Instead, it goes to CoroutineExceptionHandler when launch completes
            } catch (e: Exception) {
                println("✗ This catch block will NOT catch the exception: ${e.message}")
            }
        }
        
        delay(500)
        println("Main: Exception went to handler, NOT caught by try-catch")
    }
}

// Example 2b: launch with nested async + await - Exception IS caught by try-catch
fun example2b_LaunchWithNestedAsync_Await_Caught() {
    runBlocking {
        println("=== Example 2b: launch with nested async (WITH await) ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("✗ Exception handler called: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        // When you await the async, exception propagates to try-catch
        scope.launch {
            try {
                val deferred = async {
                    delay(100)
                    println("A")
                    throw Exception("Exception in async")
                }
                deferred.await() // Exception propagates here when await() is called
            } catch (e: Exception) {
                println("✓ Exception caught by try-catch: ${e.message}")
            }
        }
        
        delay(500)
        println("Main: Exception was caught by try-catch, handler NOT called")
    }
}

// Example 3: async with await - Exception IS handled
fun example3_AsyncWithAwait_ExceptionHandled() {
    runBlocking {
        println("=== Example 3: async with await ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("Exception handled: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        try {
            val deferred = scope.async {
                delay(100)
                println("A")
                throw Exception("Exception in async")
            }
            deferred.await() // Exception propagates when await() is called
        } catch (e: Exception) {
            println("Caught exception: ${e.message}")
        }
        
        delay(500)
    }
}

// Example 4: Nested async with await - Exception IS handled
fun example4_NestedAsyncWithAwait_ExceptionHandled() {
    runBlocking {
        println("=== Example 4: Nested async with await ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("Exception handled: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        try {
            val outerDeferred = scope.async {
                val innerDeferred = async {
                    delay(100)
                    println("A")
                    throw Exception("Exception in inner async")
                }
                innerDeferred.await() // Exception propagates here
            }
            outerDeferred.await() // Then propagates here
        } catch (e: Exception) {
            println("Caught exception: ${e.message}")
        }
        
        delay(500)
    }
}

// Example 5: Why launch + async handles exception (detailed explanation)
fun example5_WhyLaunchHandlesAsyncException() {
    runBlocking {
        println("=== Example 5: Why launch handles async exception ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("Exception handled by handler: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        // When launch {} completes, if there are unawaited Deferred objects,
        // and those Deferreds have exceptions, those exceptions can propagate
        // because the launch coroutine is a root coroutine (top-level in scope)
        scope.launch {
            val deferred = async {
                delay(100)
                println("A")
                throw Exception("Exception in async")
            }
            // launch completes immediately without waiting
            // But the unawaited Deferred's exception propagates to the scope's exception handler
        }
        
        delay(500)
        println("Main: Exception was handled by CoroutineExceptionHandler")
    }
}

// Example 6: Comparison - All scenarios side by side
fun example6_AllScenariosComparison() {
    runBlocking {
        println("=== Example 6: All Scenarios Comparison ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("✓ Exception handled: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        println("Scenario 1: scope.async { async { throw } } - NO await")
        scope.async {
            async {
                delay(50)
                throw Exception("Scenario 1 exception")
            }
        }
        delay(200)
        println("Result: Exception NOT handled (stays in Deferred)\n")

        println("Scenario 2: scope.launch { async { throw } } - NO await")
        scope.launch {
            async {
                delay(50)
                throw Exception("Scenario 2 exception")
            }
        }
        delay(200)
        println("Result: Exception IS handled (propagates from root launch)\n")

        println("Scenario 3: scope.async { async { throw }.await() }")
        try {
            scope.async {
                async {
                    delay(50)
                    throw Exception("Scenario 3 exception")
                }.await()
            }.await()
        } catch (e: Exception) {
            println("Result: Exception caught with try-catch: ${e.message}\n")
        }
        
        delay(200)
    }
}

// Example 7: Clarifying launch + async exception handling
fun example7_LaunchAsyncExceptionHandling() {
    runBlocking {
        println("=== Example 7: launch { async { throw } } - Will exception be caught? ===\n")
        
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("🔴 CoroutineExceptionHandler caught: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        println("Scenario A: launch { async { throw } } - NO await, NO try-catch")
        scope.launch {
            async {
                delay(100)
                throw Exception("Scenario A exception")
            }
            // No await, no try-catch
        }
        delay(300)
        println("Result: Exception goes to CoroutineExceptionHandler (NOT caught by try-catch)\n")

        println("Scenario B: launch { try { async { throw } } catch { } } - NO await")
        scope.launch {
            try {
                async {
                    delay(100)
                    throw Exception("Scenario B exception")
                }
                // Still no await!
            } catch (e: Exception) {
                println("🔵 Try-catch caught: ${e.message}")
            }
        }
        delay(300)
        println("Result: Exception goes to CoroutineExceptionHandler (try-catch does NOT catch it)\n")

        println("Scenario C: launch { try { async { throw }.await() } catch { } } - WITH await")
        scope.launch {
            try {
                async {
                    delay(100)
                    throw Exception("Scenario C exception")
                }.await() // await() is called!
            } catch (e: Exception) {
                println("🟢 Try-catch caught: ${e.message}")
            }
        }
        delay(300)
        println("Result: Exception IS caught by try-catch (handler NOT called)\n")

        delay(200)
    }
}

// Example 8: Best practice - Always await async or handle exceptions
fun example8_BestPractices() {
    runBlocking {
        println("=== Example 7: Best Practices ===\n")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("Exception handled: ${throwable.message}")
        }
        val scope = CoroutineScope(Job() + exceptionHandler)

        // BEST PRACTICE 1: Always await async if you care about exceptions
        println("Best Practice 1: Always await async")
        try {
            val result = scope.async {
                delay(100)
                throw Exception("Error in async")
            }.await()
        } catch (e: Exception) {
            println("Caught: ${e.message}")
        }

        // BEST PRACTICE 2: Use launch for fire-and-forget, handle exceptions in launch
        println("\nBest Practice 2: Handle exceptions in launch")
        scope.launch {
            try {
                async {
                    delay(100)
                    throw Exception("Error in async")
                }.await()
            } catch (e: Exception) {
                println("Caught in launch: ${e.message}")
            }
        }

        // BEST PRACTICE 3: Don't create unawaited async in async (creates orphaned Deferred)
        println("\nBest Practice 3: Avoid unawaited async in async")
        scope.async {
            // BAD: This creates an orphaned Deferred
            // async { throw Exception("Orphaned") }
            
            // GOOD: Await it or use launch
            launch {
                delay(100)
                throw Exception("This will be handled")
            }
        }

        delay(500)
    }
}

/**
 * NON-CANCELLABLE CODE EXAMPLES
 *
 * NonCancellable is used when you need to ensure that certain code blocks
 * execute even if the coroutine is cancelled. This is useful for:
 * - Cleanup operations
 * - Logging
 * - Critical operations that must complete
 * - Resource release
 */

// Example 1: Basic non-cancellable cleanup
fun example1_NonCancellableCleanup() {
    runBlocking {
        val job = launch {
            try {
                println("Starting work...")
                delay(1000)
                println("Work in progress...")
                delay(1000)
                println("Work completed!")
            } finally {
                // This cleanup code will run even if the coroutine is cancelled
                withContext(NonCancellable) {
                    println("Cleanup: Closing resources...")
                    delay(500) // Even delay works here!
                    println("Cleanup: Resources closed successfully")
                }
            }
        }

        delay(500) // Cancel after 500ms
        job.cancel()
        job.join()
        println("Job cancelled, but cleanup completed")
    }
}

// Example 2: Non-cancellable code in try-finally
fun example2_TryFinallyWithNonCancellable() {
    runBlocking {
        val job = launch {
            try {
                println("Acquiring resource...")
                delay(1000)
                println("Using resource...")
                delay(1000)
            } catch (e: Exception) {
                println("Exception caught: ${e.message}")
            } finally {
                // Critical cleanup that must always execute
                withContext(NonCancellable) {
                    println("Finally block: Releasing resource...")
                    delay(300) // This delay won't be cancelled
                    println("Finally block: Resource released")
                }
            }
        }

        delay(800)
        job.cancel()
        job.join()
        println("Main: Job cancelled")
    }
}

// Example 3: Comparison - Cancellable vs Non-Cancellable
fun example3_CancellableVsNonCancellable() {
    runBlocking {
        println("=== Example 3: Cancellable vs Non-Cancellable ===\n")

        // Job 1: Cancellable code (will be interrupted)
        val job1 = launch {
            try {
                println("Job 1: Starting cancellable work...")
                delay(2000) // This will be cancelled
                println("Job 1: This won't print")
            } finally {
                println("Job 1: Finally block (cancellable)")
                delay(500) // This delay WILL be cancelled
                println("Job 1: This cleanup won't complete")
            }
        }

        // Job 2: Non-cancellable code (will complete)
        val job2 = launch {
            try {
                println("Job 2: Starting cancellable work...")
                delay(2000) // This will be cancelled
                println("Job 2: This won't print")
            } finally {
                println("Job 2: Finally block (non-cancellable)")
                withContext(NonCancellable) {
                    delay(500) // This delay WON'T be cancelled
                    println("Job 2: Cleanup completed successfully")
                }
            }
        }

        delay(100)
        println("\nCancelling both jobs...\n")
        job1.cancel()
        job2.cancel()

        job1.join()
        job2.join()

        println("\nBoth jobs cancelled")
    }
}

// Example 4: Practical use case - Database transaction cleanup
fun example4_DatabaseCleanup() {
    runBlocking {
        val job = launch {
            var transactionStarted = false
            try {
                println("Starting database transaction...")
                transactionStarted = true
                delay(1000) // Simulating database work
                println("Processing data...")
                delay(1000)
                println("Committing transaction...")
            } catch (e: Exception) {
                println("Error occurred: ${e.message}")
            } finally {
                // Always rollback if transaction was started
                if (transactionStarted) {
                    withContext(NonCancellable) {
                        println("Rolling back transaction (non-cancellable)...")
                        delay(300) // Simulating rollback operation
                        println("Transaction rolled back successfully")
                    }
                }
            }
        }

        delay(800)
        job.cancel()
        job.join()
    }
}

// Example 5: Logging critical information (non-cancellable)
fun example5_CriticalLogging() {
    runBlocking {
        val job = launch {
            try {
                println("Processing important task...")
                delay(2000)
                println("Task completed")
            } finally {
                // Always log the cancellation/completion
                withContext(NonCancellable) {
                    println("Logging critical information...")
                    delay(200) // Simulating log write
                    println("Critical log entry saved: Task ended at ${System.currentTimeMillis()}")
                }
            }
        }

        delay(500)
        job.cancel()
        job.join()
    }
}

// Example 6: File operation cleanup
fun example6_FileOperationCleanup() {
    runBlocking {
        val job = launch {
            var fileOpened = false
            try {
                println("Opening file...")
                fileOpened = true
                delay(1500) // Simulating file operations
                println("Writing to file...")
                delay(1000)
            } finally {
                // Always close the file, even if cancelled
                if (fileOpened) {
                    withContext(NonCancellable) {
                        println("Closing file (non-cancellable)...")
                        delay(400) // Simulating file close operation
                        println("File closed successfully")
                    }
                }
            }
        }

        delay(1000)
        job.cancel()
        job.join()
    }
}
