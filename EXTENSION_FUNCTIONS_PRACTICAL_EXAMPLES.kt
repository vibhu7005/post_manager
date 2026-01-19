// Practical Extension Functions for Android Development
// Use these examples to understand and practice extension functions

package com.example.demoapplication.extensions

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// 1. VIEW EXTENSIONS
// ============================================================================

/**
 * Show a view (set visibility to VISIBLE)
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Hide a view (set visibility to GONE)
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Make a view invisible (set visibility to INVISIBLE)
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Toggle visibility between VISIBLE and GONE
 */
fun View.toggle() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Check if view is visible
 */
fun View.isVisible(): Boolean = visibility == View.VISIBLE

/**
 * Check if view is gone
 */
fun View.isGone(): Boolean = visibility == View.GONE

/**
 * Set visibility based on boolean
 */
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

// ============================================================================
// 2. CONTEXT EXTENSIONS
// ============================================================================

/**
 * Show a toast message
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Show a toast message from string resource
 */
fun Context.showToast(@StringRes messageRes: Int) {
    Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
}

/**
 * Get color from resources (compatible with all Android versions)
 */
fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

/**
 * Get drawable from resources (compatible with all Android versions)
 */
fun Context.getDrawableCompat(@DrawableRes drawableRes: Int) = 
    ContextCompat.getDrawable(this, drawableRes)

/**
 * Get string from resources
 */
fun Context.getStringCompat(@StringRes stringRes: Int) = getString(stringRes)

/**
 * Get string from resources with format arguments
 */
fun Context.getStringCompat(@StringRes stringRes: Int, vararg formatArgs: Any) = 
    getString(stringRes, *formatArgs)

// ============================================================================
// 3. STRING EXTENSIONS
// ============================================================================

/**
 * Check if string is a valid email
 */
fun String.isEmail(): Boolean {
    val emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$".toRegex(RegexOption.IGNORE_CASE)
    return emailRegex.matches(this)
}

/**
 * Check if string is a valid phone number
 */
fun String.isValidPhone(): Boolean {
    return this.matches(Regex("^[+]?[0-9]{10,15}$"))
}

/**
 * Convert string to title case
 */
fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { 
        it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
    }
}

/**
 * Remove all whitespace from string
 */
fun String.removeWhitespace(): String {
    return this.replace("\\s".toRegex(), "")
}

/**
 * Capitalize first letter of each word
 */
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { 
        it.replaceFirstChar { char -> char.uppercaseChar() }
    }
}

/**
 * Convert string to URL-safe slug
 */
fun String.toSlug(): String {
    return this.lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

/**
 * Truncate string to max length with ellipsis
 */
fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (this.length > maxLength) {
        this.take(maxLength - suffix.length) + suffix
    } else {
        this
    }
}

/**
 * Check if string is blank or null (extension on nullable String)
 */
fun String?.isNullOrBlank(): Boolean {
    return this == null || this.isBlank()
}

// ============================================================================
// 4. COLLECTION EXTENSIONS
// ============================================================================

/**
 * Get second element or null
 */
fun <T> List<T>.secondOrNull(): T? {
    return if (size >= 2) this[1] else null
}

/**
 * Get second element or throw exception
 */
fun <T> List<T>.second(): T {
    require(size >= 2) { "List must have at least 2 elements" }
    return this[1]
}

/**
 * Get penultimate (second to last) element
 */
fun <T> List<T>.penultimate(): T? {
    return if (size >= 2) this[size - 2] else null
}

/**
 * Get element from map or throw exception
 */
fun <K, V> Map<K, V>.getOrThrow(key: K): V {
    return this[key] ?: throw NoSuchElementException("Key $key not found")
}

/**
 * Replace element in list
 */
fun <T> List<T>.replace(oldValue: T, newValue: T): List<T> {
    return this.map { if (it == oldValue) newValue else it }
}

/**
 * Replace element at index
 */
fun <T> List<T>.replaceAt(index: Int, newValue: T): List<T> {
    return this.mapIndexed { i, value -> if (i == index) newValue else value }
}

// ============================================================================
// 5. DATE/TIME EXTENSIONS
// ============================================================================

/**
 * Convert Long timestamp to formatted date string
 */
fun Long.toDateString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Convert String to Date
 */
fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return try {
        sdf.parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert Date to formatted string
 */
fun Date.toString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(this)
}

/**
 * Check if date is today
 */
fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { time = this@isToday }
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

/**
 * Get days difference between dates
 */
fun Date.daysUntil(other: Date): Long {
    val diff = other.time - this.time
    return diff / (24 * 60 * 60 * 1000)
}

// ============================================================================
// 6. NUMBER EXTENSIONS
// ============================================================================

/**
 * Format number as currency
 */
fun Double.toCurrency(currency: String = "$"): String {
    return "$currency${String.format("%.2f", this)}"
}

/**
 * Format number with thousand separators
 */
fun Long.formatWithSeparator(): String {
    return String.format(Locale.getDefault(), "%,d", this)
}

/**
 * Convert Int to Boolean (0 = false, non-zero = true)
 */
fun Int.toBoolean(): Boolean = this != 0

