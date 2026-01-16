// I.A - Kotlin Fundamentals (30 MCQs) - Senior Developer Interview Prep
// Core Topics with Advanced Examples & Edge Cases

// 1. VAL vs VAR - Immutability Concepts
fun valVsVarExamples() {
    val immutableRef = mutableListOf<String>() // Reference immutable, content mutable
    immutableRef.add("item") // ✓ Works
    // immutableRef = mutableListOf() // ✗ Compilation error
    
    var mutableRef = listOf("a", "b") // Reference mutable, content immutable
    mutableRef = listOf("c", "d") // ✓ Works
    // mutableRef.add("e") // ✗ No add method on immutable list
    
    // Advanced: val with custom getter (computed property)
    val currentTimestamp: Long
        get() = System.currentTimeMillis() // Recalculated on each access
}


// 2. DATA TYPES & TYPE INFERENCE
fun dataTypesAndInference() {
    // Primitive types are objects in Kotlin
    val byte: Byte = 127
    val short: Short = 32767
    val int = 42 // Type inferred as Int
    val long = 42L // Type inferred as Long
    val float = 3.14f // Type inferred as Float
    val double = 3.14 // Type inferred as Double
    
    // Type inference limitations
    val list = listOf(1, 2, 3) // Inferred as List<Int>
    // val emptyList = listOf() // ✗ Cannot infer type
    val emptyList = listOf<String>() // ✓ Explicit type needed
    
    // Underscores in numeric literals
    val million = 1_000_000
    val binary = 0b11010010_01101001_10010100_10010010
    val hex = 0xFF_EC_DE_5E
}

// 3. NULL SAFETY - Advanced Patterns
fun nullSafetyAdvanced() {
    var nullableString: String? = "Hello"
    
    // Safe call operator
    println(nullableString?.length) // Prints 5
    nullableString = null
    println(nullableString?.length) // Prints null
    
    // Elvis operator with early return
    fun processString(input: String?): String {
        val processed = input?.trim() ?: return "Empty input"
        return processed.uppercase()
    }
    
    // Safe call chaining
    data class User(val profile: Profile?)
    data class Profile(val address: Address?)
    data class Address(val city: String?)
    
    val user: User? = User(Profile(Address("NYC")))
    val city = user?.profile?.address?.city ?: "Unknown"
    
    // Not-null assertion (use carefully!)
    val definitelyNotNull = nullableString!! // Throws KotlinNullPointerException if null
    
    // Safe cast
    val stringValue: Any = "Hello"
    val safeString = stringValue as? String // Returns String? (null if cast fails)
    val unsafeString = stringValue as String // Throws ClassCastException if cast fails
}

// 4. CONTROL FLOW - Advanced When & Loops
fun controlFlowAdvanced() {
    // When as expression
    fun describe(obj: Any) = when (obj) {
        1 -> "One"
        "Hello" -> "Greeting"
        is Long -> "Long number"
        in 1..10 -> "Small number"
        !is String -> "Not a string"
        else -> "Unknown"
    }
    
    // When without argument
    fun validateAge(age: Int) = when {
        age < 0 -> throw IllegalArgumentException("Age cannot be negative")
        age < 18 -> "Minor"
        age < 65 -> "Adult"
        else -> "Senior"
    }
    
    // Advanced for loops
    val items = listOf("a", "b", "c")
    for ((index, value) in items.withIndex()) {
        println("$index: $value")
    }
    
    // Ranges and progressions
    for (i in 1..5) println(i) // 1, 2, 3, 4, 5
    for (i in 1 until 5) println(i) // 1, 2, 3, 4
    for (i in 5 downTo 1) println(i) // 5, 4, 3, 2, 1
    for (i in 1..10 step 2) println(i) // 1, 3, 5, 7, 9
    
    // Labels and breaks
    outer@ for (i in 1..3) {
        for (j in 1..3) {
            if (i == 2 && j == 2) break@outer
            println("$i, $j")
        }
    }
}

// 5. FUNCTIONS - Advanced Features
class FunctionExamples {
    // Named arguments and default parameters
    fun createUser(
        name: String,
        email: String = "",
        age: Int = 0,
        isActive: Boolean = true
    ): User = User(name, email, age, isActive)
    
    // Single-expression functions
    fun double(x: Int): Int = x * 2
    fun isEven(n: Int) = n % 2 == 0 // Return type inferred
    
    // Function with receiver (extension function)
    fun String.addPrefix(prefix: String) = "$prefix$this"
    
