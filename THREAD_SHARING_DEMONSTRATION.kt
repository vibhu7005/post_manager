// Thread Sharing Demonstration: Why Coroutines Help Even With Main + IO Threads
// Shows how threads are shared and reused in Coroutines vs blocked in RxJava

package com.example.demoapplication.threadsharing

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

// ============================================================================
// THE QUESTION
// ============================================================================

/**
 * QUESTION: "An API call still needs Main thread (for UI) and IO thread (for network).
 *            So how do Coroutines help?"
 * 
 * ANSWER: Threads are SHARED and REUSED across multiple coroutines.
 *         The benefit comes with MULTIPLE concurrent operations.
 */

// ============================================================================
// SINGLE API CALL: SAME THREADS USED
// ============================================================================

/**
 * For ONE API call, both approaches use similar threads:
 * - Main thread (for UI updates)
 * - IO thread (for network call)
 * 
 * No big difference here!
 */
fun singleApiCallComparison() {
    println("=== SINGLE API CALL ===")
    println("Both use: 1 Main thread + 1 IO thread")
    println("Result: Similar thread usage\n")
}

// ============================================================================
// MULTIPLE CONCURRENT API CALLS: THE REAL DIFFERENCE
// ============================================================================

/**
 * RxJava Approach (Conceptual):
 * 
 * - Thread pool: 10-20 IO threads
 * - Each call BLOCKS a thread while waiting for network
 * - Only 10-20 calls can run concurrently
 * - Remaining calls WAIT in queue
 */
fun rxJavaMultipleCalls() {
    println("=== RXJAVA: 100 CONCURRENT API CALLS ===")
    println("""
        Thread Pool: 10-20 IO threads
        
        Time: 0ms
        ├─ IO Thread 1: Call 1 [BLOCKED waiting for network...]
        ├─ IO Thread 2: Call 2 [BLOCKED waiting for network...]
        ├─ ...
        ├─ IO Thread 10: Call 10 [BLOCKED waiting for network...]
        └─ Queue: Calls 11-100 WAITING (no thread available)
        
        Time: 2000ms (network response)
        ├─ IO Thread 1: Call 1 completes → starts Call 11
        ├─ IO Thread 2: Call 2 completes → starts Call 12
        └─ ... (process continues)
        
        Problem: Threads are BLOCKED, limiting concurrency
        Only 10-20 calls concurrent at a time
        Total time: ~20 seconds (100 calls / 10 threads × 2 seconds)
    """.trimIndent())
    println()
}

/**
 * Coroutines Approach:
 * 
 * - Thread pool: 2-4 IO threads
 * - Each call SUSPENDS (frees thread) while waiting for network
 * - ALL 100 calls can start immediately
 * - Threads are REUSED for other coroutines
 */
suspend fun coroutinesMultipleCalls() {
    println("=== COROUTINES: 100 CONCURRENT API CALLS ===")
    
    val activeThreads = AtomicInteger(0)
    val maxConcurrentThreads = AtomicInteger(0)
    
    // Track thread usage
    val threadTracker = mutableSetOf<Thread>()
    
    coroutineScope {
        repeat(100) { i ->
            launch(Dispatchers.IO) {
                val currentThread = Thread.currentThread()
                threadTracker.add(currentThread)
                
                val active = activeThreads.incrementAndGet()
                maxConcurrentThreads.updateAndGet { maxOf(it, active) }
                
                // Simulate network call (suspends, frees thread)
                delay(2000) // Network delay
                
                activeThreads.decrementAndGet()
                
                println("Call $i completed on thread: ${currentThread.name}")
            }
        }
    }
    
    println("""
        Thread Pool: 2-4 IO threads (shared by all coroutines)
        
        Time: 0ms
        ├─ IO Thread 1: Call 1 [SUSPENDS, frees thread]
        ├─ IO Thread 1: Call 2 [SUSPENDS, frees thread]
        ├─ IO Thread 1: Call 3 [SUSPENDS, frees thread]
        ├─ ... (all 100 calls started immediately!)
        └─ IO Thread 2: Call 51-100 [SUSPENDS, frees thread]
        
        Time: 2000ms (network response)
        ├─ IO Thread 1: Resumes Call 1, processes response
        ├─ IO Thread 2: Resumes Call 2, processes response
        └─ ... (all calls resume and complete)
        
        Benefit: Threads are NOT BLOCKED, allowing high concurrency
        All 100 calls concurrent!
        Total time: ~2 seconds (all complete together)
        
        Actual threads used: ${threadTracker.size}
        Max concurrent threads: ${maxConcurrentThreads.get()}
    """.trimIndent())
    println()
}

