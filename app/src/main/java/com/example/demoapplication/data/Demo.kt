package com.example.demoapplication.data

import kotlinx.coroutines.*

// COROUTINE BUILDERS EXPLAINED

fun main() = runBlocking {
    println("=== COROUTINE BUILDERS EXPLAINED ===\n")
    
    // 1. LAUNCH - Fire and Forget
    launchExample()
    
    // 2. ASYNC - Get Results
    asyncExample()
    
    // 3. RUNBLOCKING - Bridge
    runBlockingExample()
}

// 1. LAUNCH - Fire and Forget Background Work
suspend fun launchExample() = coroutineScope {
    println("1. LAUNCH Example:")
    println("Use case: Background tasks, side effects, fire-and-forget operations")
    
    // Simple background task
    val job = launch {
        repeat(3) {
            println("  Background task $it working...")
            delay(300)
        }
        println("  Background task completed!")
    }
    
    println("  Main thread continues immediately (non-blocking)")
    
    // Wait for completion if needed
    job.join()
    println("  Job finished\n")
    
    // Multiple concurrent background tasks
    println("  Multiple concurrent tasks:")
    val job1 = launch { 
        delay(500)
        println("    Task 1 done") 
    }
    val job2 = launch { 
        delay(300)
        println("    Task 2 done") 
    }
    val job3 = launch { 
        delay(700)
        println("    Task 3 done") 
    }
    
    // Wait for all
    listOf(job1, job2, job3).forEach { it.join() }
    println()
}

// 2. ASYNC - Concurrent Computation with Results
suspend fun asyncExample() = coroutineScope {
    println("2. ASYNC Example:")
    println("Use case: Parallel computations, multiple API calls, getting results")
    
    // Helper functions
    suspend fun fetchUser(id: String): String {
        delay(800)
        return "User-$id"
    }
    
    suspend fun fetchPosts(userId: String): List<String> {
        delay(600)
        return listOf("Post1-$userId", "Post2-$userId")
    }
    
    suspend fun fetchSettings(userId: String): String {
        delay(400)
        return "Settings-$userId"
    }
    
    // Sequential approach (SLOW)
    println("  Sequential execution:")
    val sequentialStart = System.currentTimeMillis()
    
    val user = fetchUser("123")
    val posts = fetchPosts("123")
    val settings = fetchSettings("123")
    
    println("    Results: $user, $posts, $settings")
    println("    Time: ${System.currentTimeMillis() - sequentialStart}ms")
    
    // Concurrent approach with async (FAST)
    println("\n  Concurrent execution with async:")
    val concurrentStart = System.currentTimeMillis()
    
    val userDeferred = async { fetchUser("456") }
    val postsDeferred = async { fetchPosts("456") }
    val settingsDeferred = async { fetchSettings("456") }
    
    // Get results
    val userResult = userDeferred.await()
    val postsResult = postsDeferred.await()
    val settingsResult = settingsDeferred.await()
    
    println("    Results: $userResult, $postsResult, $settingsResult")
    println("    Time: ${System.currentTimeMillis() - concurrentStart}ms")
    
    // Multiple calculations
    println("\n  Multiple parallel calculations:")
    val numbers = listOf(2, 3, 4, 5)
    
    val calculations = numbers.map { num ->
        async {
            delay(num * 100L)
            num * num
        }
    }
    
    val results = calculations.map { it.await() }
    println("    Squares: $results")
    println()
}

// 3. RUNBLOCKING - Bridge from Blocking to Suspending
fun runBlockingExample() {
    println("3. RUNBLOCKING Example:")
    println("Use case: Main functions, tests, bridging blocking/suspending code")
    
    // This is a regular function that needs to call suspend functions
    fun regularFunction(): String {
        // Can't call suspend functions directly from here!
        // delay(1000) // ❌ Would be compiler error
        
        // Use runBlocking to bridge
        return runBlocking {
            delay(500) // ✅ Now we can call suspend functions
            "Result from suspend function"
        }
    }
    
    val result = regularFunction()
    println("  $result")
    
    // Common in tests
    println("\n  Unit test example:")
    
    fun testSuspendFunction() = runBlocking {
        suspend fun apiCall(): String {
            delay(200)
            return "API Response"
        }
        
        val response = apiCall()
        println("    Test result: $response")
        // assert(response == "API Response")
    }
    
    testSuspendFunction()
    println()
}

// SUMMARY TABLE
/*
╔════════════════╦══════════════╦═════════════╦═══════════════════════════════════╗
║ Builder        ║ Returns      ║ Blocks?     ║ Use Case                          ║
╠════════════════╬══════════════╬═════════════╬═══════════════════════════════════╣
║ launch         ║ Job          ║ No          ║ Background work, side effects     ║
║ async          ║ Deferred<T>  ║ No          ║ Concurrent computation + results  ║
║ runBlocking    ║ T            ║ Yes         ║ Main function, tests, bridging    ║
╚════════════════╩══════════════╩═════════════╩═══════════════════════════════════╝

KEY POINTS:
1. launch: Fire-and-forget, returns Job for control
2. async: Get results from parallel operations
3. runBlocking: Blocks thread, only use for bridging

PERFORMANCE:
- Sequential: Operations run one after another
- Concurrent (async): Operations run in parallel
- Can reduce total time dramatically!

ERROR HANDLING:
- launch: Handle errors inside with try-catch
- async: Errors thrown when calling await()
- Both support cancellation and timeouts
*/