    // Higher-order functions
    fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
        val result = mutableListOf<T>()
        for (item in this) {
            if (predicate(item)) result.add(item)
        }
        return result
    }
    
    // Vararg parameters
    fun sum(vararg numbers: Int): Int = numbers.sum()
    fun callSum() {
        sum(1, 2, 3, 4) // Direct arguments
        val array = intArrayOf(1, 2, 3, 4)
        sum(*array) // Spread operator
    }

   // 1, 2, 3, 4, 5
    
    // Infix functions
    infix fun Int.times(str: String) = str.repeat(this)
    fun useInfix() {
        val result = 3 times "Hello " // Same as 3.times("Hello ")
    }
}

// 6. CLASSES AND OBJECTS - Advanced Concepts
data class User(val name: String, val email: String, val age: Int, val isActive: Boolean)

class UserManager {
    // Primary constructor with property declarations
    class Person(
        val firstName: String,
        val lastName: String,
        var age: Int = 0
    ) {
        // Secondary constructor
        constructor(fullName: String) : this(
            firstName = fullName.split(" ").first(),
            lastName = fullName.split(" ").last()
        )
        
        // Custom getter and setter
        val fullName: String
            get() = "$firstName $lastName"
            
        var email: String = ""
            set(value) {
                require(value.contains("@")) { "Invalid email" }
                field = value // 'field' refers to backing field
            }
            get() = field.lowercase()
        
        // Init block
        init {
            require(firstName.isNotBlank()) { "First name cannot be blank" }
        }
    }
    
    // Object declaration (Singleton)
    object DatabaseConfig {
        const val URL = "jdbc:postgresql://localhost/db"
        fun connect() = println("Connecting to $URL")
    }
    
    // Companion object
    companion object {
        private const val MAX_USERS = 1000
        
        @JvmStatic // For Java interop
        fun createDefault() = UserManager()
    }
}

// 7. INHERITANCE AND INTERFACES
// Open class (can be inherited)
open class Animal(val name: String) {
    open fun makeSound() = "Some sound"
    
    // Final method (cannot be overridden)
    final fun sleep() = println("$name is sleeping")
}

// Abstract class
abstract class Mammal(name: String) : Animal(name) {
    abstract fun giveBirth()
    
    // Abstract classes can have concrete methods
    fun breathe() = println("Breathing")
}

// Interface with default implementation
interface Flyable {
    val wingSpan: Double
    
    fun fly() = println("Flying with wingspan $wingSpan")
    
    // Abstract property
    val maxSpeed: Int
}

// Multiple inheritance from interface
class Bird(name: String, override val wingSpan: Double) : Animal(name), Flyable {
    override val maxSpeed: Int = 50
    
    override fun makeSound() = "Chirp"
    
    // Can override default implementation
    override fun fly() {
        println("$name is flying gracefully")
        super.fly() // Call default implementation
    }
}

// 8. VISIBILITY MODIFIERS
class VisibilityExample {
    public val publicProperty = "Visible everywhere" // Default is public
    private val privateProperty = "Only visible in this class"
    protected val protectedProperty = "Visible in subclasses"
    internal val internalProperty = "Visible in same module"
    
    private fun privateFunction() = "Private function"
    
    class NestedClass {
        // Cannot access outer class private members
        fun access() {
            // println(privateProperty) // ✗ Compilation error
        }
    }
    
    inner class InnerClass {
        // Can access outer class private members
        fun access() {
            println(privateProperty) // ✓ Works
        }
    }
}

// 9. STRING INTERPOLATION - Advanced
fun stringInterpolationAdvanced() {
    val name = "Alice"
    val age = 30
    val balance = 1234.56
    
    // Basic interpolation
    val basic = "Hello, $name!"
    
    // Expression interpolation
    val expression = "Next year $name will be ${age + 1}"
    
    // Function calls in interpolation
    val function = "Uppercase name: ${name.uppercase()}"
    
    // Complex expressions
    val complex = "Balance: ${if (balance > 1000) "High" else "Low"}"
    
    // Multi-line strings with interpolation
    val multiline = """
        Name: $name
        Age: $age
        Status: ${if (age >= 18) "Adult" else "Minor"}
    """.trimIndent()
    
    // Raw strings (no escaping needed)
    val regex = """[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"""
    val path = """C:\Users\$name\Documents"""
}

// SENIOR-LEVEL EDGE CASES & INSIGHTS

// 1. Type Erasure and Reified
inline fun <reified T> isInstanceOf(obj: Any): Boolean = obj is T

// 2. Platform Types (Java interop)
// String! means nullable unknown from Java

// 3. Nothing type
fun fail(message: String): Nothing = throw IllegalArgumentException(message)

// 4. Unit vs Void
fun returnsUnit(): Unit { } // Unit is a singleton object
fun returnsVoid() { } // Implicitly returns Unit