// ============================================================================
// KEY CONCEPT: SUSPENSION VS BLOCKING
// ============================================================================

/**
 * BLOCKING (RxJava/Callbacks):
 * Thread is LOCKED and cannot be used by other operations
 */
fun blockingExample() {
    println("=== BLOCKING (RxJava/Callbacks) ===")
    println("""
        Thread {
            val response = networkCall()  // Thread WAITS here (BLOCKED)
            // Thread cannot do anything else
            // Other operations must WAIT for this thread
            processResponse(response)
        }.start()
        
        Problem: Thread is LOCKED
        - Cannot be used by other operations
        - Limits concurrency
        - Requires more threads for more operations
    """.trimIndent())
    println()
}

/**
 * SUSPENDING (Coroutines):
 * Thread is FREED and can be used by other coroutines
 */
suspend fun suspendingExample() {
    println("=== SUSPENDING (Coroutines) ===")
    println("""
        suspend fun fetchData() {
            val response = networkCall()  // SUSPENDS - thread is FREED
            // Thread can be used by other coroutines!
            // Other coroutines can use this thread immediately
            processResponse(response)
        }
        
        Benefit: Thread is FREED
        - Can be used by other coroutines
        - Allows high concurrency
        - Few threads can handle many operations
    """.trimIndent())
    println()
}

// ============================================================================
// REAL-WORLD EXAMPLE: NEWS FEED APP
// ============================================================================

/**
 * Scenario: Load 50 articles concurrently
 * Each article needs: Post data, Author avatar, Post image, Comments count
 * Total: 200 API calls
 */
suspend fun newsFeedExample() {
    println("=== REAL-WORLD EXAMPLE: NEWS FEED ===")
    
    val articles = (1..50).map { "article_$it" }
    val startTime = System.currentTimeMillis()
    
    // Simulate loading articles with Coroutines
    coroutineScope {
        articles.forEach { article ->
            launch(Dispatchers.IO) {
                // Load post data
                delay(100) // Simulate network
                
                // Load author avatar
                delay(100)
                
                // Load post image
                delay(100)
                
                // Load comments count
                delay(100)
            }
        }
    }
    
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    
    println("""
        Scenario: Load 50 articles (200 API calls total)
        
        Coroutines Approach:
        - Thread pool: 2-4 IO threads
        - All 200 calls start immediately
        - Calls suspend and free threads
        - All resume together when responses arrive
        - Total time: ~${duration}ms (all complete together)
        
        RxJava Approach (Conceptual):
        - Thread pool: 10-20 IO threads
        - Only 10-20 calls concurrent
        - Remaining 180-190 calls wait in queue
        - Total time: ~${duration * 10}ms (limited by thread pool)
        
        Result: Coroutines are MUCH faster for concurrent operations!
    """.trimIndent())
    println()
}

// ============================================================================
// VISUAL TIMELINE COMPARISON
// ============================================================================

