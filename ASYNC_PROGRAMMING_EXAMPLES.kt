// Practical Examples: Callbacks vs RxJava vs Coroutines
// Use these examples to understand the differences and why Coroutines are preferred

package com.example.demoapplication.async

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// ============================================================================
// 1. CALLBACK-BASED APPROACH
// ============================================================================

/**
 * Callback-Based: Traditional approach using interfaces/lambdas
 * 
 * Problems:
 * - Callback hell / Pyramid of doom
 * - Hard to handle errors
 * - Manual thread management
 * - No cancellation support
 * - Memory leaks if callbacks hold references
 */

// Callback interface
interface ApiCallback<T> {
    fun onSuccess(data: T)
    fun onError(error: Throwable)
}

// API Service with callbacks
class CallbackApiService {
    fun fetchUser(userId: String, callback: ApiCallback<User>) {
        Thread {
            try {
                Thread.sleep(2000) // Simulate network delay
                val user = User(id = userId, name = "John Doe", email = "john@example.com")
                callback.onSuccess(user)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }.start()
    }
    
    fun fetchPosts(userId: String, callback: ApiCallback<List<Post>>) {
        Thread {
            try {
                Thread.sleep(1500)
                val posts = listOf(Post(id = "1", title = "Post 1"))
                callback.onSuccess(posts)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }.start()
    }
}

// Usage - Simple case
fun callbackExample() {
    val apiService = CallbackApiService()
    
    apiService.fetchUser("123", object : ApiCallback<User> {
        override fun onSuccess(user: User) {
            // Must switch to main thread manually
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                println("User loaded: ${user.name}")
            }
        }
        
        override fun onError(error: Throwable) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                println("Error: ${error.message}")
            }
        }
    })
}

// Usage - Callback Hell (Nested callbacks)
fun callbackHellExample() {
    val apiService = CallbackApiService()
    
    apiService.fetchUser("123", object : ApiCallback<User> {
        override fun onSuccess(user: User) {
            apiService.fetchPosts(user.id, object : ApiCallback<List<Post>> {
                override fun onSuccess(posts: List<Post>) {
                    // More nested callbacks...
                    println("Posts loaded: ${posts.size}")
                }
                
                override fun onError(error: Throwable) {
                    println("Error loading posts: ${error.message}")
                }
            })
        }
        
        override fun onError(error: Throwable) {
            println("Error loading user: ${error.message}")
        }
    })
}

// ============================================================================
// 2. RXJAVA APPROACH
// ============================================================================

/**
 * RxJava: Reactive programming library
 * 
 * Advantages over callbacks:
 * - Avoids callback hell
 * - Chainable operations
 * - Built-in error handling
 * - Thread management with schedulers
 * - Cancellation support (Disposable)
 * 
 * Problems:
 * - Steep learning curve
 * - Large library size
 * - Overkill for simple cases
 * - Debugging difficulty
 * - Memory overhead
 */

// Note: RxJava code is commented out because it requires dependencies
// Uncomment and add dependencies if you want to test:
// implementation 'io.reactivex.rxjava3:rxjava:3.1.5'
// implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'

/*
// API Service with RxJava
interface RxApiService {
    @GET("users/{id}")
    fun getUser(@Path("id") userId: String): Observable<User>
    
    @GET("users/{id}/posts")
    fun getPosts(@Path("id") userId: String): Observable<List<Post>>
}

// Usage - Simple case
fun rxJavaExample() {
    apiService.getUser("123")
        .subscribeOn(Schedulers.io())           // Execute on background thread
        .observeOn(AndroidSchedulers.mainThread()) // Observe on main thread
        .subscribe(
            { user -> println("User loaded: ${user.name}") },
            { error -> println("Error: ${error.message}") }
        )
}

// Usage - Chaining
fun rxJavaChainingExample() {
    apiService.getUser("123")
        .flatMap { user -> apiService.getPosts(user.id) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { posts -> println("Posts loaded: ${posts.size}") },
            { error -> println("Error: ${error.message}") }
        )
}
*/

// ============================================================================
// 3. COROUTINES APPROACH (RECOMMENDED)
// ============================================================================

/**
 * Coroutines: Kotlin's modern concurrency solution
 * 
 * Advantages:
 * - Readable code (looks like synchronous code)
 * - Lightweight (minimal library size)
 * - Easy to learn
 * - Excellent integration with Android
 * - Structured concurrency (automatic cancellation)
 * - Better performance
 * - Standard error handling (try-catch)
 */

// API Service with Coroutines
interface CoroutineApiService {
    suspend fun getUser(userId: String): User
    suspend fun getPosts(userId: String): List<Post>
    suspend fun getComments(postId: String): List<Comment>
}