// 5. Backing Properties
class BackingPropertyExample {
    private var _items = mutableListOf<String>()
    val items: List<String>
        get() = _items
}

// ============================================================================
// CLASSES AND OBJECTS - COMPREHENSIVE GUIDE
// ============================================================================

// 1. PRIMARY CONSTRUCTOR
// The most concise way to define a class with constructor parameters
class Employee(val id: Int, var name: String, var salary: Double) {
    // Properties declared in primary constructor are automatically initialized
    
    fun displayInfo() = println("ID: $id, Name: $name, Salary: $salary")
}

// Primary constructor with init block
class Customer(val email: String, val name: String) {
    // Init blocks run immediately after primary constructor
    init {
        require(email.contains("@")) { "Invalid email format" }
        println("Customer created: $name")
    }
    
    // Multiple init blocks execute in order
    init {
        println("Second init block")
    }
}

// 2. SECONDARY CONSTRUCTORS
// When you need additional ways to create objects
class Product {
    val name: String
    val price: Double
    val category: String
    
    // Primary constructor
    constructor(name: String, price: Double, category: String) {
        this.name = name
        this.price = price
        this.category = category
    }
    
    // Secondary constructor - must delegate to primary
    constructor(name: String, price: Double) : this(name, price, "Uncategorized")
    
    // Another secondary constructor
    constructor(name: String) : this(name, 0.0, "Unknown")
}

// Combining primary and secondary constructors
class Vehicle(val brand: String, val model: String) {
    var year: Int = 2024
    var color: String = "Unknown"
    
    // Secondary constructor must delegate to primary using 'this'
    constructor(brand: String, model: String, year: Int) : this(brand, model) {
        this.year = year
    }
    
    constructor(brand: String, model: String, year: Int, color: String) : this(brand, model, year) {
        this.color = color
    }
}

// 3. PROPERTIES - GETTERS AND SETTERS
class BankAccount(val accountNumber: String, initialBalance: Double) {
    
    // Property with private setter
    var balance: Double = initialBalance
        private set  // Can only be modified within this class
    
    // Read-only computed property (custom getter, no backing field)
    val isOverdrawn: Boolean
        get() = field
        set(value) = value
    
    // Property with custom getter and setter
    var interestRate: Double = 0.0
        get() = field * 100  // Return as percentage
        set(value) {
            require(value >= 0) { "Interest rate cannot be negative" }
            field = value / 100  // Store as decimal
        }
    
    // Late-initialized property (non-null, but initialized later)
    lateinit var accountHolder: String
    
    // Lazy property (initialized on first access)
    val accountSummary: String by lazy {
        println("Computing summary...") // Only runs once
        "Account: $accountNumber, Balance: $balance"
    }
    
    fun deposit(amount: Double) {
        require(amount > 0) { "Deposit amount must be positive" }
        balance += amount
    }
    
    fun withdraw(amount: Double): Boolean {
        return if (amount <= balance) {
            balance -= amount
            true
        } else false
    }
}

// 4. BACKING FIELD vs BACKING PROPERTY
class Temperature {
    // Using 'field' keyword - Kotlin's implicit backing field
    var celsius: Double = 0.0
        set(value) {
            require(value >= -273.15) { "Temperature below absolute zero!" }
            field = value  // 'field' refers to the backing field
        }
    
    // Computed property - no backing field
    val fahrenheit: Double
        get() = celsius * 9 / 5 + 32
    
    val kelvin: Double
        get() = celsius + 273.15
}

// Backing property pattern (common for exposing immutable view of mutable data)
class ShoppingCart {
    // Private mutable backing property
    private val _items = mutableListOf<String>()
    
    // Public read-only property exposing immutable view
    val items: List<String>
        get() = _items.toList()  // Returns a copy
    
    // Alternative: expose as immutable type directly
    val itemsView: List<String>
        get() = _items  // Returns read-only view (not a copy)
    
    fun addItem(item: String) {
        _items.add(item)
    }
    
    fun removeItem(item: String) = _items.remove(item)
}

// 5. OBJECT KEYWORD - SINGLETON PATTERN
// Object declaration creates a thread-safe singleton
object AppConfig {
    const val APP_NAME = "MyApp"
    const val VERSION = "1.0.0"
    var debugMode = false
    
    private var _apiKey: String = ""
    
    fun initialize(apiKey: String) {
        _apiKey = apiKey
        println("AppConfig initialized with API key")
    }
    
    fun getApiKey(): String = _apiKey
}

// Using the singleton
fun useAppConfig() {
    AppConfig.initialize("secret-key-123")
    println("App: ${AppConfig.APP_NAME} v${AppConfig.VERSION}")
    AppConfig.debugMode = true
}

