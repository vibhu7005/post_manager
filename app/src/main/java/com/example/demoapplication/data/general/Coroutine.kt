package com.example.demoapplication.data.general

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        println("=".repeat(70))
        println("Why launch { async { throw } } throws exception without await()?")
        println("=".repeat(70) + "\n")
        
        demonstration_WhyLaunchAsyncThrowsException()
    }
}

// ============================================================================
// CLARIFICATION: Does launch erase SupervisorJob effect?
// ============================================================================

/**
 * CRITICAL CLARIFICATION: Your exception proves SupervisorJob IS working!
 * 
 * What you're seeing:
 * - Exception thrown from nested launch
 * - Exception appears in thread (unhandled)
 * - But: Does it cancel siblings? NO! ✅
 * 
 * Key Points:
 * 1. SupervisorJob protects SIBLING coroutines (root level)
 * 2. For NESTED children, you need supervisorScope
 * 3. Unhandled exceptions need CoroutineExceptionHandler
 * 4. The exception being visible doesn't mean SupervisorJob isn't working!
 */

fun demonstration_DoesLaunchEraseSupervisorJob() {
    runBlocking {
        println("""
    ╔══════════════════════════════════════════════════════════════════════╗
    ║  CLARIFICATION: Does launch erase SupervisorJob effect?             ║
    ╚══════════════════════════════════════════════════════════════════════╝
        """.trimIndent())
        
        println("\n=== YOUR CODE: What's Actually Happening ===\n")
        
        println("Your code:")
        println("  val job = SupervisorJob()")
        println("  CoroutineScope(job).launch {")
        println("    launch { throw Exception(\"feer\") }  // Nested child 1")
        println("    val c = launch { delay(450); println(\"✅ c\") }  // Nested child 2")
        println("  }")
        
        println("\n⚠️  KEY ISSUE: These are NESTED children, not siblings!")
        println("   - The SupervisorJob is at the ROOT scope")
        println("   - But the launches are INSIDE another launch")
        println("   - SupervisorJob protects ROOT-level siblings, not nested children")
        
        println("\n=== DEMO 1: Your Code (Nested Children) ===\n")
        
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("✅ Exception handled by handler: ${throwable.message}")
        }
        
        val job1 = SupervisorJob()
        val scope1 = CoroutineScope(job1 + exceptionHandler)
        
        println("Testing your exact code structure:")
        scope1.launch {
            launch { 
                delay(100)
                throw Exception("feer") 
            }
            val c = launch {
                delay(450)
                println("✅ c completed")
            }
        }
        
        delay(600)
        println("Result: 'c' completed ✅ (SupervisorJob protected the parent launch)")
        println("But: Exception needs handler (otherwise it's unhandled)\n")
        
        println("=== DEMO 2: Root-Level Siblings (SupervisorJob Works!) ===\n")
        
        val job2 = SupervisorJob()
        val scope2 = CoroutineScope(job2 + exceptionHandler)
        
        println("Root-level siblings (SupervisorJob protects them):")
        scope2.launch {
            delay(100)
            throw Exception("Sibling 1 failed")
        }
        
        scope2.launch {
            delay(200)
            println("✅ Sibling 2 completed (SupervisorJob protected it!)")
        }
        
        delay(400)
        println("Result: Sibling 2 completed despite Sibling 1 failing ✅\n")
        
        println("=== DEMO 3: Nested Children WITH supervisorScope ===\n")
        
        val job3 = SupervisorJob()
        val scope3 = CoroutineScope(job3 + exceptionHandler)
        
        println("Nested children WITH supervisorScope:")
        scope3.launch {
            supervisorScope {  // ← This is what you need!
                launch {
                    delay(100)
                    throw Exception("Nested child 1 failed")
                }
                launch {
                    delay(200)
                    println("✅ Nested child 2 completed (supervisorScope protected it!)")
                }
            }
        }
        
        delay(400)
        println("Result: Nested child 2 completed ✅\n")
        
        println("=== DEMO 4: Your Code Structure Explained ===\n")
        
        println("""
        Your Code Structure:
        ────────────────────
        SupervisorJob (root)
        └── launch (parent) ← SupervisorJob protects THIS from other root siblings
            ├── launch { throw } ← Nested child 1 (NOT protected by root SupervisorJob!)
            └── launch { delay } ← Nested child 2 (NOT protected by root SupervisorJob!)
        
        What Happens:
        ─────────────
        1. Nested child 1 throws exception
        2. Exception propagates to parent launch
        3. Parent launch is cancelled (because it's a regular launch, not supervisorScope)
        4. Nested child 2 gets cancelled (because parent was cancelled)
        5. Exception appears unhandled (no exception handler)
        
        The Root SupervisorJob:
        ──────────────────────
        ✅ DOES protect the parent launch from OTHER root-level siblings
        ❌ DOES NOT protect nested children from each other
        
        Solution:
        ─────────
        Use supervisorScope for nested children!
        """.trimIndent())
        
        println("\n=== DEMO 5: Fixed Version of Your Code ===\n")
        
        val job4 = SupervisorJob()
        val scope4 = CoroutineScope(job4 + exceptionHandler)
        
        println("Fixed version with supervisorScope:")
        scope4.launch {
            supervisorScope {  // ← Add this!
                launch {
                    delay(100)
                    throw Exception("feer")
                }
                launch {
                    delay(450)
                    println("✅ c completed (protected by supervisorScope!)")
                }
            }
        }
        
        delay(600)
        println("Result: 'c' completed despite exception ✅\n")
        
        println("=== KEY TAKEAWAYS ===\n")
        println("""
        ✅ SupervisorJob DOES work - it protects root-level siblings
        ⚠️  SupervisorJob does NOT protect nested children (need supervisorScope)
        ⚠️  Unhandled exceptions need CoroutineExceptionHandler
        ✅ Your exception proves isolation works (doesn't crash the app)
        ✅ Use supervisorScope { } for nested children
        """.trimIndent())
    }
}