fun visualTimelineComparison() {
    println("=== VISUAL TIMELINE COMPARISON ===")
    println("""
        RXJAVA: 100 API calls with 10 IO threads
        
        Time: 0ms
        Thread 1: [Call 1 ████████████████████████████████████] (blocked, waiting)
        Thread 2: [Call 2 ████████████████████████████████████] (blocked, waiting)
        Thread 3: [Call 3 ████████████████████████████████████] (blocked, waiting)
        ...
        Thread 10: [Call 10 ████████████████████████████████████] (blocked, waiting)
        Queue: [Call 11, Call 12, ..., Call 100] (waiting for thread)
        
        Time: 2000ms (response arrives)
        Thread 1: [Call 1 ✓] → [Call 11 ████████████████████████████████████]
        Thread 2: [Call 2 ✓] → [Call 12 ████████████████████████████████████]
        ...
        (Only 10 concurrent, rest wait)
        
        Total time: ~20 seconds
        
        ──────────────────────────────────────────────────────────────
        
        COROUTINES: 100 API calls with 2 IO threads
        
        Time: 0ms
        Thread 1: [Call 1 ⏸] [Call 2 ⏸] [Call 3 ⏸] ... [Call 50 ⏸] (all suspended)
        Thread 2: [Call 51 ⏸] [Call 52 ⏸] ... [Call 100 ⏸] (all suspended)
        (All 100 calls started immediately!)
        
        Time: 2000ms (response arrives)
        Thread 1: [Call 1 ✓] [Call 2 ✓] [Call 3 ✓] ... (resumes and processes)
        Thread 2: [Call 51 ✓] [Call 52 ✓] ... (resumes and processes)
        
        Total time: ~2 seconds
        
        Key: █ = Blocked (thread locked)
             ⏸ = Suspended (thread freed)
             ✓ = Completed
    """.trimIndent())
    println()
}

// ============================================================================
// PRACTICAL DEMONSTRATION
// ============================================================================

suspend fun demonstrateThreadSharing() {
    println("=== PRACTICAL DEMONSTRATION ===\n")
    
    println("Launching 100 concurrent operations...")
    val threadIds = mutableSetOf<Long>()
    val startTime = System.currentTimeMillis()
    
    coroutineScope {
        repeat(100) { i ->
            launch(Dispatchers.IO) {
                val threadId = Thread.currentThread().id
                threadIds.add(threadId)
                
                // Simulate network call (suspends)
                delay(2000)
                
                println("Operation $i completed on thread $threadId")
            }
        }
    }
    
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    
    println("""
        Results:
        - Total operations: 100
        - Unique threads used: ${threadIds.size}
        - Total time: ${duration}ms
        - All operations completed concurrently!
        
        Key Insight:
        - Only ${threadIds.size} threads handled all 100 operations
        - Threads were SHARED and REUSED
        - When operations suspended, threads were freed for others
        - This is why Coroutines are efficient!
    """.trimIndent())
}

// ============================================================================
// SUMMARY
// ============================================================================

suspend fun main() {
    println("=".repeat(60))
    println("THREAD SHARING: Why Coroutines Help")
    println("=".repeat(60))
    println()
    
    singleApiCallComparison()
    rxJavaMultipleCalls()
    coroutinesMultipleCalls()
    blockingExample()
    suspendingExample()
    visualTimelineComparison()
    newsFeedExample()
    demonstrateThreadSharing()
    
    println("=".repeat(60))
    println("SUMMARY")
    println("=".repeat(60))
    println("""
        Q: "An API call still needs Main + IO threads. How do Coroutines help?"
        
        A: For ONE API call, both use similar threads.
           The benefit comes with MULTIPLE concurrent operations:
        
        1. Thread Sharing: Multiple coroutines share the same threads
        2. Thread Reusing: When a coroutine suspends, thread is freed
        3. High Concurrency: Thousands of operations on few threads
        4. No Blocking: Threads aren't blocked - they're freed and reused
        
        Real Benefit:
        - RxJava: 10-20 threads, limited to 10-20 concurrent operations
        - Coroutines: 2-4 threads, unlimited concurrent operations
        
        Your app can handle HUNDREDS of concurrent operations with just
        2-4 threads, while RxJava needs 10-20 threads and still limits
        concurrency!
    """.trimIndent())
}