// Object can inherit from classes and implement interfaces
interface Logger {
    fun log(message: String)
}

object ConsoleLogger : Logger {
    override fun log(message: String) {
        println("[LOG] $message")
    }
}

// 6. COMPANION OBJECT - Static-like members
class MathUtils {
    // Instance method
    fun instanceMethod() = "I'm an instance method"
    
    companion object {
        // These act like static members in Java
        const val PI = 3.14159
        const val E = 2.71828
        
        fun square(n: Double): Double = n * n
        fun cube(n: Double): Double = n * n * n
        
        // Factory method pattern
        fun zero() = MathUtils()
    }
}

// Named companion object
class JsonParser {
    companion object Factory {
        fun fromString(json: String): JsonParser {
            println("Parsing: $json")
            return JsonParser()
        }
        
        fun fromFile(path: String): JsonParser {
            println("Reading from: $path")
            return JsonParser()
        }
    }
}

// Companion object implementing interface (useful for factory pattern)
interface Factory<T> {
    fun create(): T
}

class MyService private constructor(val name: String) {
    companion object : Factory<MyService> {
        override fun create(): MyService = MyService("DefaultService")
        
        fun createWithName(name: String) = MyService(name)
    }
}

// 7. OBJECT EXPRESSIONS - Anonymous Objects
fun objectExpressions() {
    // Anonymous object implementing an interface
    val clickListener = object : OnClickListener {
        override fun onClick() {
            println("Button clicked!")
        }
    }
    
    // Anonymous object extending a class
    open class Person(val name: String)
    
    val anonymousPerson = object : Person("Anonymous") {
        val secretId = 12345
        
        fun revealSecret() = println("Secret ID: $secretId")
    }
    
    // Anonymous object with no supertype
    val point = object {
        var x = 10
        var y = 20
        
        fun move(dx: Int, dy: Int) {
            x += dx
            y += dy
        }
    }
    println("Point: (${point.x}, ${point.y})")
}

interface OnClickListener {
    fun onClick()
}

// 8. DATA CLASSES
data class UserProfile(
    val id: Long,
    val username: String,
    val email: String,
    var lastLogin: Long = System.currentTimeMillis()
) {
    // Data classes automatically generate:
    // - equals() / hashCode()
    // - toString() -> "UserProfile(id=1, username=john, ...)"
    // - copy()
    // - componentN() functions for destructuring
    
    // You can still add custom methods
    fun isActive() = System.currentTimeMillis() - lastLogin < 86400000
}

fun dataClassExamples() {
    val user1 = UserProfile(1, "john", "john@example.com")
    val user2 = UserProfile(1, "john", "john@example.com")
    
    // equals() compares all properties
    println(user1 == user2)  // true
    
    // copy() creates a new instance with some properties changed
    val user3 = user1.copy(email = "newemail@example.com")
    
    // Destructuring
    val (id, username, email) = user1
    println("User $id: $username ($email)")
}

// 9. NESTED AND INNER CLASSES
class Outer(private val outerValue: String) {
    
    // Nested class (static in Java terms) - NO access to outer class
    class Nested {
        fun demo() = "I'm nested"
        // Cannot access outerValue here!
    }
    
    // Inner class - HAS access to outer class
    inner class Inner {
        fun demo() = "I'm inner, outer value is: $outerValue"
        
        // Access outer class reference
        fun getOuterReference(): Outer = this@Outer
    }
}

fun nestedInnerDemo() {
    // Nested class instantiation - doesn't need Outer instance
    val nested = Outer.Nested()
    println(nested.demo())
    
    // Inner class instantiation - REQUIRES Outer instance
    val outer = Outer("Hello")
    val inner = outer.Inner()
    println(inner.demo())
}

// 10. SEALED CLASSES - Restricted Hierarchy
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

fun handleResult(result: NetworkResult<String>) {
    // When is exhaustive - compiler knows all cases
    when (result) {
        is NetworkResult.Success -> println("Data: ${result.data}")
        is NetworkResult.Error -> println("Error ${result.code}: ${result.message}")
        is NetworkResult.Loading -> println("Loading...")
        // No 'else' needed - all cases covered!
    }
}

// 11. ENUM CLASSES
enum class OrderStatus(val displayName: String, val isFinal: Boolean) {
    PENDING("Pending", false),
    PROCESSING("Processing", false),
    SHIPPED("Shipped", false),
    DELIVERED("Delivered", true),
    CANCELLED("Cancelled", true);
    
    // Enums can have methods
    fun canCancel(): Boolean = !isFinal
    