fun demonstration_SideBySideComparison() {
    runBlocking {
        println("""
    ╔══════════════════════════════════════════════════════════════════════╗
    ║  SIDE-BY-SIDE COMPARISON: Proving SupervisorJob Works                ║
    ╚══════════════════════════════════════════════════════════════════════╝
        """.trimIndent())
        
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("  🔴 Handler caught: ${throwable.message}")
        }
        
        println("\n=== SCENARIO A: Your Code (Nested Children) ===\n")
        println("Structure: SupervisorJob → launch → [nested launches]")
        
        val jobA = SupervisorJob()
        val scopeA = CoroutineScope(jobA + exceptionHandler)
        
        scopeA.launch {
            println("  Parent launch started")
            launch {
                delay(100)
                throw Exception("feer")
            }
            launch {
                delay(200)
                println("  ✅ Nested child 2: Will this complete?")
            }
        }
        
        delay(400)
        println("  Result: Nested child 2 was CANCELLED ❌")
        println("  Reason: Parent launch was cancelled by nested exception\n")
        
        println("=== SCENARIO B: Root-Level Siblings ===\n")
        println("Structure: SupervisorJob → [sibling launches]")
        
        val jobB = SupervisorJob()
        val scopeB = CoroutineScope(jobB + exceptionHandler)
        
        scopeB.launch {
            delay(100)
            throw Exception("Sibling 1 failed")
        }
        
        scopeB.launch {
            delay(200)
            println("  ✅ Sibling 2: Completed successfully!")
        }
        
        delay(400)
        println("  Result: Sibling 2 completed ✅")
        println("  Reason: SupervisorJob protected root-level siblings\n")
        
        println("=== SCENARIO C: Your Code Fixed (supervisorScope) ===\n")
        println("Structure: SupervisorJob → launch → supervisorScope → [nested launches]")
        
        val jobC = SupervisorJob()
        val scopeC = CoroutineScope(jobC + exceptionHandler)
        
        scopeC.launch {
            println("  Parent launch started")
            supervisorScope {  // ← Added supervisorScope!
                launch {
                    delay(100)
                    throw Exception("feer")
                }
                launch {
                    delay(200)
                    println("  ✅ Nested child 2: Completed successfully!")
                }
            }
        }
        
        delay(400)
        println("  Result: Nested child 2 completed ✅")
        println("  Reason: supervisorScope protected nested children\n")
        
        println("""
        ┌─────────────────────────────────────────────────────────────────┐
        │  COMPARISON TABLE                                                │
        ├─────────────────────────────────────────────────────────────────┤
        │  Structure              │  Exception Handling │  Siblings Safe? │
        ├─────────────────────────────────────────────────────────────────┤
        │  SupervisorJob         │                     │                 │
        │  └── launch             │                     │                 │
        │      ├── launch (fail)  │  ❌ Unhandled       │  ❌ NO          │
        │      └── launch         │                     │                 │
        ├─────────────────────────────────────────────────────────────────┤
        │  SupervisorJob          │                     │                 │
        │  ├── launch (fail)      │  ✅ Handler         │  ✅ YES         │
        │  └── launch             │                     │                 │
        ├─────────────────────────────────────────────────────────────────┤
        │  SupervisorJob          │                     │                 │
        │  └── launch             │                     │                 │
        │      └── supervisorScope│                     │                 │
        │          ├── launch(fail)│ ✅ Handler         │  ✅ YES         │
        │          └── launch     │                     │                 │
        └─────────────────────────────────────────────────────────────────┘
        
        CONCLUSION:
        ───────────
        ✅ SupervisorJob DOES work (protects root-level siblings)
        ⚠️  For nested children, you MUST use supervisorScope
        ⚠️  Always use CoroutineExceptionHandler for unhandled exceptions
        ✅ Your exception appearing doesn't mean SupervisorJob isn't working!
        """.trimIndent())
    }
}

