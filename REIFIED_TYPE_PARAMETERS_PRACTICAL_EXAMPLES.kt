// Practical Reified Type Parameters Examples for Android Development
// Use these examples to understand and practice reified type parameters

package com.example.demoapplication.reified

import android.content.Intent
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import kotlin.reflect.KClass
import java.lang.reflect.Type

// ============================================================================
// 1. BASIC REIFIED USAGE
// ============================================================================

/**
 * Basic reified type checking
 */
inline fun <reified T> isInstanceOf(obj: Any): Boolean {
    return obj is T
}

fun basicExample() {
    val isString = isInstanceOf<String>("hello")  // true
    val isInt = isInstanceOf<Int>("hello")        // false
    val isNumber = isInstanceOf<Number>(5)       // true
}

/**
 * Get class information with reified
 */
inline fun <reified T> getClass(): KClass<T> {
    return T::class
}

inline fun <reified T> getJavaClass(): Class<T> {
    return T::class.java
}

fun classInfoExample() {
    val kClass = getClass<String>()      // KClass<String>
    val javaClass = getJavaClass<String>() // Class<String>
    println("Simple name: ${kClass.simpleName}")  // "String"
}

/**
 * Create instance with reified (requires no-arg constructor)
 */
inline fun <reified T> createInstance(): T {
    return T::class.java.newInstance()
}

fun createInstanceExample() {
    val string = createInstance<String>()  // Creates new String()
    val list = createInstance<ArrayList<String>>()  // Creates new ArrayList()
}

// ============================================================================
// 2. TYPE-SAFE LOGGING
// ============================================================================

/**
 * Type-safe logging with reified
 */
inline fun <reified T> T.log(message: String) {
    val tag = T::class.simpleName ?: "Unknown"
    println("[$tag] $message")
}

fun loggingExample() {
    class MyActivity {
        fun test() {
            log("Test message")  // [MyActivity] Test message
        }
    }
    
    class MyFragment {
        fun test() {
            log("Fragment test")  // [MyFragment] Fragment test
        }
    }
}

/**
 * Log with custom tag prefix
 */
inline fun <reified T> T.logWithPrefix(prefix: String, message: String) {
    val tag = T::class.simpleName ?: "Unknown"
    println("[$prefix:$tag] $message")
}

// ============================================================================
// 3. TYPE-SAFE CASTING
// ============================================================================

/**
 * Safe cast with reified
 */
inline fun <reified T> Any?.safeCast(): T? {
    return this as? T
}

fun castingExample() {
    val value: Any = "hello"
    val string: String? = value.safeCast<String>()  // "hello"
    val number: Int? = value.safeCast<Int>()         // null
    
    val list: Any = listOf(1, 2, 3)
    val intList: List<Int>? = list.safeCast<List<Int>>()
}

/**
 * Cast or throw with reified
 */
inline fun <reified T> Any.castOrThrow(): T {
    return this as? T ?: throw ClassCastException("Cannot cast ${this::class} to ${T::class}")
}

// ============================================================================
// 4. FILTER BY TYPE
// ============================================================================

/**
 * Filter list by type with reified
 */
inline fun <reified T> List<*>.filterIsInstanceOf(): List<T> {
    return filterIsInstance<T>()
}

fun filterExample() {
    val mixed = listOf(1, "hello", 2, "world", 3, true)
    val numbers = mixed.filterIsInstanceOf<Int>()      // [1, 2, 3]
    val strings = mixed.filterIsInstanceOf<String>()   // ["hello", "world"]
    val booleans = mixed.filterIsInstanceOf<Boolean>()  // [true]
}

/**
 * Find first instance of type
 */
inline fun <reified T> List<*>.findInstanceOf(): T? {
    return findIsInstance<T>()
}

inline fun <reified T> List<*>.findIsInstance(): T? {
    return filterIsInstance<T>().firstOrNull()
}

// ============================================================================
// 5. ANDROID-SPECIFIC: VIEWMODEL
// ============================================================================

/**
 * Get ViewModel with reified type
 */
inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

inline fun <reified T : ViewModel> androidx.fragment.app.FragmentActivity.getViewModel(): T {
    return ViewModelProvider(this)[T::class.java]
}

// Usage example
class MyViewModel : ViewModel() {
    fun loadData() {}
}

class MyFragment : Fragment() {
    // Type-safe ViewModel retrieval
    private val viewModel: MyViewModel by lazy { getViewModel<MyViewModel>() }
    