    companion object {
        fun fromString(value: String): OrderStatus? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

fun enumExample() {
    val status = OrderStatus.PROCESSING
    println("Status: ${status.displayName}, Can cancel: ${status.canCancel()}")
    
    // Iterate all values
    OrderStatus.entries.forEach { println(it.name) }
}

// 12. COMPLETE EXAMPLE - Combining All Concepts
class LibrarySystem private constructor(val name: String) {
    
    private val _books = mutableListOf<Book>()
    val books: List<Book> get() = _books
    
    val totalBooks: Int get() = _books.size
    
    lateinit var librarian: String
    
    init {
        println("Library '$name' initialized")
    }
    
    fun addBook(book: Book) = _books.add(book)
    
    data class Book(
        val isbn: String,
        val title: String,
        val author: String,
        var isAvailable: Boolean = true
    )
    
    companion object {
        private var instance: LibrarySystem? = null
        
        fun getInstance(name: String = "Default Library"): LibrarySystem {
            return instance ?: LibrarySystem(name).also { instance = it }
        }
    }
    
    object Statistics {
        fun availableBooks(library: LibrarySystem): Int {
            return library.books.count { it.isAvailable }
        }
    }
}

fun libraryDemo() {
    val library = LibrarySystem.getInstance("City Library")
    library.librarian = "Jane Doe"
    
    library.addBook(LibrarySystem.Book("978-0-13-468599-1", "Kotlin in Action", "Dmitry"))
    library.addBook(LibrarySystem.Book("978-0-59-651798-3", "Head First Kotlin", "Dawn"))
    
    println("Total: ${library.totalBooks}")
    println("Available: ${LibrarySystem.Statistics.availableBooks(library)}")
}

// ============================================================================
// SENIOR ANDROID INTERVIEW PREP: CLASSES AND OBJECTS
// ============================================================================

/*
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                         1. CORE CONCEPTS                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * CONSTRUCTORS:
 * - Primary Constructor: Declared in class header, most concise form
 * - Secondary Constructor: Additional constructors using 'constructor' keyword
 * - Init Blocks: Initialization logic that runs after primary constructor
 * - Delegation: Secondary constructors MUST delegate to primary via 'this'
 *
 * PROPERTIES:
 * - val: Read-only (getter only), must be initialized
 * - var: Mutable (getter + setter)
 * - Backing Field: Implicit 'field' keyword in accessors
 * - Backing Property: Pattern using private _property for encapsulation
 * - lateinit: Deferred initialization for non-null properties
 * - lazy: Computed on first access, thread-safe by default
 *
 * OBJECT KEYWORD:
 * - Object Declaration: Singleton pattern (thread-safe, lazy initialization)
 * - Companion Object: Static-like members attached to class
 * - Object Expression: Anonymous objects for one-time implementations
 *
 * KEY INSIGHT: Kotlin properties are NOT fields - they're getter/setter pairs.
 * When you write 'val x = 5', Kotlin generates: backing field + getX() method
 */

// ============================================================================
// 2. REAL-WORLD ANDROID APPLICATIONS
// ============================================================================

// EXAMPLE 1: ViewModel with proper encapsulation (MVVM pattern)
/*
class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {
    
    // Backing property pattern - ESSENTIAL for LiveData/StateFlow
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user  // Expose immutable LiveData
    
    // Computed property - no backing field needed
    val isLoggedIn: Boolean
        get() = _user.value != null
    
    // lateinit for injection or late binding
    lateinit var navigator: Navigator
    
    // lazy for expensive one-time computations
    val dateFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("MMM dd, yyyy")
    }
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            userRepository.getUser(userId)
                .onSuccess { _user.value = it; _uiState.value = UiState.Success }
                .onFailure { _uiState.value = UiState.Error(it.message) }
        }
    }
}
*/

// EXAMPLE 2: Singleton for App-wide Configuration
object AppConfiguration {
    // const for compile-time constants (primitives & String only)
    const val API_VERSION = "v2"
    const val MAX_RETRY_COUNT = 3
    
    // Runtime configuration
    var isDebugMode: Boolean = false
        private set
    
    var baseUrl: String = "https://api.production.com"
        private set
    
    private lateinit var application: Application
    
    fun initialize(app: Application, debug: Boolean = false) {
        application = app
        isDebugMode = debug
        baseUrl = if (debug) "https://api.staging.com" else "https://api.production.com"
    }
    
    val appContext: Context
        get() = application.applicationContext
}

// EXAMPLE 3: Repository with Companion Object Factory
abstract class BaseRepository {
    abstract val tag: String
}

class UserRepositoryImpl private constructor(
    private val apiService: ApiServiceExample,
    private val userDao: UserDaoExample,
    private val dispatcher: CoroutineDispatcher
) : BaseRepository() {
    
    override val tag = "UserRepository"
    
    // Companion object as factory - controls instantiation
    companion object {
        @Volatile
        private var INSTANCE: UserRepositoryImpl? = null
        
        fun getInstance(
            apiService: ApiServiceExample,
            userDao: UserDaoExample,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): UserRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepositoryImpl(apiService, userDao, dispatcher)
                    .also { INSTANCE = it }
            }
        }
        
        // For testing - allows clearing singleton
        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
    
    suspend fun getUser(id: String): Result<UserExample> = withContext(dispatcher) {
        try {
            val user = apiService.fetchUser(id)
            userDao.insert(user)
            Result.success(user)
        } catch (e: Exception) {
            // Fallback to cache
            val cached = userDao.getUser(id)
            if (cached != null) Result.success(cached)
            else Result.failure(e)
        }
    }
}

// Placeholder interfaces for compilation
interface ApiServiceExample { suspend fun fetchUser(id: String): UserExample }
interface UserDaoExample { suspend fun insert(user: UserExample); suspend fun getUser(id: String): UserExample? }
data class UserExample(val id: String, val name: String)
annotation class VisibleForTesting

// EXAMPLE 4: Sealed Class with Data Classes for UI State
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    
    // Utility methods
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    
    fun getOrNull(): T? = (this as? Success)?.data
    
