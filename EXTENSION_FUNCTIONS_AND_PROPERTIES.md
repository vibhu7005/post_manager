# Extension Functions and Properties - Deep Dive

## Table of Contents
1. [Fundamentals](#fundamentals)
2. [How They Work](#how-they-work)
3. [Extension Functions](#extension-functions)
4. [Extension Properties](#extension-properties)
5. [Scope and Visibility](#scope-and-visibility)
6. [Best Practices](#best-practices)
7. [Common Android Use Cases](#common-android-use-cases)
8. [Advanced Concepts](#advanced-concepts)
9. [Interview Questions](#interview-questions)
10. [Code Examples](#code-examples)

---

## Fundamentals

### What are Extension Functions?

Extension functions allow you to add new functionality to existing classes without modifying their source code or using inheritance.

```kotlin
// Extension function syntax
fun ReceiverType.functionName(parameters): ReturnType {
    // this refers to the receiver object
    // Implementation
}

// Example
fun String.removeWhitespace(): String {
    return this.replace(" ", "")
}

// Usage
val result = "Hello World".removeWhitespace() // "HelloWorld"
```

### What are Extension Properties?

Extension properties allow you to add properties to existing classes, but they cannot have backing fields (no `field` keyword).

```kotlin
// Extension property syntax
val ReceiverType.propertyName: Type
    get() = // implementation

var ReceiverType.propertyName: Type
    get() = // implementation
    set(value) { // implementation }

// Example
val String.wordCount: Int
    get() = this.split(" ").size

// Usage
val count = "Hello World".wordCount // 2
```

---

## How They Work

### Under the Hood

Extension functions are **static functions** that take the receiver object as the first parameter. They're compiled to regular functions.

```kotlin
// What you write
fun String.lastChar(): Char = this[this.length - 1]

// What gets compiled (simplified)
fun lastChar(receiver: String): Char = receiver[receiver.length - 1]

// Usage is the same
val char = "Hello".lastChar() // 'o'
```

### Key Points

1. **No actual modification**: Extensions don't modify the original class
2. **Static dispatch**: Resolved at compile-time based on declared type
3. **No overriding**: Cannot override member functions
4. **Receiver object**: `this` refers to the receiver instance

---

## Extension Functions

### Basic Syntax

```kotlin
// Simple extension function
fun String.isEmail(): Boolean {
    return this.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
}

// Usage
if ("user@example.com".isEmail()) {
    // Valid email
}
```

### Nullable Receivers

```kotlin
// Extension on nullable type
fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

// Safe call operator still works
val result = nullableString?.isNullOrEmpty()
```

### Generic Extension Functions

```kotlin
// Generic extension function
fun <T> List<T>.secondOrNull(): T? {
    return if (this.size >= 2) this[1] else null
}

// Usage
val second = listOf(1, 2, 3).secondOrNull() // 2
val empty = emptyList<Int>().secondOrNull() // null
```

### Infix Extension Functions

```kotlin
// Infix extension function
infix fun Int.pow(exponent: Int): Int {
    return Math.pow(this.toDouble(), exponent.toDouble()).toInt()
}

// Usage (can omit dot and parentheses)
val result = 2 pow 3 // 8
// Or traditional: 2.pow(3)
```

### Operator Overloading with Extensions

```kotlin
// Extension operator function
operator fun String.times(count: Int): String {
    return this.repeat(count)
}

// Usage
val repeated = "Hello" * 3 // "HelloHelloHello"
```

---

## Extension Properties

### Read-Only Extension Properties

```kotlin
val String.firstChar: Char
    get() = this[0]

val List<Int>.sum: Int
    get() = this.fold(0) { acc, value -> acc + value }
```

### Mutable Extension Properties

```kotlin
var StringBuilder.lastChar: Char
    get() = this[this.length - 1]
    set(value) {
        this.setCharAt(this.length - 1, value)
    }

// Usage
val sb = StringBuilder("Hello")
sb.lastChar = '!' // "Hell!"
```

### Important Limitation

Extension properties **cannot have backing fields**:

```kotlin
// ❌ This won't compile
var String.cachedValue: String
    get() = field // ERROR: Extension properties cannot have backing fields
    set(value) { field = value }
```

### Workaround for Backing Fields

```kotlin
// Use a map or other storage mechanism
private val cache = mutableMapOf<String, String>()

var String.cachedValue: String
    get() = cache[this] ?: ""
    set(value) { cache[this] = value }
```

---

## Scope and Visibility

### File-Level Extensions

```kotlin
// In StringExtensions.kt
fun String.removeSpaces(): String = this.replace(" ", "")

// Can be used anywhere StringExtensions.kt is imported
```

### Package-Level Extensions

```kotlin
// In com.example.utils package
package com.example.utils

fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { 
        it.capitalize() 
    }
}
```

### Member Extensions

```kotlin
class Host {
    fun String.removeSpaces(): String = this.replace(" ", "")
    
    fun test() {
        "Hello World".removeSpaces() // Works
    }
}

fun test() {
    "Hello World".removeSpaces() // ERROR: Not accessible
}
```

### Importing Extensions

```kotlin
// Import specific extension
import com.example.utils.removeSpaces

// Import all extensions from package
import com.example.utils.*

// Import with alias
import com.example.utils.removeSpaces as removeSpacesCustom
```

---

## Best Practices

### 1. Use Extensions for Utility Functions

```kotlin
// ✅ Good: Utility function
fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { 
        it.capitalize() 
    }
}

// ❌ Bad: Business logic
fun String.processOrder(): Order {
    // Business logic doesn't belong in extensions
}
```

### 2. Keep Extensions Focused

```kotlin
// ✅ Good: Single responsibility
fun String.isValidEmail(): Boolean { /* ... */ }
fun String.isValidPhone(): Boolean { /* ... */ }

// ❌ Bad: Too many responsibilities
fun String.validateEverything(): ValidationResult { /* ... */ }
```

### 3. Use Meaningful Names

```kotlin
// ✅ Good: Clear and descriptive
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }

// ❌ Bad: Unclear
fun View.doit() { visibility = View.VISIBLE }
```

### 4. Document Complex Extensions

```kotlin
/**
 * Converts a string to a URL-safe slug.
 * 
 * @return A slug version of the string with lowercase letters,
 *         numbers, and hyphens only.
 * 
 * @example
 * "Hello World!".toSlug() // "hello-world"
 */
fun String.toSlug(): String {
    return this.lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}
```

### 5. Avoid Shadowing Member Functions

```kotlin
// ❌ Bad: Shadows existing function
fun String.length(): Int {
    return this.length // ERROR: Recursive call
}

// ✅ Good: Different name
fun String.wordCount(): Int {
    return this.split(" ").size
}
```

### 6. Consider Performance

```kotlin
// ✅ Good: Efficient
fun String.isEmail(): Boolean {
    return EMAIL_REGEX.matches(this)
}

// ❌ Bad: Creates regex every time
fun String.isEmail(): Boolean {
    return Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(this)
}
```

---

## Common Android Use Cases

### View Extensions

```kotlin
// Visibility extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

// Usage
textView.show()
button.hide()
```

### Context Extensions

```kotlin
// Toast extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes messageRes: Int) {
    Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
}

// Usage
context.showToast("Hello")
context.showToast(R.string.hello)
```

### Fragment Extensions

```kotlin
// Fragment extensions
fun Fragment.showToast(message: String) {
    requireContext().showToast(message)
}

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}
```

### Activity Extensions

```kotlin
// Activity extensions
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}

fun Activity.showKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}
```

### Collection Extensions

```kotlin
// List extensions
fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null
fun <T> List<T>.second(): T = this[1]

// Map extensions
fun <K, V> Map<K, V>.getOrThrow(key: K): V {
    return this[key] ?: throw NoSuchElementException("Key $key not found")
}
```

### Date/Time Extensions

```kotlin
// Date extensions
fun Long.toDateString(format: String = "yyyy-MM-dd"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.toDate(format: String = "yyyy-MM-dd"): Date? {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return try {
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}
```

### Resource Extensions

```kotlin
// Context resource extensions
fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}
```

### Coroutine Extensions

```kotlin
// ViewModel extensions
fun ViewModel.launch(
    context: CoroutineContext = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch(context) {
        block()
    }
}

// Usage
viewModel.launch {
    val result = repository.getData()
    _uiState.value = result
}
```

---

## Advanced Concepts

### Extension Functions vs Member Functions

```kotlin
class MyClass {
    fun memberFunction() { } // Member function
}

fun MyClass.extensionFunction() { } // Extension function

// Resolution priority: Member functions win
class MyClass {
    fun test() { }
}

fun MyClass.test() { } // Won't be called if member exists
```

### Extension Functions on Companion Objects

```kotlin
class MyClass {
    companion object
}

fun MyClass.Companion.create(): MyClass {
    return MyClass()
}

// Usage
val instance = MyClass.create()
```

### Extension Functions on Generic Types

```kotlin
// Extension on generic type
fun <T> List<T>.filterNotNull(): List<T> {
    return this.filter { it != null } as List<T>
}

// Extension with type constraints
fun <T : Comparable<T>> List<T>.sorted(): List<T> {
    return this.sorted()
}
```

### Extension Functions with Receivers

```kotlin
// Extension function that takes another extension as parameter
fun <T, R> T.let(block: (T) -> R): R = block(this)

// Higher-order extension function
fun <T> T.apply(block: T.() -> Unit): T {
    block()
    return this
}
```

### Scope Functions as Extensions

```kotlin
// let, run, with, apply, also are all extension functions

// let: Returns result of lambda
val result = "Hello".let { it.length }

// run: Returns result of lambda, 'this' context
val result = "Hello".run { length }

// apply: Returns receiver, 'this' context
val result = StringBuilder().apply {
    append("Hello")
    append("World")
}

// also: Returns receiver, 'it' context
val result = "Hello".also { println(it) }
```

---

## Interview Questions

### Q1: Explain how extension functions work under the hood

**Answer:**
Extension functions are compiled to static functions that take the receiver object as the first parameter. They don't modify the original class and use static dispatch (resolved at compile-time).

```kotlin
// Source code
fun String.lastChar(): Char = this[this.length - 1]

// Compiled (simplified)
fun lastChar(receiver: String): Char = receiver[receiver.length - 1]

// Call site
"Hello".lastChar()
// Becomes: lastChar("Hello")
```

### Q2: What's the difference between extension functions and member functions?

**Answer:**
- **Member functions**: Defined inside the class, can access private members, can be overridden
- **Extension functions**: Defined outside the class, cannot access private members, cannot override members, resolved statically

**Priority**: Member functions always take precedence over extension functions.

### Q3: Can extension functions override member functions?

**Answer:**
No. Extension functions cannot override member functions. If a member function exists with the same signature, it will always be called instead of the extension function.

### Q4: Why can't extension properties have backing fields?

**Answer:**
Extension properties don't actually add storage to the class - they're just syntactic sugar for getter/setter functions. Since there's no actual field in the class, you can't use the `field` keyword.

**Workaround**: Use external storage (maps, companion objects, etc.)

### Q5: How do you handle extension function conflicts?

**Answer:**
1. Use fully qualified names: `com.example.utils.removeSpaces()`
2. Use import aliases: `import com.example.utils.removeSpaces as customRemoveSpaces`
3. Organize extensions in separate files/packages
4. Use member extensions for scoped access

### Q6: When should you use extension functions vs utility functions?

**Answer:**
**Use extensions when:**
- The function logically belongs to the type
- It improves readability: `string.isEmail()` vs `isEmail(string)`
- It chains well: `string.trim().capitalize().removeSpaces()`

**Use utility functions when:**
- The function operates on multiple types
- It's a general-purpose utility
- It doesn't conceptually belong to one type

### Q7: Explain the difference between `let`, `run`, `with`, `apply`, and `also`

**Answer:**

| Function | Receiver | Returns | Use Case |
|----------|----------|---------|----------|
| `let` | `it` | Lambda result | Null checks, transformations |
| `run` | `this` | Lambda result | Object configuration, computing result |
| `with` | `this` | Lambda result | Non-extension, multiple operations |
| `apply` | `this` | Receiver | Object initialization |
| `also` | `it` | Receiver | Side effects, logging |

### Q8: How would you implement a safe navigation extension?

**Answer:**

```kotlin
inline fun <T, R> T.safeLet(block: (T) -> R): R? {
    return if (this != null) block(this) else null
}

// Usage
val result = nullableString.safeLet { it.length }
```

### Q9: Design an extension function for debouncing

**Answer:**

```kotlin
fun <T> Flow<T>.debounce(timeoutMillis: Long): Flow<T> {
    return this.debounce(timeoutMillis)
}

// Or for callbacks
fun View.onClickDebounced(
    debounceTime: Long = 300L,
    action: (View) -> Unit
) {
    var lastClickTime = 0L
    this.setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            action(view)
        }
    }
}
```

### Q10: How do extension functions affect performance?

**Answer:**
- **Compile-time**: No performance impact - they're compiled to regular functions
- **Runtime**: Same performance as regular functions
- **Memory**: No additional memory overhead
- **Considerations**: 
  - Avoid creating objects in frequently called extensions
  - Cache expensive computations
  - Be mindful of inline functions

---

## Code Examples

### Example 1: View Extensions

```kotlin
// ViewExtensions.kt
package com.example.extensions

import android.view.View
import androidx.core.view.isVisible

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.toggle() {
    visibility = if (isVisible) View.GONE else View.VISIBLE
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE
```

### Example 2: String Extensions

```kotlin
// StringExtensions.kt
package com.example.extensions

import java.util.regex.Pattern

private val EMAIL_PATTERN = Pattern.compile(
    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
    Pattern.CASE_INSENSITIVE
)

fun String.isEmail(): Boolean {
    return EMAIL_PATTERN.matcher(this).matches()
}

fun String.isValidPhone(): Boolean {
    return this.matches(Regex("^[+]?[0-9]{10,15}$"))
}

fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { 
        it.lowercase().capitalize() 
    }
}

fun String.removeWhitespace(): String {
    return this.replace("\\s".toRegex(), "")
}
```

### Example 3: Collection Extensions

```kotlin
// CollectionExtensions.kt
package com.example.extensions

fun <T> List<T>.secondOrNull(): T? {
    return if (size >= 2) this[1] else null
}

fun <T> List<T>.second(): T {
    require(size >= 2) { "List must have at least 2 elements" }
    return this[1]
}

fun <T> List<T>.penultimate(): T? {
    return if (size >= 2) this[size - 2] else null
}

fun <K, V> Map<K, V>.getOrThrow(key: K): V {
    return this[key] ?: throw NoSuchElementException("Key $key not found")
}
```

### Example 4: Context Extensions

```kotlin
// ContextExtensions.kt
package com.example.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes messageRes: Int) {
    Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
}

fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int) = 
    ContextCompat.getDrawable(this, drawableRes)

fun Context.getStringCompat(@StringRes stringRes: Int) = 
    getString(stringRes)
```

### Example 5: Date/Time Extensions

```kotlin
// DateExtensions.kt
package com.example.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return try {
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}

fun Date.toString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(this)
}
```

### Example 6: Coroutine Extensions

```kotlin
// CoroutineExtensions.kt
package com.example.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

fun ViewModel.launch(
    context: CoroutineContext = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch(context) {
        block()
    }
}
```

### Example 7: Resource Extensions

```kotlin
// ResourceExtensions.kt
package com.example.extensions

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

fun Resources.getQuantityStringCompat(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: Any
): String {
    return getQuantityString(id, quantity, *formatArgs)
}
```

### Example 8: Fragment Extensions

```kotlin
// FragmentExtensions.kt
package com.example.extensions

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(InputMethodManager::class.java)
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun Fragment.showKeyboard(view: View) {
    val imm = requireContext().getSystemService(InputMethodManager::class.java)
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun <T> Fragment.observe(liveData: LiveData<T>, observer: Observer<T>) {
    liveData.observe(viewLifecycleOwner, observer)
}
```

### Example 9: Activity Extensions

```kotlin
// ActivityExtensions.kt
package com.example.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

fun Activity.hideKeyboard() {
    val imm = getSystemService(InputMethodManager::class.java)
    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}

fun Activity.showKeyboard(view: View) {
    val imm = getSystemService(InputMethodManager::class.java)
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}
```

### Example 10: Advanced Extension with Generics

```kotlin
// GenericExtensions.kt
package com.example.extensions

inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
    if (condition) {
        block()
    }
    return this
}

inline fun <T> T.alsoIf(condition: Boolean, block: (T) -> Unit): T {
    if (condition) {
        block(this)
    }
    return this
}

fun <T> List<T>.replace(oldValue: T, newValue: T): List<T> {
    return this.map { if (it == oldValue) newValue else it }
}

fun <T> List<T>.replaceAt(index: Int, newValue: T): List<T> {
    return this.mapIndexed { i, value -> if (i == index) newValue else value }
}
```

---

## Common Pitfalls and How to Avoid Them

### Pitfall 1: Shadowing Member Functions

```kotlin
// ❌ Bad
fun String.length(): Int {
    return this.length // ERROR: Recursive call
}

// ✅ Good
fun String.wordCount(): Int {
    return this.split(" ").size
}
```

### Pitfall 2: Extension Properties with Backing Fields

```kotlin
// ❌ Bad - Won't compile
var String.cachedValue: String
    get() = field
    set(value) { field = value }

// ✅ Good - Use external storage
private val cache = mutableMapOf<String, String>()
var String.cachedValue: String
    get() = cache[this] ?: ""
    set(value) { cache[this] = value }
```

### Pitfall 3: Performance Issues

```kotlin
// ❌ Bad - Creates regex every time
fun String.isEmail(): Boolean {
    return Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(this)
}

// ✅ Good - Cached regex
private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
fun String.isEmail(): Boolean {
    return EMAIL_REGEX.matches(this)
}
```

### Pitfall 4: Overusing Extensions

```kotlin
// ❌ Bad - Business logic in extension
fun String.processOrder(): Order {
    // Complex business logic doesn't belong here
}

// ✅ Good - Utility function
fun String.isValidEmail(): Boolean {
    // Simple, focused utility
}
```

---

## Summary

### Key Takeaways

1. **Extension functions** add functionality without modifying classes
2. **Extension properties** cannot have backing fields
3. **Static dispatch** - resolved at compile-time
4. **Member functions** always take precedence
5. **Use extensions** for utilities that logically belong to a type
6. **Organize extensions** in separate files/packages
7. **Document complex extensions** for maintainability
8. **Consider performance** - cache expensive operations

### When to Use Extensions

✅ **Good use cases:**
- Utility functions (`String.isEmail()`)
- View helpers (`View.show()`)
- Resource access (`Context.getColorCompat()`)
- Collection operations (`List.secondOrNull()`)
- Type conversions (`Long.toDateString()`)

❌ **Avoid for:**
- Business logic
- Complex operations
- Functions that don't logically belong to the type
- Functions that need access to private members

---

## Practice Exercises

1. Create extension functions for `View` to handle visibility states
2. Create extension functions for `String` validation (email, phone, URL)
3. Create extension properties for `List` to get second, third, last elements
4. Create extension functions for `Context` to show toasts and get resources
5. Create extension functions for date/time formatting
6. Create extension functions for coroutines in ViewModel
7. Create a debounced click listener extension for `View`
8. Create extension functions for safe navigation (null handling)

---

Good luck with your interview preparation! 🚀