    override fun onViewCreated() {
        super.onViewCreated()
        viewModel.loadData()
    }
}

// ============================================================================
// 6. ANDROID-SPECIFIC: INTENT EXTRAS
// ============================================================================

/**
 * Get typed extra from Intent
 */
inline fun <reified T> Intent.getTypedExtra(key: String): T? {
    return when (T::class) {
        String::class -> getStringExtra(key) as? T
        Int::class -> getIntExtra(key, 0) as? T
        Boolean::class -> getBooleanExtra(key, false) as? T
        Float::class -> getFloatExtra(key, 0f) as? T
        Long::class -> getLongExtra(key, 0L) as? T
        Double::class -> getDoubleExtra(key, 0.0) as? T
        else -> getSerializableExtra(key) as? T
    }
}

fun intentExample() {
    val intent = Intent()
    intent.putExtra("user_id", "123")
    intent.putExtra("is_enabled", true)
    
    val userId: String? = intent.getTypedExtra<String>("user_id")
    val isEnabled: Boolean? = intent.getTypedExtra<Boolean>("is_enabled")
}

/**
 * Get typed extra with default value
 */
inline fun <reified T> Intent.getTypedExtra(key: String, defaultValue: T): T {
    return getTypedExtra<T>(key) ?: defaultValue
}

// ============================================================================
// 7. ANDROID-SPECIFIC: SHAREDPREFERENCES
// ============================================================================

/**
 * Get typed preference with reified
 */
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

/**
 * Put typed preference with reified
 */
inline fun <reified T> SharedPreferences.Editor.putTyped(key: String, value: T): SharedPreferences.Editor {
    return when (T::class) {
        String::class -> putString(key, value as String)
        Int::class -> putInt(key, value as Int)
        Boolean::class -> putBoolean(key, value as Boolean)
        Float::class -> putFloat(key, value as Float)
        Long::class -> putLong(key, value as Long)
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}

fun preferencesExample(prefs: SharedPreferences) {
    // Get typed values
    val userName = prefs.getTyped<String>("user_name", "")
    val userId = prefs.getTyped<Int>("user_id", 0)
    val isEnabled = prefs.getTyped<Boolean>("is_enabled", false)
    
    // Put typed values
    prefs.edit()
        .putTyped("user_name", "John")
        .putTyped("user_id", 123)
        .putTyped("is_enabled", true)
        .apply()
}

// ============================================================================
// 8. ANDROID-SPECIFIC: FRAGMENT ARGUMENTS
// ============================================================================

/**
 * Get typed argument from Fragment
 */
inline fun <reified T> Fragment.argument(key: String): T? {
    return arguments?.get(key) as? T
}

/**
 * Get typed argument with default
 */
inline fun <reified T> Fragment.argument(key: String, defaultValue: T): T {
    return argument<T>(key) ?: defaultValue
}

fun fragmentExample() {
    class DetailFragment : Fragment() {
        private val itemId: String by lazy { argument<String>("item_id") ?: "" }
        private val userId: Int by lazy { argument<Int>("user_id", 0) }
    }
}

// ============================================================================
// 9. JSON PARSING WITH REIFIED
// ============================================================================

/**
 * Parse JSON to type T with reified
 */
inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}

/**
 * Parse JSON array to List<T>
 */
inline fun <reified T> Gson.fromJsonArray(json: String): List<T> {
    val listType = object : com.google.gson.reflect.TypeToken<List<T>>() {}.type
    return fromJson<List<T>>(json, listType)
}

data class User(val name: String, val email: String)

fun jsonExample() {
    val gson = Gson()
    val jsonString = """{"name":"John","email":"john@example.com"}"""
    
    // Parse to User
    val user = gson.fromJson<User>(jsonString)
    
    // Parse array
    val jsonArray = """[{"name":"John","email":"john@example.com"}]"""
    val users = gson.fromJsonArray<User>(jsonArray)
}

// ============================================================================
// 10. GENERIC FACTORY WITH REIFIED
// ============================================================================

/**
 * Create instance with constructor arguments
 */
inline fun <reified T> createInstance(vararg args: Any?): T {
    val constructors = T::class.java.constructors
    val matchingConstructor = constructors.find { 
        it.parameterCount == args.size 
    }
    return matchingConstructor?.newInstance(*args) as? T
        ?: throw IllegalArgumentException("No matching constructor for ${T::class}")
}

data class Person(val name: String, val age: Int)