    inline fun onSuccess(action: (T) -> Unit): UiState<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (String, Throwable?) -> Unit): UiState<T> {
        if (this is Error) action(message, cause)
        return this
    }
}

// EXAMPLE 5: Builder Pattern with apply scope function
class NotificationConfig private constructor(
    val title: String,
    val message: String,
    val channelId: String,
    val priority: Int,
    val autoCancel: Boolean,
    val vibrate: Boolean
) {
    class Builder {
        private var title: String = ""
        private var message: String = ""
        private var channelId: String = "default"
        private var priority: Int = 0 // NotificationCompat.PRIORITY_DEFAULT
        private var autoCancel: Boolean = true
        private var vibrate: Boolean = false
        
        fun title(title: String) = apply { this.title = title }
        fun message(message: String) = apply { this.message = message }
        fun channelId(channelId: String) = apply { this.channelId = channelId }
        fun priority(priority: Int) = apply { this.priority = priority }
        fun autoCancel(autoCancel: Boolean) = apply { this.autoCancel = autoCancel }
        fun vibrate(vibrate: Boolean) = apply { this.vibrate = vibrate }
        
        fun build(): NotificationConfig {
            require(title.isNotBlank()) { "Title is required" }
            require(message.isNotBlank()) { "Message is required" }
            return NotificationConfig(title, message, channelId, priority, autoCancel, vibrate)
        }
    }
    
    companion object {
        inline fun build(block: Builder.() -> Unit): NotificationConfig {
            return Builder().apply(block).build()
        }
    }
}

// Usage: val config = NotificationConfig.build { title("Hi"); message("Hello") }

// ============================================================================
// 3. COMMON INTERVIEW QUESTIONS & ANSWERS
// ============================================================================

/*
 * Q1: What's the difference between 'object' declaration and 'companion object'?
 * ─────────────────────────────────────────────────────────────────────────────
 * A: - 'object' creates a standalone SINGLETON - one instance in entire app
 *    - 'companion object' is tied to a CLASS - provides static-like members
 *    - Companion objects can implement interfaces (useful for factory pattern)
 *    - Companion objects can have names: companion object Factory { }
 *    - Both are lazily initialized on first access
 *
 * Q2: Explain the 'field' keyword in custom getters/setters
 * ─────────────────────────────────────────────────────────────────────────────
 * A: 'field' is the identifier for the backing field in accessors.
 *    - Only available inside get() and set()
 *    - Using the property name in accessor causes infinite recursion!
 *    - Not all properties have backing fields (computed properties don't)
 */

class FieldKeywordExample {
    var value: Int = 0
        set(newValue) {
            println("Old: $field, New: $newValue")
            field = newValue  // Correct: uses backing field
            // value = newValue  // WRONG: infinite recursion!
        }
    
    // No backing field - purely computed
    val doubled: Int
        get() = value * 2  // 'field' not available here
}

/*
 * Q3: What's the difference between 'lateinit var' and 'by lazy'?
 * ─────────────────────────────────────────────────────────────────────────────
 * A: 
 *    lateinit var:
 *    - For var only (mutable)
 *    - Non-null types only, no primitives
 *    - Can be re-assigned
 *    - Throws UninitializedPropertyAccessException if accessed before init
 *    - Use for: Dependency injection, View binding, test setup
 *    
 *    by lazy:
 *    - For val only (immutable)
 *    - Works with any type including primitives
 *    - Thread-safe by default (SYNCHRONIZED mode)
 *    - Computed once on first access
 *    - Use for: Expensive computations, one-time initialization
 */