// Mock implementation
class MockCoroutineApiService : CoroutineApiService {
    override suspend fun getUser(userId: String): User {
        delay(2000) // Simulate network delay
        return User(id = userId, name = "John Doe", email = "john@example.com")
    }
    
    override suspend fun getPosts(userId: String): List<Post> {
        delay(1500)
        return listOf(
            Post(id = "1", title = "Post 1"),
            Post(id = "2", title = "Post 2")
        )
    }
    
    override suspend fun getComments(postId: String): List<Comment> {
        delay(1000)
        return listOf(Comment(id = "1", text = "Comment 1"))
    }
}

// Usage - Simple case (in ViewModel)
class UserViewModel : ViewModel() {
    private val apiService = MockCoroutineApiService()
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val user = apiService.getUser(userId)
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

// Usage - Chaining (looks like synchronous code!)
suspend fun loadUserWithPosts(userId: String) {
    val apiService = MockCoroutineApiService()
    
    try {
        val user = apiService.getUser(userId)        // Suspend - waits here
        val posts = apiService.getPosts(user.id)     // Suspend - waits here
        println("User: ${user.name}, Posts: ${posts.size}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

// Usage - Parallel execution
suspend fun loadUserDataParallel(userId: String) {
    val apiService = MockCoroutineApiService()
    
    coroutineScope {
        // Launch both calls in parallel
        val userDeferred = async { apiService.getUser(userId) }
        val postsDeferred = async { apiService.getPosts(userId) }
        
        // Wait for both to complete
        val user = userDeferred.await()
        val posts = postsDeferred.await()
        
        println("User: ${user.name}, Posts: ${posts.size}")
    }
}

// Usage - Complex chaining (no callback hell!)
suspend fun loadUserWithPostsAndComments(userId: String) {
    val apiService = MockCoroutineApiService()
    
    try {
        val user = apiService.getUser(userId)
        val posts = apiService.getPosts(user.id)
        val comments = apiService.getComments(posts[0].id)
        
        println("User: ${user.name}")
        println("Posts: ${posts.size}")
        println("Comments: ${comments.size}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

// ============================================================================
// 4. SIDE-BY-SIDE COMPARISON
// ============================================================================

/**
 * Same operation implemented in three different ways
 */

// Task: Fetch user, then fetch posts, then display

// CALLBACK-BASED (Callback Hell)
fun callbackVersion() {
    val callbackApi = CallbackApiService()
    
    callbackApi.fetchUser("123", object : ApiCallback<User> {
        override fun onSuccess(user: User) {
            callbackApi.fetchPosts(user.id, object : ApiCallback<List<Post>> {
                override fun onSuccess(posts: List<Post>) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        println("User: ${user.name}, Posts: ${posts.size}")
                    }
                }
                override fun onError(error: Throwable) {
                    println("Error: ${error.message}")
                }
            })
        }
        override fun onError(error: Throwable) {
            println("Error: ${error.message}")
        }
    })
}

// RXJAVA (Better but complex)
/*
fun rxJavaVersion() {
    apiService.getUser("123")
        .flatMap { user -> apiService.getPosts(user.id) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { posts -> println("Posts: ${posts.size}") },
            { error -> println("Error: ${error.message}") }
        )
}
*/

// COROUTINES (Clean and readable!)
fun coroutineVersion() {
    val apiService = MockCoroutineApiService()
    
    // In ViewModel or Activity
    viewModelScope.launch {
        try {
            val user = apiService.getUser("123")
            val posts = apiService.getPosts(user.id)
            println("User: ${user.name}, Posts: ${posts.size}")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
}

// ============================================================================
// 5. ERROR HANDLING COMPARISON
// ============================================================================

// CALLBACK-BASED
fun callbackErrorHandling() {
    val apiService = CallbackApiService()
    
    apiService.fetchUser("123", object : ApiCallback<User> {
        override fun onSuccess(user: User) {
            // Success handling
        }
        override fun onError(error: Throwable) {
            // Error handling - separate callback
            when (error) {
                is NetworkException -> println("Network error")
                is TimeoutException -> println("Timeout")
                else -> println("Unknown error")
            }
        }
    })
}

// COROUTINES (Standard try-catch!)
fun coroutineErrorHandling() {
    val apiService = MockCoroutineApiService()
    
    viewModelScope.launch {
        try {
            val user = apiService.getUser("123")
            // Success handling
        } catch (e: NetworkException) {
            println("Network error: ${e.message}")
        } catch (e: TimeoutException) {
            println("Timeout: ${e.message}")
        } catch (e: Exception) {
            println("Unknown error: ${e.message}")
        }
    }
}

// ============================================================================
// 6. CANCELLATION COMPARISON
// ============================================================================

// CALLBACK-BASED (No cancellation support)
fun callbackNoCancellation() {
    val apiService = CallbackApiService()
    
    apiService.fetchUser("123", object : ApiCallback<User> {
        override fun onSuccess(user: User) {
            // What if user navigated away? Callback still executes!
            println("User loaded: ${user.name}")
        }
        override fun onError(error: Throwable) {}
    })
    // No way to cancel!
}

// COROUTINES (Automatic cancellation)
class CancellationExampleViewModel : ViewModel() {
    private val apiService = MockCoroutineApiService()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            try {
                val user = apiService.getUser(userId)
                println("User loaded: ${user.name}")
            } catch (e: CancellationException) {
                // Automatically cancelled when ViewModel is cleared
                println("Cancelled")
            }
        }
    }
    // When ViewModel is cleared, coroutine is automatically cancelled!
}

// ============================================================================
// 7. PARALLEL EXECUTION
// ============================================================================

// COROUTINES - Parallel execution
suspend fun parallelExecutionExample() {
    val apiService = MockCoroutineApiService()
    
    coroutineScope {
        // Both execute in parallel
        val userDeferred = async { apiService.getUser("123") }
        val postsDeferred = async { apiService.getPosts("123") }
        
        // Wait for both
        val user = userDeferred.await()
        val posts = postsDeferred.await()
        
        println("User: ${user.name}, Posts: ${posts.size}")
    }
}

// COROUTINES - Sequential execution
suspend fun sequentialExecutionExample() {
    val apiService = MockCoroutineApiService()
    
    // One after another
    val user = apiService.getUser("123")
    val posts = apiService.getPosts(user.id)
    
    println("User: ${user.name}, Posts: ${posts.size}")
}

// ============================================================================
// 8. FLOW FOR STREAMS (Coroutines)
// ============================================================================

/**
 * Flow: For reactive streams (like RxJava Observable)
 */
fun flowExample() {
    val apiService = MockCoroutineApiService()
    
    // Create a flow that emits data periodically
    fun observeUserData(userId: String): Flow<User> = flow {
        while (true) {
            val user = apiService.getUser(userId)
            emit(user)
            delay(5000) // Emit every 5 seconds
        }
    }.flowOn(Dispatchers.IO)
    
    // Collect in ViewModel
    class FlowViewModel : ViewModel() {
        fun observeUser(userId: String) {
            viewModelScope.launch {
                observeUserData(userId)
                    .catch { e -> println("Error: ${e.message}") }
                    .collect { user ->
                        println("User updated: ${user.name}")
                    }
            }
        }
    }
}

// ============================================================================
// 9. REAL-WORLD ANDROID EXAMPLE
// ============================================================================

/**
 * Complete ViewModel example using Coroutines
 */
class RealWorldViewModel : ViewModel() {
    private val apiService = MockCoroutineApiService()
    
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    fun loadUserData(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Fetch user and posts in parallel
                val userDeferred = async { apiService.getUser(userId) }
                val postsDeferred = async { apiService.getPosts(userId) }
                
                val user = userDeferred.await()
                val posts = postsDeferred.await()
                
                _uiState.value = _uiState.value.copy(
                    user = user,
                    posts = posts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun retry() {
        val userId = _uiState.value.user?.id ?: return
        loadUserData(userId)
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class User(
    val id: String,
    val name: String,
    val email: String
)

data class Post(
    val id: String,
    val title: String
)

data class Comment(
    val id: String,
    val text: String
)

data class UserUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Custom exceptions
class NetworkException(message: String) : Exception(message)
class TimeoutException(message: String) : Exception(message)

// ============================================================================
// SUMMARY
// ============================================================================

/**
 * WHY COROUTINES ARE BETTER:
 * 
 * 1. ✅ Readability - Code looks like synchronous code
 * 2. ✅ Lightweight - Minimal library size (~200KB)
 * 3. ✅ Easy to Learn - Simple concepts (suspend, launch, async)
 * 4. ✅ Excellent Integration - Built into Kotlin and Android
 * 5. ✅ Structured Concurrency - Automatic cancellation, no memory leaks
 * 6. ✅ Performance - More efficient than RxJava
 * 7. ✅ Standard Error Handling - Familiar try-catch
 * 8. ✅ Future-Proof - Recommended by Google and JetBrains
 * 
 * WHEN TO USE EACH:
 * 
 * - Coroutines: ✅ Default choice for all async operations
 * - RxJava: Only if you need complex reactive streams or already have it
 * - Callbacks: Legacy code or simple one-off operations
 */