/**
 * Convert Boolean to Int (true = 1, false = 0)
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * Clamp value between min and max
 */
fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

// ============================================================================
// 7. LIVEDATA EXTENSIONS
// ============================================================================

/**
 * Observe LiveData once (automatically removes observer after first emission)
 */
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

/**
 * Observe LiveData with null safety
 */
fun <T> LiveData<T>.observeNonNull(
    owner: LifecycleOwner,
    observer: (T) -> Unit
) {
    observe(owner) { value ->
        value?.let(observer)
    }
}

// ============================================================================
// 8. FLOW EXTENSIONS
// ============================================================================

/**
 * Collect Flow with null safety
 */
suspend fun <T> Flow<T?>.collectNonNull(action: suspend (T) -> Unit) {
    collect { value ->
        value?.let(action)
    }
}

// ============================================================================
// 9. GENERIC UTILITY EXTENSIONS
// ============================================================================

/**
 * Apply block if condition is true
 */
inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
    if (condition) {
        block()
    }
    return this
}

/**
 * Also block if condition is true
 */
inline fun <T> T.alsoIf(condition: Boolean, block: (T) -> Unit): T {
    if (condition) {
        block(this)
    }
    return this
}

/**
 * Safe let - only executes if not null
 */
inline fun <T, R> T?.safeLet(block: (T) -> R): R? {
    return if (this != null) block(this) else null
}

/**
 * Execute block if this is null
 */
inline fun <T> T?.ifNull(block: () -> Unit): T? {
    if (this == null) block()
    return this
}

/**
 * Execute block if this is not null
 */
inline fun <T> T?.ifNotNull(block: (T) -> Unit): T? {
    this?.let(block)
    return this
}

// ============================================================================
// 10. ANDROID-SPECIFIC EXTENSIONS
// ============================================================================

/**
 * Convert dp to pixels
 */
fun Int.dpToPx(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this * density).toInt()
}

/**
 * Convert pixels to dp
 */
fun Int.pxToDp(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this / density).toInt()
}

/**
 * Convert Float dp to pixels
 */
fun Float.dpToPx(context: Context): Float {
    val density = context.resources.displayMetrics.density
    return this * density
}

/**
 * Convert Float pixels to dp
 */
fun Float.pxToDp(context: Context): Float {
    val density = context.resources.displayMetrics.density
    return this / density
}

// ============================================================================
// 11. PRACTICE EXERCISES
// ============================================================================

/**
 * Exercise 1: Create an extension function for String that checks if it's a valid URL
 */
fun String.isValidUrl(): Boolean {
    return try {
        java.net.URL(this)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Exercise 2: Create an extension function for List that returns a random element
 */
fun <T> List<T>.randomOrNull(): T? {
    return if (isEmpty()) null else this[Random().nextInt(size)]
}

/**
 * Exercise 3: Create an extension function for Int that formats as time (MM:SS)
 */
fun Int.toTimeString(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Exercise 4: Create an extension property for String that returns word count
 */
val String.wordCount: Int
    get() = this.split("\\s+".toRegex()).filter { it.isNotBlank() }.size

/**
 * Exercise 5: Create an extension function for View that adds click debouncing
 */
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

// ============================================================================
// 12. ADVANCED EXAMPLES
// ============================================================================

/**
 * Extension function with receiver type parameter
 */
inline fun <T> T.applyIfNotNull(block: T.() -> Unit): T {
    block()
    return this
}

/**
 * Extension function on nullable type with safe call
 */
fun String?.orEmpty(): String = this ?: ""

/**
 * Extension function that returns receiver or default
 */
fun <T> T?.orElse(default: T): T = this ?: default

/**
 * Extension function for chaining operations
 */
fun <T> T.chain(vararg operations: (T) -> T): T {
    return operations.fold(this) { acc, op -> op(acc) }
}

/**
 * Extension function for conditional transformation
 */
inline fun <T> T.transformIf(
    condition: Boolean,
    transform: T.() -> T
): T = if (condition) transform() else this

// ============================================================================
// USAGE EXAMPLES
// ============================================================================

fun usageExamples() {
    // View extensions
    val view: View? = null
    view?.show()
    view?.hide()
    view?.toggle()
    
    // String extensions
    val email = "user@example.com"
    if (email.isEmail()) {
        println("Valid email")
    }
    
    val title = "hello world"
    println(title.toTitleCase()) // "Hello World"
    
    // Collection extensions
    val list = listOf(1, 2, 3, 4, 5)
    println(list.secondOrNull()) // 2
    println(list.penultimate()) // 4
    
    // Date extensions
    val timestamp = System.currentTimeMillis()
    println(timestamp.toDateString("yyyy-MM-dd")) // "2024-01-15"
    
    // Number extensions
    val price = 1234.56
    println(price.toCurrency()) // "$1234.56"
    
    // Generic extensions
    val value = "Hello"
    value.applyIf(true) {
        println(this.uppercase())
    }
    
    // Chain operations
    "hello world"
        .chain(
            { it.uppercase() },
            { it.replace(" ", "_") },
            { it + "!" }
        ) // "HELLO_WORLD!"
}