class LateinitVsLazy {
    // lateinit - for DI or late binding
    lateinit var viewBinding: Any  // Will be set in onCreate()
    
    // Check if initialized
    fun isViewReady(): Boolean = ::viewBinding.isInitialized
    
    // lazy - for expensive one-time computation
    val expensiveObject: ExpensiveClass by lazy {
        println("Creating expensive object...")
        ExpensiveClass()
    }
    
    // lazy with different modes
    val threadSafe: String by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { "default" }
    val notThreadSafe: String by lazy(LazyThreadSafetyMode.NONE) { "faster if single-threaded" }
    val publishOnce: String by lazy(LazyThreadSafetyMode.PUBLICATION) { "multiple may compute, one wins" }
}

class ExpensiveClass

/*
 * Q4: How does Kotlin ensure object declarations are thread-safe singletons?
 * ─────────────────────────────────────────────────────────────────────────────
 * A: Kotlin compiles 'object' to a Java class with:
 *    - Private constructor
 *    - Static INSTANCE field
 *    - Static initializer block (class loading guarantees thread safety)
 *    - JVM class loading is inherently thread-safe
 *    
 *    Equivalent Java:
 *    public final class MySingleton {
 *        public static final MySingleton INSTANCE;
 *        static { INSTANCE = new MySingleton(); }
 *        private MySingleton() {}
 *    }
 *
 * Q5: What's the backing property pattern and when should you use it?
 * ─────────────────────────────────────────────────────────────────────────────
 * A: Pattern where you have:
 *    - Private mutable property: private val _data = MutableLiveData<T>()
 *    - Public immutable property: val data: LiveData<T> = _data
 *    
 *    Use when:
 *    - Exposing LiveData/StateFlow (mutable internally, immutable externally)
 *    - Returning immutable views of mutable collections
 *    - Encapsulating write access while allowing reads
 *    
 *    This is ESSENTIAL for proper MVVM - prevents UI from modifying state directly
 *
 * Q6: Explain primary vs secondary constructors. When use each?
 * ─────────────────────────────────────────────────────────────────────────────
 * A: Primary Constructor:
 *    - Declared in class header: class Person(val name: String)
 *    - Most concise, idiomatic Kotlin
 *    - Can declare properties directly
 *    - Use init {} blocks for validation/logic
 *    
 *    Secondary Constructor:
 *    - Declared with 'constructor' keyword
 *    - MUST delegate to primary using 'this(...)'
 *    - Use when: Java interop, multiple initialization paths
 *    
 *    Best Practice: Prefer primary constructor with default parameters
 *    over multiple secondary constructors
 *
 * Q7: What happens when you use 'const val' vs 'val' in companion object?
 * ─────────────────────────────────────────────────────────────────────────────
 * A: const val:
 *    - Compile-time constant, inlined at call sites
 *    - Only primitives and String
 *    - Must be initialized with literal or other const
 *    - No custom getter allowed
 *    
 *    val:
 *    - Runtime constant
 *    - Any type allowed
 *    - Can have custom getter
 *    - Stored in companion object instance
 *    
 *    Use const for: True constants (URLs, keys, config values)
 *    Use val for: Computed values, complex objects
 */

// ============================================================================
// 4. BEST PRACTICES
// ============================================================================

/*
 * ✓ Use data classes for DTOs, models, and state objects
 * ✓ Use backing property pattern for LiveData/StateFlow in ViewModels
 * ✓ Prefer primary constructor with default parameters over secondary constructors
 * ✓ Use 'object' for true singletons, avoid manual double-checked locking
 * ✓ Use companion object for factory methods and constants
 * ✓ Mark properties as 'private set' when external modification is unwanted
 * ✓ Use 'lateinit' for lifecycle-dependent initialization (views, DI)
 * ✓ Use 'by lazy' for expensive one-time computations
 * ✓ Use sealed classes for restricted type hierarchies (UI states, results)
 * ✓ Validate in init {} blocks, throw early with require/check
 */

// BEST PRACTICE: Proper ViewModel structure
/*
class BestPracticeViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    
    // 1. Private mutable state
    private val _state = MutableStateFlow(ScreenState())
    
    // 2. Public immutable exposure
    val state: StateFlow<ScreenState> = _state.asStateFlow()
    
    // 3. Computed properties for derived state
    val isEmpty: Boolean get() = _state.value.items.isEmpty()
    
    // 4. Init block for initial load
    init {
        loadData()
    }
    
    // 5. Companion for constants and factory
    companion object {
        private const val TAG = "BestPracticeViewModel"
        const val PAGE_SIZE = 20
    }
}
*/