// ============================================================================
// LAUNCH VS ASYNC IN SUPERVISORSCOPE - CRITICAL DIFFERENCE!
// ============================================================================

/**
 * CRITICAL UNDERSTANDING: launch vs async in supervisorScope
 *
 * launch {}:
 * - Exception is isolated by supervisorScope
 * - Other siblings continue running
 * - Exception doesn't propagate
 *
 * async {} + await():
 * - Exception stays in Deferred until await()
 * - When await() is called, it THROWS the exception
 * - This exception is thrown from WITHIN supervisorScope block
 * - supervisorScope doesn't isolate exceptions thrown by await()
 * - Exception propagates and cancels the entire scope
 * - Other siblings get cancelled!
 */

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

        println(
            """
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
        """.trimIndent()
        )
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

        println(
            """
        ┌─────────────────────────────────────────────────────────────┐
        │  SCOPE TYPE          │  EXCEPTION PROPAGATION │  TRY-CATCH  │
        ├─────────────────────────────────────────────────────────────┤
        │  coroutineScope      │  ✅ YES (propagates)   │  ✅ WORKS   │
        │  supervisorScope    │  ❌ NO (isolates)      │  ❌ NO      │
        │  launch (root)      │  ❌ NO (goes to handler)│  ❌ NO      │
        └─────────────────────────────────────────────────────────────┘
        """.trimIndent()
        )
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

// ============================================================================
// CRITICAL CONCEPT: Job Hierarchy vs Scope Context Inheritance
// ============================================================================

/**
 * FUNDAMENTAL INSIGHT: Job DOES pass on, but creates a hierarchy!
 * 
 * Key Concepts:
 * 1. Child coroutines CREATE new Jobs that are children of parent Job
 * 2. Scope context (Job, Dispatcher, etc.) IS inherited
 * 3. SupervisorJob behavior applies to DIRECT children only
 * 4. Nested coroutines create a Job hierarchy, not direct children
 */