fun factoryExample() {
    // Create with arguments
    val person = createInstance<Person>("John", 30)
    
    // Create without arguments (no-arg constructor)
    val string = createInstance<String>()
}

// ============================================================================
// 11. TYPE-SAFE RETROFIT RESPONSE
// ============================================================================

/**
 * Get typed body from Retrofit Response
 */
inline fun <reified T> retrofit2.Response<*>.bodyAs(): T? {
    return body() as? T
}

/**
 * Get typed body or throw
 */
inline fun <reified T> retrofit2.Response<*>.bodyAsOrThrow(): T {
    return bodyAs<T>() ?: throw IllegalStateException("Response body is null or not of type ${T::class}")
}

fun retrofitExample() {
    // Simulated response
    val response = retrofit2.Response.success(User("John", "john@example.com"))
    
    val user: User? = response.bodyAs<User>()
    val user2: User = response.bodyAsOrThrow<User>()
}

// ============================================================================
// 12. TYPE-SAFE BUNDLE EXTRAS
// ============================================================================

/**
 * Get typed value from Bundle
 */
inline fun <reified T> android.os.Bundle.getTyped(key: String): T? {
    return when (T::class) {
        String::class -> getString(key) as? T
        Int::class -> getInt(key) as? T
        Boolean::class -> getBoolean(key) as? T
        Float::class -> getFloat(key) as? T
        Long::class -> getLong(key) as? T
        else -> getSerializable(key) as? T
    }
}

/**
 * Put typed value to Bundle
 */
inline fun <reified T> android.os.Bundle.putTyped(key: String, value: T) {
    when (T::class) {
        String::class -> putString(key, value as String)
        Int::class -> putInt(key, value as Int)
        Boolean::class -> putBoolean(key, value as Boolean)
        Float::class -> putFloat(key, value as Float)
        Long::class -> putLong(key, value as Long)
        else -> putSerializable(key, value as? java.io.Serializable)
    }
}

// ============================================================================
// 13. ADVANCED: MULTIPLE REIFIED PARAMETERS
// ============================================================================

/**
 * Transform with multiple reified types
 */
inline fun <reified T, reified R> transform(input: T): R {
    return when {
        T::class == String::class && R::class == Int::class -> {
            (input as String).toInt() as R
        }
        T::class == Int::class && R::class == String::class -> {
            (input as Int).toString() as R
        }
        else -> throw IllegalArgumentException("Unsupported transformation")
    }
}

fun multipleReifiedExample() {
    val number = transform<String, Int>("123")  // 123
    val string = transform<Int, String>(123)    // "123"
}

// ============================================================================
// 14. ADVANCED: REIFIED WITH UPPER BOUNDS
// ============================================================================

/**
 * Reified with type bounds
 */
inline fun <reified T : ViewModel> Fragment.getViewModelWithFactory(
    factory: ViewModelProvider.Factory
): T {
    return ViewModelProvider(this, factory)[T::class.java]
}

/**
 * Reified with multiple bounds
 */
interface Serializable
interface Cloneable

inline fun <reified T> createSerializable(): T 
    where T : Serializable, T : Cloneable {
    return T::class.java.newInstance()
}

// ============================================================================
// 15. ADVANCED: REIFIED WITH SEALED CLASSES
// ============================================================================

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

/**
 * Check if result is success of specific type
 */
inline fun <reified T> Result<*>.isSuccessOfType(): Boolean {
    return this is Result.Success && this.data is T
}

/**
 * Get success data if matches type
 */
inline fun <reified T> Result<*>.getSuccessData(): T? {
    return (this as? Result.Success<*>)?.data as? T
}

fun sealedClassExample() {
    val result: Result<String> = Result.Success("hello")
    val isStringSuccess = result.isSuccessOfType<String>()  // true
    val data: String? = result.getSuccessData<String>()     // "hello"
}

// ============================================================================
// 16. ADVANCED: REIFIED WITH EXTENSION FUNCTIONS
// ============================================================================

/**
 * Extension function with reified
 */
inline fun <reified T> List<*>.filterByType(): List<T> {
    return filterIsInstance<T>()
}

/**
 * Extension with reified and receiver
 */
inline fun <reified T> Any?.letIfInstance(block: (T) -> Unit) {
    if (this is T) block(this)
}

fun extensionExample() {
    val mixed = listOf(1, "hello", 2, "world")
    val numbers = mixed.filterByType<Int>()  // [1, 2]
    
    val value: Any? = "hello"
    value.letIfInstance<String> { str ->
        println("It's a string: $str")
    }
}