// ============================================================================
// 5. COMMON PITFALLS
// ============================================================================

class CommonPitfalls {
    
    // PITFALL 1: Infinite recursion in setter
    var badProperty: String = ""
        set(value) {
            // badProperty = value  // INFINITE RECURSION!
            field = value  // Correct
        }
    
    // PITFALL 2: Memory leak with object holding Activity context
    /*
    object LeakyManager {
        lateinit var context: Context  // DON'T store Activity context!
        // Use: applicationContext instead
    }
    */
    
    // PITFALL 3: Forgetting that data class copy() is shallow
    data class Container(val items: MutableList<String>)
    
    fun shallowCopyPitfall() {
        val original = Container(mutableListOf("a", "b"))
        val copy = original.copy()
        copy.items.add("c")  // MODIFIES ORIGINAL's list too!
        // Solution: copy.copy(items = original.items.toMutableList())
    }
    
    // PITFALL 4: lateinit with primitives
    // lateinit var count: Int  // COMPILE ERROR: primitives not allowed
    
    // PITFALL 5: Companion object is NOT static - it's a singleton instance
    companion object {
        // This is actually an object, not static methods
        // @JvmStatic needed for true Java static interop
        @JvmStatic
        fun forJavaCallers() { }
    }
    
    // PITFALL 6: Object expressions capture outer scope
    fun createCallback(): Runnable {
        val localData = "sensitive"
        return object : Runnable {
            override fun run() {
                println(localData)  // Captures 'localData' - potential memory issue
            }
        }
    }
}

// ============================================================================
// 6. PERFORMANCE CONSIDERATIONS
// ============================================================================

/*
 * MEMORY:
 * - object declarations: Single instance, loaded on first access
 * - data class: Auto-generated methods add to method count (DEX limit)
 * - copy(): Creates new object - avoid in tight loops
 * - Companion objects: Each creates a separate class file
 * 
 * STARTUP:
 * - Eager initialization in objects can slow app startup
 * - Use lazy {} for expensive initializations
 * - Consider LazyThreadSafetyMode.NONE if single-threaded access guaranteed
 * 
 * BEST PRACTICES:
 * - Avoid storing Context in singletons (memory leak)
 * - Use applicationContext if context needed in singleton
 * - Profile with Android Profiler for object allocation patterns
 * - Consider object pooling for frequently created objects
 */

class PerformanceExamples {
    // Good: Lazy initialization
    val heavyObject: HeavyProcessor by lazy(LazyThreadSafetyMode.NONE) {
        HeavyProcessor()
    }
    
    // Bad in tight loops
    data class Point(val x: Int, val y: Int)
    
    fun badLoop() {
        repeat(1000000) {
            val p = Point(it, it)  // Creates 1M objects
            val p2 = p.copy(x = 0)  // Creates another 1M objects
        }
    }
    
    // Better: Reuse or use primitives
    fun betterApproach(points: List<Point>): List<Int> {
        return points.map { it.x + it.y }  // Avoid unnecessary copies
    }
}

class HeavyProcessor

// ============================================================================
// 7. RECENT UPDATES (API 30+ / Kotlin 1.5+)
// ============================================================================

/*
 * KOTLIN 1.5+:
 * - Value classes (inline classes): 'value class' for zero-overhead wrappers
 * - Sealed interfaces: More flexible than sealed classes
 * - Record support for JVM 16+
 * 
 * KOTLIN 1.9+:
 * - data object: Combines data class benefits with singleton
 * - Entries property for enums (replaces values())
 * 
 * ANDROID:
 * - Hilt: Preferred DI eliminates manual singleton management
 * - Compose: State hoisting replaces some ViewModel patterns
 * - StateFlow/SharedFlow: Preferred over LiveData for new code
 */

// Value class - zero overhead wrapper (Kotlin 1.5+)
@JvmInline
value class UserId(val value: String) {
    init { require(value.isNotBlank()) { "UserId cannot be blank" } }
    val isValid: Boolean get() = value.length >= 8
}

// data object (Kotlin 1.9+) - toString returns class name
data object EmptyResponse

// Sealed interface (Kotlin 1.5+)
sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data class Loaded<T>(val value: T) : Loadable<T>
    data class Failed(val error: Throwable) : Loadable<Nothing>
}

// Modern enum usage
enum class Theme { LIGHT, DARK, SYSTEM;
    companion object {
        fun fromOrdinal(ordinal: Int) = entries.getOrElse(ordinal) { SYSTEM }
    }
}