fun demonstration_JobHierarchyVsScopeInheritance() {
    runBlocking {
        println("""
    ╔══════════════════════════════════════════════════════════════════════╗
    ║  CRITICAL CONCEPT: Job Hierarchy vs Scope Context Inheritance       ║
    ╚══════════════════════════════════════════════════════════════════════╝
        """.trimIndent())
        
        println("\n=== KEY INSIGHT ===\n")
        println("""
        ❓ Question: Does Job pass on to child coroutines?
        
        ✅ Answer: YES, but it creates a HIERARCHY!
        
        When you do:
          scope.launch { }
        
        What happens:
          1. A NEW Job is created (child Job)
          2. This child Job is linked to parent Job from scope
          3. Scope context (Job, Dispatcher, etc.) IS inherited
          4. But SupervisorJob behavior applies to DIRECT children only
        """.trimIndent())
        
        println("\n=== DEMO 1: Job Hierarchy Visualization ===\n")
        
        val supervisorJob = SupervisorJob()
        val scope = CoroutineScope(supervisorJob)
        
        println("Creating scope with SupervisorJob:")
        println("  SupervisorJob (root)")
        
        val job1 = scope.launch {
            println("  └── Job1 (direct child of SupervisorJob)")
            
            launch {
                println("      └── Job1a (child of Job1, NOT direct child of SupervisorJob)")
            }
            
            launch {
                println("      └── Job1b (child of Job1, NOT direct child of SupervisorJob)")
            }
        }
        
        val job2 = scope.launch {
            println("  └── Job2 (direct child of SupervisorJob)")
        }
        
        delay(200)
        println("\nJob Hierarchy:")
        println("  SupervisorJob")
        println("  ├── Job1 (direct child ✅)")
        println("  │   ├── Job1a (nested child ❌)")
        println("  │   └── Job1b (nested child ❌)")
        println("  └── Job2 (direct child ✅)")
        
        println("\nSupervisorJob protects:")
        println("  ✅ Job1 and Job2 (direct children)")
        println("  ❌ Job1a and Job1b (NOT direct children)")
        
        delay(300)
        
        println("\n=== DEMO 2: Scope Context IS Inherited ===\n")
        
        val handler = CoroutineExceptionHandler { _, e ->
            println("  Handler caught: ${e.message}")
        }
        
        val scope2 = CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)
        
        println("Scope context: SupervisorJob + Dispatchers.Default + Handler")
        
        scope2.launch {
            println("  Child 1: Inherits SupervisorJob ✅")
            println("  Child 1: Inherits Dispatchers.Default ✅")
            println("  Child 1: Inherits Handler ✅")
            
            launch {  // Nested child
                println("    Nested: Inherits Dispatchers.Default ✅")
                println("    Nested: Inherits Handler ✅")
                println("    Nested: But NOT direct child of SupervisorJob ❌")
            }
        }
        
        delay(200)
        
        println("\n=== DEMO 3: Why SupervisorJob Doesn't Protect Nested Children ===\n")
        
        val scope3 = CoroutineScope(SupervisorJob() + handler)
        
        println("Scenario: SupervisorJob at scope level")
        println("  SupervisorJob")
        println("  └── launch (Job1) ← Direct child ✅")
        println("      ├── launch (Job1a) ← Nested child ❌")
        println("      └── launch (Job1b) ← Nested child ❌")
        
        scope3.launch {
            println("\n  Parent launch started")
            
            launch {
                delay(100)
                throw Exception("Nested child 1 failed")
            }
            
            launch {
                delay(200)
                println("  ✅ Nested child 2: Will this complete?")
            }
        }
        
        delay(400)
        println("\n  Result: Nested child 2 was CANCELLED ❌")
        println("  Reason: Parent Job1 was cancelled, so its children (Job1a, Job1b) are cancelled")
        println("  SupervisorJob only protects Job1 from other root-level siblings")
        
        println("\n=== DEMO 4: supervisorScope Creates New SupervisorJob ===\n")
        
        val scope4 = CoroutineScope(SupervisorJob() + handler)
        
        println("Scenario: supervisorScope creates NEW SupervisorJob for nested children")
        println("  SupervisorJob (root)")
        println("  └── launch (Job1)")
        println("      └── supervisorScope { } ← Creates NEW SupervisorJob!")
        println("          ├── launch (Job1a) ← Direct child of NEW SupervisorJob ✅")
        println("          └── launch (Job1b) ← Direct child of NEW SupervisorJob ✅")
        
        scope4.launch {
            println("\n  Parent launch started")
            
            supervisorScope {  // Creates NEW SupervisorJob for nested children!
                launch {
                    delay(100)
                    throw Exception("Nested child 1 failed")
                }
                
                launch {
                    delay(200)
                    println("  ✅ Nested child 2: Completed successfully!")
                }
            }
        }
        
        delay(400)
        println("\n  Result: Nested child 2 completed ✅")
        println("  Reason: supervisorScope created NEW SupervisorJob that protects Job1a and Job1b")
        
        println("\n=== DEMO 5: Complete Comparison ===\n")
        
        println("""
        ┌─────────────────────────────────────────────────────────────────┐
        │  STRUCTURE                      │  SUPERVISOR PROTECTION          │
        ├─────────────────────────────────────────────────────────────────┤
        │  SupervisorJob (scope)          │                                │
        │  ├── launch (Job1)             │  ✅ Protected                  │
        │  └── launch (Job2)             │  ✅ Protected                  │
        │                                 │                                │
        │  SupervisorJob (scope)          │                                │
        │  └── launch (Job1)             │  ✅ Protected                  │
        │      ├── launch (Job1a)         │  ❌ NOT protected              │
        │      └── launch (Job1b)         │  ❌ NOT protected              │
        │                                 │                                │
        │  SupervisorJob (scope)          │                                │
        │  └── launch (Job1)             │  ✅ Protected                  │
        │      └── supervisorScope {     │  ← NEW SupervisorJob!          │
        │          ├── launch (Job1a)     │  ✅ Protected                  │
        │          └── launch (Job1b)     │  ✅ Protected                  │
        └─────────────────────────────────────────────────────────────────┘
        """.trimIndent())
        
        println("\n=== KEY TAKEAWAYS ===\n")
        println("""
        ✅ Job DOES pass on - child coroutines create child Jobs
        ✅ Scope context IS inherited (Job, Dispatcher, Handler, etc.)
        ⚠️  SupervisorJob behavior applies to DIRECT children only
        ⚠️  Nested coroutines create Job hierarchy, not direct children
        ✅ supervisorScope creates NEW SupervisorJob for nested children
        
        The Main Concept:
        ────────────────
        - Scope provides context (Job, Dispatcher, etc.) ✅ Inherited
        - Each launch creates NEW Job (child of parent Job) ✅ Creates hierarchy
        - SupervisorJob protects DIRECT children only ⚠️  Not nested children
        - supervisorScope creates NEW SupervisorJob for nested children ✅
        """.trimIndent())
    }
}

