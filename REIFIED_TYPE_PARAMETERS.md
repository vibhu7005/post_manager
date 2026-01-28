# Reified Type Parameters - Complete Guide

## Table of Contents
1. [Fundamentals](#fundamentals)
2. [The Problem Reified Solves](#the-problem-reified-solves)
3. [How Reified Works](#how-reified-works)
4. [Basic Usage](#basic-usage)
5. [Common Use Cases](#common-use-cases)
6. [Android-Specific Examples](#android-specific-examples)
7. [Limitations and Constraints](#limitations-and-constraints)
8. [Reified vs Alternatives](#reified-vs-alternatives)
9. [Best Practices](#best-practices)
10. [Advanced Concepts](#advanced-concepts)
11. [Interview Questions](#interview-questions)
12. [Code Examples](#code-examples)

---

## Fundamentals

### What is Reified?

**Reified** is a Kotlin keyword that allows you to access type information at runtime for generic type parameters. Normally, generic types are erased at runtime due to type erasure, but `reified` preserves this information.

### Key Points

- **`reified`** can only be used with **`inline`** functions
- It allows access to type information at runtime
- Eliminates the need to pass `Class<T>` parameters
- Makes code more type-safe and cleaner

### Basic Syntax

```kotlin
// Reified type parameter
inline fun <reified T> functionName(): T {
    // Can access T at runtime
    return T::class.java.newInstance()
}
```

---

## The Problem Reified Solves

### Type Erasure Problem

In Java/Kotlin, generic type information is erased at runtime due to type erasure:

```kotlin
// Without reified - Type erasure problem
fun <T> isInstanceOf(obj: Any): Boolean {
    // ❌ Can't do this: obj is T
    // T is erased at runtime, so we can't check it
    return obj is T  // ERROR: Cannot check for instance of erased type
}

// Workaround: Pass Class<T> parameter
fun <T> isInstanceOf(obj: Any, clazz: Class<T>): Boolean {
    return clazz.isInstance(obj)  // Works, but verbose
}

// Usage
val isString = isInstanceOf("hello", String::class.java)  // Verbose!
```

### With Reified - Solution

```kotlin
// With reified - Clean solution
inline fun <reified T> isInstanceOf(obj: Any): Boolean {
    return obj is T  // ✅ Works! T is available at runtime
}

// Usage
val isString = isInstanceOf<String>("hello")  // Clean!
```

---

## How Reified Works

### Under the Hood

When you use `reified` with `inline`, the compiler:
1. Inlines the function body at the call site
2. Replaces `T` with the actual type argument
3. Makes type information available at runtime

```kotlin
// What you write
inline fun <reified T> getType(): String {
    return T::class.simpleName ?: "Unknown"
}

// Usage
val type = getType<String>()

// What compiler generates (simplified)
val type = String::class.simpleName ?: "Unknown"
```

### Why Inline is Required

`reified` requires `inline` because:
- Inline functions are expanded at call sites
- This allows the compiler to know the actual type at compile time
- The type can then be preserved at runtime

```kotlin
// ❌ ERROR: reified requires inline
fun <reified T> example() { }  // Error: Only type parameters of inline functions can be reified

// ✅ CORRECT: reified with inline
inline fun <reified T> example() { }  // Works!
```

---

## Basic Usage

### Type Checking

```kotlin
// Check if object is instance of type T
inline fun <reified T> isInstanceOf(obj: Any): Boolean {
    return obj is T
}

// Usage
val isString = isInstanceOf<String>("hello")  // true
val isInt = isInstanceOf<Int>("hello")       // false
```

### Getting Class Information

```kotlin
// Get class of type T
inline fun <reified T> getClass(): KClass<T> {
    return T::class
}

// Get Java class
inline fun <reified T> getJavaClass(): Class<T> {
    return T::class.java
}

// Usage
val kClass = getClass<String>()      // KClass<String>
val javaClass = getJavaClass<String>() // Class<String>
```

### Creating Instances

```kotlin
// Create instance of type T (requires no-arg constructor)
inline fun <reified T> createInstance(): T {
    return T::class.java.newInstance()
}

// Usage
val string = createInstance<String>()  // Creates new String()
val list = createInstance<ArrayList<String>>()  // Creates new ArrayList()
```

---

## Common Use Cases

### 1. Type-Safe Logging

```kotlin
// Log with type information
inline fun <reified T> T.log(message: String) {
    val tag = T::class.simpleName
    println("[$tag] $message")
}

// Usage
class MyClass {
    fun test() {
        log("Test message")  // [MyClass] Test message
    }
}
```

### 2. Generic Type Checking

```kotlin
// Check and cast safely
inline fun <reified T> Any?.safeCast(): T? {
    return this as? T
}

// Usage
val string: String? = "hello".safeCast<String>()
val number: Int? = "hello".safeCast<Int>()  // null
```

### 3. Filter by Type

```kotlin
// Filter list by type
inline fun <reified T> List<*>.filterIsInstance(): List<T> {
    return filterIsInstance<T>()
}

// Usage
val mixed = listOf(1, "hello", 2, "world", 3)
val numbers = mixed.filterIsInstance<Int>()  // [1, 2, 3]
val strings = mixed.filterIsInstance<String>()  // ["hello", "world"]
```

### 4. Type-Safe Factory

```kotlin
// Create instance with type safety
inline fun <reified T> create(): T {
    return when (T::class) {
        String::class -> "" as T
        Int::class -> 0 as T
        List::class -> emptyList<Any>() as T
        else -> T::class.java.newInstance()
    }
}

// Usage
val string = create<String>()  // ""
val number = create<Int>()     // 0
```

### 5. JSON Parsing

```kotlin
// Parse JSON to type T
inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}

// Usage
val user = gson.fromJson<User>(jsonString)
val list = gson.fromJson<List<User>>(jsonArray)
```

---

## Android-Specific Examples

### 1. ViewModel Creation

```kotlin
// Get ViewModel with reified type
inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

// Usage in Fragment
class MyFragment : Fragment() {
    private val viewModel: MyViewModel by lazy { getViewModel<MyViewModel>() }
}
```

### 2. Intent Extras

```kotlin
// Get typed extra from Intent
inline fun <reified T> Intent.getExtra(key: String): T? {
    return when (T::class) {
        String::class -> getStringExtra(key) as? T
        Int::class -> getIntExtra(key, 0) as? T
        Boolean::class -> getBooleanExtra(key, false) as? T
        else -> getSerializableExtra(key) as? T
    }
}

// Usage
val userId: String? = intent.getExtra<String>("user_id")
val isEnabled: Boolean? = intent.getExtra<Boolean>("enabled")
```

### 3. Fragment Arguments

```kotlin
// Get typed argument from Fragment
inline fun <reified T> Fragment.argument(key: String): T? {
    return arguments?.get(key) as? T
}

// Usage
class DetailFragment : Fragment() {
    private val itemId: String? by lazy { argument<String>("item_id") }
}
```

### 4. SharedPreferences

```kotlin
// Get typed preference
inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    return when (T::class) {
        String::class -> getString(key, defaultValue as? String ?: "") as T
        Int::class -> getInt(key, defaultValue as? Int ?: 0) as T
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T
        Float::class -> getFloat(key, defaultValue as? Float ?: 0f) as T
        Long::class -> getLong(key, defaultValue as? Long ?: 0L) as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

// Usage
val userName = prefs.get<String>("user_name", "")
val userId = prefs.get<Int>("user_id", 0)
```

### 5. Retrofit Response Handling

```kotlin
// Parse Retrofit response
inline fun <reified T> Response<*>.bodyAs(): T? {
    return body() as? T
}

// Usage
val response = apiService.getUser()
val user: User? = response.bodyAs<User>()
```

### 6. Room Database Queries

```kotlin
// Execute Room query with reified type
inline fun <reified T> RoomDatabase.query(sql: String): List<T> {
    val dao = when (T::class) {
        User::class -> userDao()
        Product::class -> productDao()
        else -> throw IllegalArgumentException("Unknown type")
    }
    // Execute query...
    return emptyList()
}
```

### 7. Dependency Injection (Hilt)

```kotlin
// Get dependency with reified type
inline fun <reified T> ComponentActivity.inject(): Lazy<T> {
    return lazy {
        when (T::class) {
            UserRepository::class -> UserRepositoryImpl() as T
            ApiService::class -> RetrofitClient.apiService as T
            else -> throw IllegalArgumentException("Unknown type")
        }
    }
}

// Usage
class MainActivity : ComponentActivity() {
    private val repository: UserRepository by inject<UserRepository>()
}
```

### 8. Navigation Arguments

```kotlin
// Get navigation argument with type
inline fun <reified T> NavBackStackEntry.argument(key: String): T? {
    return arguments?.get(key) as? T
}

// Usage in Navigation Compose
composable("detail/{itemId}") { backStackEntry ->
    val itemId: String? = backStackEntry.argument<String>("itemId")
    DetailScreen(itemId)
}
```

---

## Limitations and Constraints

### 1. Must Be Inline

```kotlin
// ❌ ERROR: reified requires inline
fun <reified T> example() { }

// ✅ CORRECT
inline fun <reified T> example() { }
```

### 2. Cannot Store Reified Type

```kotlin
// ❌ ERROR: Can't store reified type
inline fun <reified T> storeType(): Class<T> {
    val clazz: Class<T> = T::class.java  // Error if trying to return/store
    return clazz  // This works, but be careful
}

// ⚠️ Limitation: Can't use reified type in non-inline context
class MyClass<T> {
    // Can't use reified here - not a function
}
```

### 3. Cannot Use in Non-Inline Functions

```kotlin
// ❌ ERROR: Can't use reified in non-inline function
fun <reified T> nonInlineFunction() { }

// ✅ CORRECT: Must be inline
inline fun <reified T> inlineFunction() { }
```

### 4. Cannot Use with Virtual Functions

```kotlin
// ❌ ERROR: Can't override with reified
open class Base {
    open fun <reified T> example() { }  // Error
}

// ⚠️ Limitation: Reified functions can't be overridden
```

### 5. Performance Considerations

```kotlin
// Reified functions are inlined, which can increase code size
// Use judiciously - don't inline very large functions

// ✅ Good: Small function
inline fun <reified T> getTypeName(): String = T::class.simpleName ?: "Unknown"

// ⚠️ Be careful: Large function increases code size
inline fun <reified T> largeFunction() {
    // 100+ lines of code - increases binary size at each call site
}
```

---

## Reified vs Alternatives

### Reified vs Class Parameter

```kotlin
// Without reified: Pass Class<T>
fun <T> isInstanceOf(obj: Any, clazz: Class<T>): Boolean {
    return clazz.isInstance(obj)
}

// Usage
val isString = isInstanceOf("hello", String::class.java)  // Verbose

// With reified: No Class parameter needed
inline fun <reified T> isInstanceOf(obj: Any): Boolean {
    return obj is T
}

// Usage
val isString = isInstanceOf<String>("hello")  // Clean!
```

### Reified vs Type Erasure Workarounds

```kotlin
// Old way: Type token pattern
class TypeToken<T> {
    val type: Type = (javaClass.genericSuperclass as ParameterizedType)
        .actualTypeArguments[0]
}

// Usage
val token = object : TypeToken<List<String>>() {}
val type = token.type  // Complex!

// New way: Reified
inline fun <reified T> getType(): Type {
    return object : TypeToken<T>() {}.type
}

// Usage
val type = getType<List<String>>()  // Simple!
```

---

## Best Practices

### 1. Use Reified for Type-Safe Operations

```kotlin
// ✅ Good: Type-safe logging
inline fun <reified T> T.log(message: String) {
    val tag = T::class.simpleName
    println("[$tag] $message")
}

// ✅ Good: Type-safe casting
inline fun <reified T> Any?.safeCast(): T? = this as? T
```

### 2. Keep Reified Functions Small

```kotlin
// ✅ Good: Small, focused function
inline fun <reified T> getTypeName(): String = T::class.simpleName ?: "Unknown"

// ⚠️ Avoid: Large inline functions increase code size
inline fun <reified T> largeFunction() {
    // 100+ lines - increases binary size
}
```

### 3. Use for Common Patterns

```kotlin
// ✅ Good: Common pattern (ViewModel, Intent extras, etc.)
inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

// ✅ Good: Type-safe factory
inline fun <reified T> create(): T = T::class.java.newInstance()
```

### 4. Don't Overuse

```kotlin
// ❌ Bad: Unnecessary reified
inline fun <reified T> simpleFunction(value: T): T {
    return value  // Don't need reified here!
}

// ✅ Good: Only use when you need type information
inline fun <reified T> getTypeInfo(): String {
    return T::class.simpleName ?: "Unknown"  // Need type info
}
```

### 5. Combine with Other Kotlin Features

```kotlin
// ✅ Good: Combine with extension functions
inline fun <reified T> List<*>.filterByType(): List<T> {
    return filterIsInstance<T>()
}

// ✅ Good: Combine with scope functions
inline fun <reified T> Any?.letIfInstance(block: (T) -> Unit) {
    if (this is T) block(this)
}
```

---

## Advanced Concepts

### Reified with Multiple Type Parameters

```kotlin
// Multiple reified parameters
inline fun <reified T, reified R> transform(input: T): R {
    return when {
        T::class == String::class && R::class == Int::class -> {
            (input as String).toInt() as R
        }
        else -> throw IllegalArgumentException("Unsupported transformation")
    }
}

// Usage
val number = transform<String, Int>("123")  // 123
```

### Reified with Upper Bounds

```kotlin
// Reified with type bounds
inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

// Reified with multiple bounds
inline fun <reified T> create(): T where T : Serializable, T : Cloneable {
    return T::class.java.newInstance()
}
```

### Reified in Extension Functions

```kotlin
// Reified extension function
inline fun <reified T> List<*>.filterIsInstanceOf(): List<T> {
    return filterIsInstance<T>()
}

// Usage
val mixed = listOf(1, "hello", 2, "world")
val numbers = mixed.filterIsInstanceOf<Int>()  // [1, 2]
```

### Reified with Sealed Classes

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

// Check result type
inline fun <reified T> Result<*>.isSuccessOfType(): Boolean {
    return this is Result.Success && this.data is T
}

// Usage
val result: Result<String> = Result.Success("hello")
val isStringSuccess = result.isSuccessOfType<String>()  // true
```

### Reified with Coroutines

```kotlin
// Reified in suspend function (must be inline)
inline suspend fun <reified T> fetchData(): T {
    return withContext(Dispatchers.IO) {
        // Fetch and parse data
        parseJson<T>(fetchJson())
    }
}

// Usage
val user = fetchData<User>()
```

---

## Interview Questions

### Q1: What is reified and why do we need it?

**Answer:**
- `reified` preserves generic type information at runtime
- Solves type erasure problem in JVM
- Allows access to type `T` at runtime without passing `Class<T>`
- Makes code cleaner and more type-safe
- Requires `inline` because types are known at compile time

### Q2: Why does reified require inline?

**Answer:**
- Inline functions are expanded at call sites
- Compiler knows actual type argument at compile time
- Type can be preserved and accessed at runtime
- Without inline, type is erased and unavailable at runtime

### Q3: What are the limitations of reified?

**Answer:**
- Must be used with `inline` functions
- Cannot be used in non-inline functions
- Cannot override reified functions
- Large inline functions increase code size
- Cannot store reified type in non-inline context

### Q4: How would you implement type-safe ViewModel retrieval?

**Answer:**
```kotlin
inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

// Usage
private val viewModel: MyViewModel by lazy { getViewModel<MyViewModel>() }
```

### Q5: Explain the difference between reified and passing Class<T>.

**Answer:**
- **Reified**: No parameter needed, cleaner API: `isInstanceOf<String>(obj)`
- **Class parameter**: Verbose, need to pass: `isInstanceOf(obj, String::class.java)`
- Reified is compile-time safe, Class parameter can have mismatches
- Reified requires inline, Class parameter works everywhere

### Q6: When should you NOT use reified?

**Answer:**
- When you don't need type information at runtime
- In very large functions (increases code size)
- When function can't be inline (e.g., recursive, virtual)
- When type information isn't available at compile time

### Q7: How does reified work under the hood?

**Answer:**
- Compiler inlines function at call site
- Replaces `T` with actual type argument
- Type information becomes available at runtime
- No runtime overhead (compile-time transformation)

### Q8: Can you use reified with multiple type parameters?

**Answer:**
Yes, you can have multiple reified parameters:
```kotlin
inline fun <reified T, reified R> transform(input: T): R {
    // Both T and R are available at runtime
}
```

### Q9: How would you implement a type-safe JSON parser?

**Answer:**
```kotlin
inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}

// Usage
val user = gson.fromJson<User>(jsonString)
```

### Q10: What's the performance impact of reified?

**Answer:**
- No runtime overhead (compile-time feature)
- Increases code size (function is inlined at each call site)
- Small functions: negligible impact
- Large functions: can significantly increase binary size
- Generally faster than reflection-based alternatives

---

## Code Examples

### Example 1: Type-Safe Logging

```kotlin
inline fun <reified T> T.log(message: String) {
    val tag = T::class.simpleName ?: "Unknown"
    Log.d(tag, message)
}

// Usage
class MyActivity : AppCompatActivity() {
    fun test() {
        log("Test message")  // Tag: MyActivity
    }
}
```

### Example 2: ViewModel Factory

```kotlin
inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

inline fun <reified T : ViewModel> Activity.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

// Usage
class MyFragment : Fragment() {
    private val viewModel: MyViewModel by lazy { getViewModel<MyViewModel>() }
}
```

### Example 3: Intent Extras Helper

```kotlin
inline fun <reified T> Intent.getTypedExtra(key: String): T? {
    return when (T::class) {
        String::class -> getStringExtra(key) as? T
        Int::class -> getIntExtra(key, 0) as? T
        Boolean::class -> getBooleanExtra(key, false) as? T
        Parcelable::class -> getParcelableExtra<T>(key)
        else -> getSerializableExtra(key) as? T
    }
}

// Usage
val userId: String? = intent.getTypedExtra<String>("user_id")
val isEnabled: Boolean? = intent.getTypedExtra<Boolean>("enabled")
```

### Example 4: SharedPreferences Helper

```kotlin
inline fun <reified T> SharedPreferences.getTyped(key: String, defaultValue: T): T {
    return when (T::class) {
        String::class -> getString(key, defaultValue as? String ?: "") as T
        Int::class -> getInt(key, defaultValue as? Int ?: 0) as T
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T
        Float::class -> getFloat(key, defaultValue as? Float ?: 0f) as T
        Long::class -> getLong(key, defaultValue as? Long ?: 0L) as T
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}

// Usage
val userName = prefs.getTyped<String>("user_name", "")
val userId = prefs.getTyped<Int>("user_id", 0)
```

### Example 5: Generic Factory

```kotlin
inline fun <reified T> createInstance(vararg args: Any?): T {
    val constructors = T::class.java.constructors
    val matchingConstructor = constructors.find { 
        it.parameterCount == args.size 
    }
    return matchingConstructor?.newInstance(*args) as? T
        ?: throw IllegalArgumentException("No matching constructor")
}

// Usage
val user = createInstance<User>("John", "john@example.com")
```

---

## Summary

### Key Takeaways

1. **Reified** preserves generic type information at runtime
2. **Requires `inline`** - types are known at compile time
3. **Eliminates need** for `Class<T>` parameters
4. **Makes code cleaner** and more type-safe
5. **No runtime overhead** - compile-time transformation
6. **Can increase code size** - function inlined at each call site
7. **Use judiciously** - only when you need type information

### When to Use Reified

✅ **Good use cases:**
- Type-safe logging
- ViewModel retrieval
- Intent/Bundle extras
- JSON parsing
- Type checking and casting
- Factory patterns

❌ **Avoid when:**
- Don't need type information
- Function is very large
- Can't use inline
- Type not known at compile time

---

## Practice Exercises

1. Implement type-safe ViewModel retrieval
2. Create Intent extras helper with reified
3. Build SharedPreferences helper with reified
4. Implement type-safe JSON parser
5. Create generic factory with reified
6. Build type-safe logging system
7. Implement filter by type with reified
8. Create type-safe navigation arguments helper
9. Build dependency injection helper with reified
10. Implement type-safe Room DAO accessor

---

Good luck with your interview preparation! 🚀