// ============================================================================
// 17. TYPE-SAFE NAVIGATION ARGUMENTS
// ============================================================================

/**
 * Get navigation argument with type
 */
inline fun <reified T> androidx.navigation.NavBackStackEntry.argument(key: String): T? {
    return arguments?.get(key) as? T
}

/**
 * Get navigation argument with default
 */
inline fun <reified T> androidx.navigation.NavBackStackEntry.argument(
    key: String,
    defaultValue: T
): T {
    return argument<T>(key) ?: defaultValue
}

// Usage in Navigation Compose
/*
@Composable
fun DetailScreen(backStackEntry: NavBackStackEntry) {
    val itemId: String? = backStackEntry.argument<String>("itemId")
    val userId: Int = backStackEntry.argument<Int>("userId", 0)
}
*/

// ============================================================================
// 18. TYPE-SAFE DEPENDENCY INJECTION HELPER
// ============================================================================

/**
 * Get dependency with reified type (simplified example)
 */
inline fun <reified T> getDependency(): T {
    return when (T::class) {
        UserRepository::class -> UserRepositoryImpl() as T
        ApiService::class -> RetrofitClient.apiService as T
        else -> throw IllegalArgumentException("Unknown dependency: ${T::class}")
    }
}

interface UserRepository {
    fun getUser(id: String): User?
}

class UserRepositoryImpl : UserRepository {
    override fun getUser(id: String): User? = User("John", "john@example.com")
}

class RetrofitClient {
    companion object {
        val apiService = object : ApiService {}
    }
}

interface ApiService

fun dependencyExample() {
    val repository: UserRepository = getDependency<UserRepository>()
    val apiService: ApiService = getDependency<ApiService>()
}

// ============================================================================
// 19. TYPE-SAFE ROOM DAO ACCESS
// ============================================================================

/**
 * Get DAO with reified type (simplified example)
 */
inline fun <reified T> androidx.room.RoomDatabase.getDao(): T {
    return when (T::class) {
        UserDao::class -> userDao() as T
        ProductDao::class -> productDao() as T
        else -> throw IllegalArgumentException("Unknown DAO: ${T::class}")
    }
}

interface UserDao
interface ProductDao

fun androidx.room.RoomDatabase.userDao(): UserDao = object : UserDao {}
fun androidx.room.RoomDatabase.productDao(): ProductDao = object : ProductDao {}

// ============================================================================
// 20. PRACTICAL ANDROID USE CASES
// ============================================================================

/**
 * Complete ViewModel setup example
 */
class UserViewModel : ViewModel() {
    fun loadUser(userId: String) {}
}

class MainActivity : androidx.fragment.app.FragmentActivity() {
    private val viewModel: UserViewModel by lazy { getViewModel<UserViewModel>() }
}

/**
 * Complete Intent handling example
 */
fun handleIntent(intent: Intent) {
    val userId: String? = intent.getTypedExtra<String>("user_id")
    val isEnabled: Boolean = intent.getTypedExtra<Boolean>("is_enabled", false)
    
    if (userId != null) {
        // Use userId
    }
}

/**
 * Complete SharedPreferences example
 */
fun saveUserPreferences(prefs: SharedPreferences, user: User) {
    prefs.edit()
        .putTyped("user_name", user.name)
        .putTyped("user_email", user.email)
        .apply()
}

fun loadUserPreferences(prefs: SharedPreferences): User? {
    val name = prefs.getTyped<String>("user_name", "")
    val email = prefs.getTyped<String>("user_email", "")
    
    return if (name.isNotBlank() && email.isNotBlank()) {
        User(name, email)
    } else {
        null
    }
}

// ============================================================================
// USAGE EXAMPLES
// ============================================================================

fun usageExamples() {
    // Type checking
    val isString = isInstanceOf<String>("hello")
    
    // Type-safe logging
    class MyClass {
        fun test() {
            log("Test message")
        }
    }
    
    // Safe casting
    val value: Any = "hello"
    val string: String? = value.safeCast<String>()
    
    // Filter by type
    val mixed = listOf(1, "hello", 2)
    val numbers = mixed.filterIsInstanceOf<Int>()
    
    // JSON parsing
    val gson = Gson()
    val user = gson.fromJson<User>("""{"name":"John","email":"john@example.com"}""")
    
    // Factory
    val person = createInstance<Person>("John", 30)
}