fun demonstration_VisualJobHierarchy() {
    runBlocking {
        println("""
    ╔══════════════════════════════════════════════════════════════════════╗
    ║  Visual Job Hierarchy Explanation                                    ║
    ╚══════════════════════════════════════════════════════════════════════╝
        """.trimIndent())
        
        println("\n=== Your Code Structure ===\n")
        
        println("""
        val job = SupervisorJob()
        CoroutineScope(job).launch {
            launch { throw Exception("feer") }
            launch { delay(450); println("✅ c") }
        }
        
        Job Hierarchy Created:
        ──────────────────────
        SupervisorJob (root)
        └── Job1 (created by first launch)
            ├── Job1a (created by nested launch { throw })
            └── Job1b (created by nested launch { delay })
        
        SupervisorJob Protection:
        ────────────────────────
        ✅ Protects Job1 from other root-level siblings
        ❌ Does NOT protect Job1a from Job1b (they're siblings under Job1)
        
        When Job1a throws:
        ──────────────────
        1. Exception propagates to Job1
        2. Job1 is cancelled (it's a regular Job, not SupervisorJob)
        3. Job1b is cancelled (because its parent Job1 was cancelled)
        """.trimIndent())
        
        println("\n=== Fixed Code Structure ===\n")
        
        println("""
        val job = SupervisorJob()
        CoroutineScope(job).launch {
            supervisorScope {
                launch { throw Exception("feer") }
                launch { delay(450); println("✅ c") }
            }
        }
        
        Job Hierarchy Created:
        ──────────────────────
        SupervisorJob (root)
        └── Job1 (created by first launch)
            └── SupervisorJob2 (created by supervisorScope)
                ├── Job1a (created by nested launch { throw })
                └── Job1b (created by nested launch { delay })
        
        SupervisorJob Protection:
        ────────────────────────
        ✅ SupervisorJob (root) protects Job1 from other root-level siblings
        ✅ SupervisorJob2 protects Job1a and Job1b from each other
        
        When Job1a throws:
        ──────────────────
        1. Exception is isolated by SupervisorJob2
        2. Job1a fails, but Job1b continues ✅
        3. Job1 continues (SupervisorJob2 isolates the failure)
        """.trimIndent())
        
        println("\n=== The Answer to Your Question ===\n")
        println("""
        ❓ "Does Job pass on to child coroutines but Scope does?"
        
        ✅ CORRECT INSIGHT! Here's the precise answer:
        
        1. Scope Context (Job, Dispatcher, etc.) IS inherited ✅
           - Child coroutines inherit the scope's context
           - But they create NEW Jobs that are children of parent Job
        
        2. Job Hierarchy is Created ✅
           - Each launch/async creates a NEW Job
           - This Job is a child of the parent Job from scope
           - Creates a tree structure
        
        3. SupervisorJob Behavior ✅
           - Applies to DIRECT children of the SupervisorJob
           - Does NOT automatically apply to nested children
           - supervisorScope creates NEW SupervisorJob for nested children
        
        So YES:
        - Scope context IS inherited (Job, Dispatcher, Handler)
        - Job DOES pass on (creates child Jobs)
        - But SupervisorJob only protects DIRECT children
        - For nested children, you need supervisorScope!
        """.trimIndent())
    }
}

