package com.example.demoapplication.data.general

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() {
    runBlocking {
        val parentJob = Job()
        val scope = CoroutineScope(Dispatchers.Default+ parentJob)
        val job1 = scope.launch {
            launch { delay(1000)
            println("child 1")}
        }

        val job2 = scope.launch {
            delay(2000)
            println("child2")
        }

        parentJob.cancel()

        delay(4000)
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
