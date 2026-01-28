# Async Programming in Android: Callbacks vs RxJava vs Coroutines

## Table of Contents
1. [Overview](#overview)
2. [Callback-Based Approach](#callback-based-approach)
3. [RxJava Approach](#rxjava-approach)
4. [Coroutines Approach](#coroutines-approach)
5. [Side-by-Side Comparison](#side-by-side-comparison)
6. [Why Coroutines are Better](#why-coroutines-are-better)
7. [Migration Guide](#migration-guide)
8. [Best Practices](#best-practices)

---

## Overview

Android apps need to handle asynchronous operations like network calls, database operations, and file I/O. There are three main approaches:

1. **Callback-Based** - Traditional approach using callbacks/interfaces
2. **RxJava** - Reactive programming library
3. **Coroutines** - Kotlin's modern concurrency solution

---

## Callback-Based Approach

### How It Works

Uses callback interfaces or lambda functions to handle async results.

### Example: Network Call with Callbacks

```kotlin
// Interface definition
interface ApiCallback<T> {
    fun onSuccess(data: T)
    fun onError(error: Throwable)
}

// API Service
class ApiService {
    fun fetchUser(userId: String, callback: ApiCallback<User>) {
        // Simulate network call
        Thread {
            try {
                Thread.sleep(2000) // Network delay
                val user = User(id = userId, name = "John Doe")
                callback.onSuccess(user)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }.start()
    }
}

// Usage
fun loadUser() {
    apiService.fetchUser("123", object : ApiCallback<User> {
        override fun onSuccess(data: User) {
            // Update UI on main thread
            runOnUiThread {
                textView.text = data.name
            }
        }
        
        override fun onError(error: Throwable) {
            runOnUiThread {
                showError(error.message)
            }
        }
    })
}
```

### Problems with Callbacks

❌ **Callback Hell / Pyramid of Doom**
```kotlin
apiService.fetchUser("123", object : ApiCallback<User> {
    override fun onSuccess(user: User) {
        apiService.fetchPosts(user.id, object : ApiCallback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                apiService.fetchComments(posts[0].id, object : ApiCallback<List<Comment>> {
                    override fun onSuccess(comments: List<Comment>) {
                        // Nested callbacks - hard to read!
                    }
                    override fun onError(error: Throwable) { }
                })
            }
            override fun onError(error: Throwable) { }
        })
    }
    override fun onError(error: Throwable) { }
})
```

❌ **Error Handling Complexity**
- Errors can be lost in nested callbacks
- Hard to handle errors at different levels

❌ **Thread Management**
- Manual thread switching required
- Easy to forget `runOnUiThread`

❌ **No Cancellation**
- Hard to cancel operations
- Memory leaks if callbacks hold references

---

## RxJava Approach

### How It Works

Uses reactive streams (Observable/Flowable) with operators for async operations.

### Example: Network Call with RxJava

```kotlin
// Dependencies needed
// implementation 'io.reactivex.rxjava3:rxjava:3.1.5'
// implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
// implementation 'com.squareup.retrofit2:adapter-rxjava3:2.9.0'

// API Service
interface ApiService {
    @GET("users/{id}")
    fun getUser(@Path("id") userId: String): Observable<User>
}

// Usage
fun loadUser() {
    apiService.getUser("123")
        .subscribeOn(Schedulers.io())           // Execute on background thread
        .observeOn(AndroidSchedulers.mainThread()) // Observe on main thread
        .subscribe(
            { user -> 
                textView.text = user.name 
            },
            { error -> 
                showError(error.message) 
            }
        )
}

// Chaining multiple calls
fun loadUserWithPosts() {
    apiService.getUser("123")
        .flatMap { user -> 
            apiService.getPosts(user.id) 
        }
        .flatMap { posts -> 
            apiService.getComments(posts[0].id) 
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { comments -> 
                displayComments(comments) 
            },
            { error -> 
                showError(error.message) 
            }
        )
}
```

### Advantages of RxJava

✅ **Better than Callbacks**
- Avoids callback hell
- Chainable operations
- Built-in error handling
- Thread management with schedulers
- Cancellation support (Disposable)

### Problems with RxJava

❌ **Steep Learning Curve**
- Complex concepts (Observable, Observer, Operators)
- Many operators to learn (map, flatMap, switchMap, etc.)
- Different types (Observable, Single, Completable, Flowable)

❌ **Large Library Size**
- Adds significant APK size
- Multiple dependencies

❌ **Overkill for Simple Cases**
- Too complex for simple async operations
- Can be over-engineered

❌ **Debugging Difficulty**
- Stack traces can be hard to read
- Complex error messages

❌ **Memory Overhead**
- More memory usage than coroutines (both heap and thread memory)
- See detailed explanation in "Memory Usage Comparison" section below

---

## Coroutines Approach

### How It Works

Uses suspending functions and coroutine scopes for async operations. Looks like synchronous code but runs asynchronously.

### Example: Network Call with Coroutines

```kotlin
// Dependencies needed (usually already included in modern Android projects)
// implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
// implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'

// API Service
interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): User
}

// Usage in ViewModel
class UserViewModel : ViewModel() {
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

// Chaining multiple calls (looks like synchronous code!)
suspend fun loadUserWithPosts(userId: String) {
    try {
        val user = apiService.getUser(userId)        // Suspend - waits here
        val posts = apiService.getPosts(user.id)     // Suspend - waits here
        val comments = apiService.getComments(posts[0].id) // Suspend - waits here
        
        displayComments(comments)
    } catch (e: Exception) {
        showError(e.message)
    }
}

// Parallel execution
suspend fun loadUserDataParallel(userId: String) {
    coroutineScope {
        val userDeferred = async { apiService.getUser(userId) }
        val postsDeferred = async { apiService.getPosts(userId) }
        
        val user = userDeferred.await()
        val posts = postsDeferred.await()
        
        // Both completed, use results
        displayUserData(user, posts)
    }
}
```

### Advantages of Coroutines

✅ **Readable Code**
- Looks like synchronous code
- Easy to understand and maintain
- No callback hell

✅ **Lightweight**
- Minimal library size
- Low memory overhead
- Built into Kotlin

✅ **Easy to Learn**
- Simple concepts (suspend, launch, async)
- Familiar try-catch error handling
- Natural flow

✅ **Excellent Integration**
- Built into Kotlin language
- First-class support in Android
- Works with existing code

✅ **Structured Concurrency**
- Automatic cancellation
- Lifecycle-aware (viewModelScope, lifecycleScope)
- No memory leaks

✅ **Performance**
- More efficient than RxJava
- Less overhead (both heap and thread memory)
- See detailed explanation in "Memory Usage Comparison" section below

---

## Side-by-Side Comparison

### Example: Fetch User and Display

#### Callback-Based
```kotlin
fun loadUser() {
    apiService.fetchUser("123", object : ApiCallback<User> {
        override fun onSuccess(user: User) {
            runOnUiThread {
                textView.text = user.name
                hideLoading()
            }
        }
        
        override fun onError(error: Throwable) {
            runOnUiThread {
                showError(error.message)
                hideLoading()
            }
        }
    })
}
```

#### RxJava
```kotlin
fun loadUser() {
    apiService.getUser("123")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { showLoading() }
        .doFinally { hideLoading() }
        .subscribe(
            { user -> textView.text = user.name },
            { error -> showError(error.message) }
        )
}
```

#### Coroutines
```kotlin
fun loadUser() {
    viewModelScope.launch {
        try {
            showLoading()
            val user = apiService.getUser("123")
            textView.text = user.name
        } catch (e: Exception) {
            showError(e.message)
        } finally {
            hideLoading()
        }
    }
}
```

### Example: Chaining Multiple Calls

#### Callback-Based (Callback Hell!)
```kotlin
apiService.fetchUser("123", object : ApiCallback<User> {
    override fun onSuccess(user: User) {
        apiService.fetchPosts(user.id, object : ApiCallback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                apiService.fetchComments(posts[0].id, object : ApiCallback<List<Comment>> {
                    override fun onSuccess(comments: List<Comment>) {
                        displayComments(comments)
                    }
                    override fun onError(error: Throwable) { }
                })
            }
            override fun onError(error: Throwable) { }
        })
    }
    override fun onError(error: Throwable) { }
})
```

#### RxJava
```kotlin
apiService.getUser("123")
    .flatMap { user -> apiService.getPosts(user.id) }
    .flatMap { posts -> apiService.getComments(posts[0].id) }
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { comments -> displayComments(comments) },
        { error -> showError(error.message) }
    )
```

#### Coroutines (Clean and Readable!)
```kotlin
viewModelScope.launch {
    try {
        val user = apiService.getUser("123")
        val posts = apiService.getPosts(user.id)
        val comments = apiService.getComments(posts[0].id)
        displayComments(comments)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

---

## Why Coroutines are Better

### 1. **Readability** ⭐⭐⭐⭐⭐

**Coroutines:** Code reads like synchronous code - easy to understand
```kotlin
val user = apiService.getUser("123")
val posts = apiService.getPosts(user.id)
```

**RxJava:** Requires understanding reactive concepts
```kotlin
apiService.getUser("123")
    .flatMap { user -> apiService.getPosts(user.id) }
```

**Callbacks:** Nested callbacks - hard to read
```kotlin
apiService.fetchUser("123", object : ApiCallback<User> {
    override fun onSuccess(user: User) {
        apiService.fetchPosts(user.id, object : ApiCallback<List<Post>> {
            // Nested...
        })
    }
})
```

### 2. **Learning Curve** ⭐⭐⭐⭐⭐

**Coroutines:** 
- Simple concepts: suspend, launch, async
- Familiar try-catch error handling
- Easy for developers new to async programming

**RxJava:**
- Complex: Observable, Observer, Operators
- Many operators to learn (map, flatMap, switchMap, etc.)
- Steep learning curve

**Callbacks:**
- Simple concept but leads to complex code
- Easy to make mistakes

### 3. **Library Size** ⭐⭐⭐⭐⭐

**Coroutines:**
- Minimal size (~200KB)
- Built into Kotlin
- No additional dependencies for basic use

**RxJava:**
- Large size (~2MB+)
- Multiple dependencies
- Adds significant APK size

**Callbacks:**
- No library needed
- But requires more boilerplate code

### 4. **Error Handling** ⭐⭐⭐⭐⭐

**Coroutines:**
```kotlin
try {
    val user = apiService.getUser("123")
} catch (e: Exception) {
    // Standard exception handling
    showError(e.message)
}
```

**RxJava:**
```kotlin
apiService.getUser("123")
    .subscribe(
        { user -> },
        { error -> showError(error.message) } // Separate error handler
    )
```

**Callbacks:**
```kotlin
override fun onError(error: Throwable) {
    // Separate error callback
}
```

### 5. **Cancellation** ⭐⭐⭐⭐⭐

**Coroutines:**
- Automatic cancellation when scope is cancelled
- Lifecycle-aware (viewModelScope, lifecycleScope)
- No memory leaks

```kotlin
viewModelScope.launch {
    val user = apiService.getUser("123") // Auto-cancelled if ViewModel cleared
}
```

**RxJava:**
- Manual disposal required
- Easy to forget, causing memory leaks

```kotlin
val disposable = apiService.getUser("123")
    .subscribe { }
// Must remember to dispose!
disposable.dispose()
```

**Callbacks:**
- No built-in cancellation
- Hard to cancel operations

### 6. **Performance** ⭐⭐⭐⭐⭐

**Coroutines:**
- Lightweight threads
- Low memory overhead
- Efficient

**RxJava:**
- More memory overhead
- Slower than coroutines

**Callbacks:**
- Low overhead but leads to inefficient code patterns

### 7. **Integration** ⭐⭐⭐⭐⭐

**Coroutines:**
- First-class support in Android
- Built into Kotlin
- Works seamlessly with Retrofit, Room, etc.

**RxJava:**
- Requires adapters
- Additional setup needed

**Callbacks:**
- Works everywhere but verbose

### 8. **Debugging** ⭐⭐⭐⭐⭐

**Coroutines:**
- Clear stack traces
- Standard debugging tools work
- Easy to debug

**RxJava:**
- Complex stack traces
- Harder to debug
- Special debugging tools needed

**Callbacks:**
- Simple but nested callbacks make debugging harder

---

## Comparison Table

| Feature | Callbacks | RxJava | Coroutines |
|---------|-----------|--------|------------|
| **Readability** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Learning Curve** | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Library Size** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Error Handling** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Cancellation** | ⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Performance** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Integration** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Debugging** | ⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Callback Hell** | ❌ | ✅ | ✅ |
| **Memory Leaks Risk** | ⚠️ High | ⚠️ Medium | ✅ Low |

---

## Migration Guide

### From Callbacks to Coroutines

**Before (Callbacks):**
```kotlin
interface ApiCallback<T> {
    fun onSuccess(data: T)
    fun onError(error: Throwable)
}

fun fetchUser(userId: String, callback: ApiCallback<User>) {
    Thread {
        try {
            val user = // fetch user
            callback.onSuccess(user)
        } catch (e: Exception) {
            callback.onError(e)
        }
    }.start()
}
```

**After (Coroutines):**
```kotlin
suspend fun fetchUser(userId: String): User {
    return withContext(Dispatchers.IO) {
        // fetch user
    }
}

// Usage
viewModelScope.launch {
    try {
        val user = fetchUser("123")
        // Use user
    } catch (e: Exception) {
        // Handle error
    }
}
```

### From RxJava to Coroutines

**Before (RxJava):**
```kotlin
apiService.getUser("123")
    .flatMap { user -> apiService.getPosts(user.id) }
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { posts -> displayPosts(posts) },
        { error -> showError(error.message) }
    )
```

**After (Coroutines):**
```kotlin
viewModelScope.launch {
    try {
        val user = apiService.getUser("123")
        val posts = apiService.getPosts(user.id)
        displayPosts(posts)
    } catch (e: Exception) {
        showError(e.message)
    }
}
```

---

## Best Practices

### 1. Use Appropriate Coroutine Scopes

```kotlin
// ViewModel
viewModelScope.launch { }

// Activity/Fragment
lifecycleScope.launch { }

// Custom scope
val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
```

### 2. Use suspend Functions for Async Operations

```kotlin
suspend fun fetchData(): Result<Data> {
    return withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getData())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Handle Errors Properly

```kotlin
viewModelScope.launch {
    try {
        val data = repository.fetchData()
        _uiState.value = UiState.Success(data)
    } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message)
    }
}
```

### 4. Use Flow for Streams

```kotlin
fun observeData(): Flow<List<Item>> = flow {
    while (true) {
        emit(repository.getItems())
        delay(5000)
    }
}.flowOn(Dispatchers.IO)
```

### 5. Avoid GlobalScope

```kotlin
// ❌ Bad
GlobalScope.launch { }

// ✅ Good
viewModelScope.launch { }
lifecycleScope.launch { }
```

---

## Summary

**Coroutines are the best choice for modern Android development because:**

1. ✅ **Readable** - Code looks like synchronous code
2. ✅ **Lightweight** - Minimal library size
3. ✅ **Easy to Learn** - Simple concepts
4. ✅ **Excellent Integration** - Built into Kotlin and Android
5. ✅ **Structured Concurrency** - Automatic cancellation, no memory leaks
6. ✅ **Performance** - More efficient than alternatives
7. ✅ **Future-Proof** - Recommended by Google and JetBrains

**When to use each:**

- **Coroutines**: ✅ Default choice for all async operations
- **RxJava**: Only if you need complex reactive streams or already have it
- **Callbacks**: Legacy code or simple one-off operations

---

## Memory Usage Comparison: RxJava vs Coroutines

### Overview

RxJava uses **more memory** than Coroutines in **both Heap and Thread memory**. Here's why:

### 1. Thread Memory (Stack Memory)

#### RxJava: Uses Real Threads

**RxJava uses actual OS threads** (via Schedulers):
- Each thread has its own **stack** (~1-2MB per thread on Android)
- Threads are expensive to create and maintain
- Limited number of threads (typically 10-20 threads in thread pool)

```kotlin
// RxJava - Creates/uses real threads
apiService.getUser("123")
    .subscribeOn(Schedulers.io())  // Uses thread pool (real threads)
    .observeOn(AndroidSchedulers.mainThread()) // Uses main thread
    .subscribe { }
```

**Memory Cost:**
- Thread stack: ~1-2MB per thread
- Thread pool: 10-20 threads = **10-40MB** just for threads
- Each concurrent operation uses a thread

#### Coroutines: Uses Lightweight Coroutines

**Coroutines use lightweight coroutines** (not real threads):
- Coroutines are **suspended functions** that share threads
- No separate stack per coroutine
- Thousands of coroutines can run on a few threads

```kotlin
// Coroutines - Uses lightweight coroutines
viewModelScope.launch {
    val user = apiService.getUser("123")  // Suspends, doesn't block thread
}
```

**Memory Cost:**
- Coroutine: ~100-200 bytes per coroutine (just a small object)
- Thread pool: 2-4 threads typically = **~8MB** for threads
- Can run **thousands** of coroutines on these few threads

**Example:**
```kotlin
// Coroutines - Can launch 1000 coroutines on 2-4 threads
repeat(1000) {
    viewModelScope.launch {
        apiService.getUser("$it")
    }
}
// Uses only 2-4 threads, ~200KB for coroutines

// RxJava - Would need 1000 threads (impossible!)
// Limited by thread pool size (10-20 threads)
```

### 2. Heap Memory (Object Memory)

#### RxJava: More Objects Created

**RxJava creates many objects** for each operation:

```kotlin
apiService.getUser("123")
    .map { it.name }                    // Creates MapObserver
    .filter { it.isNotEmpty() }         // Creates FilterObserver
    .subscribeOn(Schedulers.io())       // Creates SubscribeOnObserver
    .observeOn(AndroidSchedulers.mainThread()) // Creates ObserveOnObserver
    .subscribe { }                      // Creates SubscribeObserver
```

**Objects Created:**
- Observable wrapper
- Multiple Observer objects (one per operator)
- Scheduler wrappers
- Subscription/Disposable objects
- **~5-10 objects per chain**

**Memory Cost:**
- Each object: ~50-200 bytes
- Per operation: **~500-2000 bytes**
- With many operations: **significant heap usage**

#### Coroutines: Minimal Objects

**Coroutines create minimal objects**:

```kotlin
viewModelScope.launch {                 // Creates 1 Continuation
    val user = apiService.getUser("123")
    val name = user.name
    if (name.isNotEmpty()) {
        displayName(name)
    }
}
```

**Objects Created:**
- Continuation object (coroutine state)
- **~1-2 objects per coroutine**

**Memory Cost:**
- Continuation: ~100-200 bytes
- Per operation: **~100-200 bytes**
- Much less than RxJava

### 3. Detailed Comparison

#### Scenario: 100 Concurrent Network Calls

**RxJava:**
```
Thread Memory:
- Thread pool: 10-20 threads
- Stack per thread: ~1-2MB
- Total thread memory: ~10-40MB

Heap Memory:
- Observable objects: 100 × 200 bytes = 20KB
- Observer objects: 100 × 500 bytes = 50KB
- Scheduler wrappers: 100 × 100 bytes = 10KB
- Total heap: ~80KB

Total Memory: ~10-40MB (thread) + ~80KB (heap) = ~10-40MB
```

**Coroutines:**
```
Thread Memory:
- Thread pool: 2-4 threads
- Stack per thread: ~1-2MB
- Total thread memory: ~2-8MB

Heap Memory:
- Continuation objects: 100 × 150 bytes = 15KB
- Total heap: ~15KB

Total Memory: ~2-8MB (thread) + ~15KB (heap) = ~2-8MB
```

**Result: Coroutines use 2-5x less memory!**

### 4. Visual Comparison

```
RxJava Memory Usage:
┌─────────────────────────────────────┐
│ Thread Pool (10-20 threads)        │
│ ├─ Thread 1: ~2MB stack            │
│ ├─ Thread 2: ~2MB stack            │
│ ├─ Thread 3: ~2MB stack            │
│ └─ ...                              │
│ Total: ~20-40MB                     │
├─────────────────────────────────────┤
│ Heap Memory                         │
│ ├─ Observable objects: ~20KB      │
│ ├─ Observer objects: ~50KB          │
│ └─ Wrappers: ~10KB                  │
│ Total: ~80KB                        │
└─────────────────────────────────────┘
Total: ~20-40MB

Coroutines Memory Usage:
┌─────────────────────────────────────┐
│ Thread Pool (2-4 threads)            │
│ ├─ Thread 1: ~2MB stack            │
│ ├─ Thread 2: ~2MB stack            │
│ Total: ~4-8MB                       │
├─────────────────────────────────────┤
│ Heap Memory                         │
│ ├─ Continuation objects: ~15KB      │
│ Total: ~15KB                        │
└─────────────────────────────────────┘
Total: ~4-8MB

Memory Savings: 2-5x less!
```

### 5. Why Coroutines Use Less Memory

#### Thread Memory (Stack)

**RxJava:**
- Uses **real OS threads**
- Each thread has its own **stack** (~1-2MB)
- Threads are **heavyweight**
- Limited by thread count

**Coroutines:**
- Uses **lightweight coroutines**
- Coroutines **share threads**
- No separate stack per coroutine
- Can have **thousands** of coroutines

**Key Difference:**
```
RxJava:  1 operation = 1 thread (or waits for thread pool)
Coroutines: 1000 operations = 2-4 threads (all share)
```

#### Heap Memory (Objects)

**RxJava:**
- Creates **many objects** per operation
- Each operator creates new Observer
- More object allocations
- More GC pressure

**Coroutines:**
- Creates **minimal objects**
- Just Continuation object
- Fewer allocations
- Less GC pressure

### 6. Real-World Impact

#### Example: News Feed App

**Scenario:** Loading 50 news items with images

**RxJava:**
```
- 50 network calls
- Thread pool: 10-20 threads
- Memory: ~20-40MB (threads) + ~100KB (objects)
- Can handle: Limited by thread pool
```

**Coroutines:**
```
- 50 network calls
- Thread pool: 2-4 threads
- Memory: ~4-8MB (threads) + ~10KB (objects)
- Can handle: Thousands of concurrent operations
```

**Result:** Coroutines use **5x less memory** and can handle **more concurrent operations**!

### 7. Memory Profiling Example

```kotlin
// Test: Launch 1000 concurrent operations

// RxJava - Limited by thread pool
fun rxJavaTest() {
    val disposables = mutableListOf<Disposable>()
    repeat(1000) { i ->
        val disposable = apiService.getUser("$i")
            .subscribeOn(Schedulers.io())
            .subscribe { }
        disposables.add(disposable)
    }
    // Memory: ~20-40MB (threads) + ~200KB (objects)
    // Actually limited to ~10-20 concurrent operations
}

// Coroutines - Can handle all
fun coroutinesTest() {
    viewModelScope.launch {
        repeat(1000) { i ->
            launch {
                apiService.getUser("$i")
            }
        }
    }
    // Memory: ~4-8MB (threads) + ~150KB (objects)
    // Can handle all 1000 concurrently!
}
```

### 8. Summary Table

| Memory Type | RxJava | Coroutines | Winner |
|-------------|--------|------------|--------|
| **Thread Stack** | ~1-2MB per thread<br/>10-20 threads = **20-40MB** | ~1-2MB per thread<br/>2-4 threads = **4-8MB** | ✅ Coroutines (5x less) |
| **Heap Objects** | ~500-2000 bytes per operation<br/>Many objects per chain | ~100-200 bytes per operation<br/>Minimal objects | ✅ Coroutines (5-10x less) |
| **Total Memory** | **~20-40MB** (for 100 operations) | **~4-8MB** (for 100 operations) | ✅ Coroutines (5x less) |
| **Scalability** | Limited by thread pool (10-20) | Unlimited (thousands) | ✅ Coroutines |

### 9. Key Takeaways

1. **Thread Memory (Stack):**
   - RxJava: Uses real threads (~1-2MB each)
   - Coroutines: Uses lightweight coroutines (~100-200 bytes each)
   - **Coroutines use 5x less thread memory**

2. **Heap Memory (Objects):**
   - RxJava: Creates many objects per operation
   - Coroutines: Creates minimal objects
   - **Coroutines use 5-10x less heap memory**

3. **Total Memory:**
   - RxJava: ~20-40MB for typical usage
   - Coroutines: ~4-8MB for typical usage
   - **Coroutines use 5x less total memory**

4. **Scalability:**
   - RxJava: Limited by thread pool size
   - Coroutines: Can handle thousands of concurrent operations
   - **Coroutines scale much better**

### 10. When Memory Matters Most

**Memory is especially important for:**
- Low-end devices (limited RAM)
- Apps with many concurrent operations
- Long-running background tasks
- Apps that need to stay in memory

**Coroutines excel in all these scenarios!**

---

## Resources

- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Android Coroutines Guide](https://developer.android.com/kotlin/coroutines)
- [Coroutines Best Practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