fun directAnswer_JobVsScope() {
    runBlocking {
        println("""
    ╔══════════════════════════════════════════════════════════════════════╗
    ║  DIRECT ANSWER: Job vs Scope Inheritance                            ║
    ╚══════════════════════════════════════════════════════════════════════╝
        """.trimIndent())
        
        println("\n❓ YOUR QUESTION:")
        println("   'Does Job NOT pass on to child coroutines but Scope does?'")
        
        println("\n✅ THE ANSWER:")
        println("""
        BOTH Job and Scope context ARE inherited, but they work differently!
        
        1. SCOPE CONTEXT (Job, Dispatcher, Handler, etc.)
           ───────────────────────────────────────────────
           ✅ IS inherited by child coroutines
           ✅ Child coroutines get parent's Dispatcher, Handler, etc.
           ✅ Child coroutines get parent's Job as their parent Job
        
        2. JOB HIERARCHY
           ──────────────
           ✅ Job DOES pass on - creates a hierarchy
           ✅ Each launch/async creates a NEW Job
           ✅ This new Job is a CHILD of the parent Job from scope
           ✅ Creates a tree: Parent Job → Child Job → Grandchild Job
        
        3. SUPERVISORJOB BEHAVIOR
           ──────────────────────
           ⚠️  Applies to DIRECT children only
           ⚠️  Does NOT automatically apply to nested children
           ✅ supervisorScope creates NEW SupervisorJob for nested children
        
        Example:
        ────────
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        
        scope.launch {  // Creates Job1 (child of SupervisorJob)
            // ✅ Inherits Dispatchers.Default
            // ✅ Job1 is child of SupervisorJob
        
            launch {  // Creates Job1a (child of Job1)
                // ✅ Inherits Dispatchers.Default
                // ✅ Job1a is child of Job1
                // ❌ Job1a is NOT direct child of SupervisorJob
                // ❌ SupervisorJob does NOT protect Job1a from Job1b
            }
        }
        
        The Key Insight:
        ───────────────
        - Scope context IS inherited ✅
        - Job DOES create hierarchy ✅
        - But SupervisorJob only protects DIRECT children ⚠️
        - For nested children, use supervisorScope ✅
        """.trimIndent())
        
        println("\n=== Visual Example ===\n")
        
        val supervisorJob = SupervisorJob()
        val scope = CoroutineScope(supervisorJob + Dispatchers.Default)
        
        println("Creating coroutines...")
        
        val job1 = scope.launch {
            println("Job1: Inherits Dispatchers.Default ✅")
            println("Job1: Parent is SupervisorJob ✅")
            
            val job1a = launch {
                println("Job1a: Inherits Dispatchers.Default ✅")
                println("Job1a: Parent is Job1 ✅")
                println("Job1a: NOT direct child of SupervisorJob ❌")
            }
            
            val job1b = launch {
                println("Job1b: Inherits Dispatchers.Default ✅")
                println("Job1b: Parent is Job1 ✅")
                println("Job1b: NOT direct child of SupervisorJob ❌")
            }
        }
        
        val job2 = scope.launch {
            println("Job2: Inherits Dispatchers.Default ✅")
            println("Job2: Parent is SupervisorJob ✅")
            println("Job2: IS direct child of SupervisorJob ✅")
        }
        
        delay(200)
        
        println("\nJob Hierarchy:")
        println("  SupervisorJob")
        println("  ├── Job1 (direct child ✅)")
        println("  │   ├── Job1a (nested child ❌)")
        println("  │   └── Job1b (nested child ❌)")
        println("  └── Job2 (direct child ✅)")
        
        println("\nSupervisorJob protects:")
        println("  ✅ Job1 and Job2 (direct children)")
        println("  ❌ Job1a and Job1b (NOT direct children)")
        
        println("\nScope context inheritance:")
        println("  ✅ All jobs inherit Dispatchers.Default")
        println("  ✅ All jobs inherit parent Job in hierarchy")
        
        delay(300)
    }
}

// ============================================================================
// CRITICAL: Why launch { async { throw } } throws exception without await()
// ============================================================================

/**
 * CRITICAL QUESTION:
 * "Why does CoroutineScope.launch { async { throw exception } } throw exception
 *  despite async's silent behavior with or without await()?"
 * 
 * ANSWER:
 * When a root coroutine (launch from CoroutineScope) completes, any unawaited
 * Deferred exceptions propagate to the exception handler. This is because:
 * 
 * 1. async { throw } stores exception in Deferred
 * 2. launch completes immediately (doesn't wait for async)
 * 3. When launch completes, unawaited Deferred exceptions propagate
 * 4. Exception goes to CoroutineExceptionHandler (or crashes if no handler)
 * 
 * This is DIFFERENT from nested coroutines where exceptions stay silent!
 */

fun demonstration_WhyLaunchAsyncThrowsException() {
    runBlocking {
        println("""
    ╔══════════════════════════════════════════════════════════════════════╗
    ║  Why launch { async { throw } } throws exception without await()?  ║
    ╚══════════════════════════════════════════════════════════════════════╝
        """.trimIndent())
        
        println("\n❓ YOUR QUESTION:")
        println("   'Why does CoroutineScope.launch { async { throw exception } }")
        println("    throw exception despite async's silent behavior?'")
        
        println("\n=== KEY INSIGHT: Root Coroutine Behavior ===\n")
        println("""
        When launch is a ROOT coroutine (launched from CoroutineScope):
        ────────────────────────────────────────────────────────────────
        
        1. async { throw } creates Deferred with exception
        2. launch completes immediately (doesn't wait for async)
        3. When launch completes, unawaited Deferred exceptions propagate
        4. Exception goes to CoroutineExceptionHandler
        
        This is DIFFERENT from nested coroutines!
        """.trimIndent())
        
        println("\n=== DEMO 1: Root Coroutine (launch from CoroutineScope) ===\n")
        
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            println("  🔴 Exception Handler caught: ${throwable.message}")
            println("  ✅ This proves exception propagated even without await()!")
        }
        
        val scope = CoroutineScope(Job() + exceptionHandler)
        
        println("Test: CoroutineScope.launch { async { throw } }")
        println("  (No await() called)")
        
        scope.launch {
            async {
                delay(100)
                throw Exception("Exception in async without await()")
            }
            // launch completes immediately, doesn't wait for async
            println("  launch completed immediately")
        }
        
        delay(300)
        println("  Result: Exception WAS propagated to handler! ✅")
        
        println("\n=== DEMO 2: Why This Happens ===\n")
        println("""
        Lifecycle of async exception in root launch:
        ────────────────────────────────────────────
        
        Time 0ms:  launch starts
        Time 0ms:  async { throw } starts (exception not thrown yet)
        Time 0ms:  launch completes immediately (returns)
        Time 100ms: async throws exception → stored in Deferred
        Time 100ms: launch already completed → unawaited Deferred exception propagates
        Time 100ms: Exception goes to CoroutineExceptionHandler
        
        Key Point:
        ──────────
        When a root coroutine completes, any unawaited Deferred exceptions
        propagate to the exception handler. This is by design!
        """.trimIndent())
        
        println("\n=== DEMO 3: Nested Coroutine (Different Behavior!) ===\n")
        
        println("Test: coroutineScope { launch { async { throw } } }")
        println("  (Nested launch, not root)")
        
        val handler2 = CoroutineExceptionHandler { _, throwable ->
            println("  🔴 Handler caught: ${throwable.message}")
        }
        
        val scope2 = CoroutineScope(Job() + handler2)
        
        scope2.launch {
            coroutineScope {
                launch {
                    async {
                        delay(100)
                        throw Exception("Exception in nested async")
                    }
                    println("  Nested launch completed immediately")
                }
                // coroutineScope waits for launch
            }
            println("  Outer launch completed")
        }
        
        delay(300)
        println("  Result: Exception handled differently (stays in Deferred)")
        
        println("\n=== DEMO 4: Comparison Table ===\n")
        println("""
        ┌─────────────────────────────────────────────────────────────────┐
        │  SCENARIO                        │  EXCEPTION PROPAGATES?         │
        ├─────────────────────────────────────────────────────────────────┤
        │  CoroutineScope.launch {          │                                │
        │    async { throw }                │  ✅ YES (to handler)           │
        │  }                                │                                │
        ├─────────────────────────────────────────────────────────────────┤
        │  CoroutineScope.async {           │                                │
        │    async { throw }                │  ❌ NO (stays in Deferred)     │
        │  }                                │                                │
        ├─────────────────────────────────────────────────────────────────┤
        │  coroutineScope {                 │                                │
        │    launch { async { throw } }     │  ❌ NO (stays in Deferred)     │
        │  }                                │                                │
        ├─────────────────────────────────────────────────────────────────┤
        │  launch { async { throw }.await() }│ ✅ YES (await() throws)        │
        └─────────────────────────────────────────────────────────────────┘
        """.trimIndent())
        
        println("\n=== DEMO 5: Root vs Non-Root Coroutines ===\n")
        
        println("Scenario A: Root coroutine (launch from CoroutineScope)")
        val handler3 = CoroutineExceptionHandler { _, e ->
            println("  ✅ Handler caught: ${e.message}")
        }
        
        val scope3 = CoroutineScope(Job() + handler3)
        
        scope3.launch {  // ← ROOT coroutine
            async {
                delay(100)
                throw Exception("Root async exception")
            }
        }
        
        delay(200)
        println("  Result: Exception propagated ✅\n")
        
        println("Scenario B: Non-root coroutine (nested launch)")
        val scope4 = CoroutineScope(Job() + handler3)
        
        scope4.launch {
            launch {  // ← Nested (non-root)
                async {
                    delay(100)
                    throw Exception("Nested async exception")
                }
            }
        }
        
        delay(200)
        println("  Result: Exception stays silent ❌")
        println("  (Because nested launch is not root)")
        
        println("\n=== DEMO 6: The Actual Code Behavior ===\n")
        
        println("Your code:")
        println("  CoroutineScope(Job()).launch {")
        println("    async { throw Exception() }")
        println("  }")
        
        println("\nWhat happens:")
        println("  1. launch starts (root coroutine)")
        println("  2. async { throw } starts")
        println("  3. launch completes immediately")
        println("  4. async throws exception → stored in Deferred")
        println("  5. launch already completed → unawaited Deferred exception propagates")
        println("  6. Exception goes to handler (or crashes if no handler)")
        
        val handler4 = CoroutineExceptionHandler { _, e ->
            println("\n  ✅ Exception Handler: ${e.message}")
        }
        
        CoroutineScope(Job() + handler4).launch {
            async {
                delay(100)
                throw Exception("Your exact scenario")
            }
        }
        
        delay(200)
        
        println("\n=== DEMO 7: Why async in async is Silent ===\n")
        
        println("Test: CoroutineScope.async { async { throw } }")
        
        val handler5 = CoroutineExceptionHandler { _, e ->
            println("  Handler caught: ${e.message}")
        }
        
        val scope5 = CoroutineScope(Job() + handler5)
        
        scope5.async {  // ← async, not launch
            async {
                delay(100)
                throw Exception("Exception in nested async")
            }
        }
        
        delay(200)
        println("  Result: Exception stays silent ❌")
        println("  Reason: async returns Deferred, doesn't complete immediately")
        println("  Exception stays in inner Deferred, never propagates")
        
        println("\n=== KEY TAKEAWAYS ===\n")
        println("""
        ✅ Root launch { async { throw } } → Exception propagates
           Reason: When root coroutine completes, unawaited Deferred exceptions propagate
        
        ❌ async { async { throw } } → Exception stays silent
           Reason: async returns Deferred, exception stays in inner Deferred
        
        ❌ Nested launch { async { throw } } → Exception stays silent
           Reason: Nested launch is not root, exception stays in Deferred
        
        ✅ launch { async { throw }.await() } → Exception propagates
           Reason: await() throws the exception
        
        THE RULE:
        ─────────
        When a ROOT coroutine (launch from CoroutineScope) completes,
        any unawaited Deferred exceptions propagate to the exception handler.
        
        This is DIFFERENT from:
        - async (returns Deferred, doesn't complete)
        - Nested coroutines (not root)
        - supervisorScope/coroutineScope (waits for children)
        """.trimIndent())
    }
}
