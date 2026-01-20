// Sealed Classes and Interfaces - Interactive Demo & Practice
// Run these examples to understand sealed classes, sealed interfaces, and exhaustive when

package com.example.demoapplication

// ============================================================================
// SECTION 1: BASIC SEALED CLASS
// ============================================================================

/**
 * Example 1: Basic Sealed Class
 * Represents a result with known possible states
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

fun demonstrateBasicSealedClass() {
    println("=== Example 1: Basic Sealed Class ===")
    
    val success: Result<String> = Result.Success("Hello World")
    val error: Result<String> = Result.Error("Something went wrong")
    val loading: Result<String> = Result.Loading
    
    // Exhaustive when expression
    fun processResult(result: Result<String>) {
        when (result) {
            is Result.Success -> {
                val (data) = result  // Destructuring
                println("Success: $data")
            }
            is Result.Error -> {
                val (message) = result  // Destructuring
                println("Error: $message")
            }
            is Result.Loading -> {
                println("Loading...")
            }
            // ✅ No else needed - exhaustive!
        }
    }
    
    processResult(success)  // Success: Hello World
    processResult(error)    // Error: Something went wrong
    processResult(loading)  // Loading...
    
    println()
}

// ============================================================================
// SECTION 2: EXHAUSTIVE WHEN EXPRESSIONS
// ============================================================================

/**
 * Example 2: Exhaustive When as Expression
 * Returns a value, compiler ensures all cases handled
 */
fun demonstrateExhaustiveWhen() {
    println("=== Example 2: Exhaustive When Expression ===")
    
    sealed class Status {
        object Active : Status()
        object Inactive : Status()
        data class Suspended(val reason: String) : Status()
    }
    
    fun getStatusMessage(status: Status): String {
        return when (status) {
            is Status.Active -> "User is active"
            is Status.Inactive -> "User is inactive"
            is Status.Suspended -> "User suspended: ${status.reason}"
            // ✅ Exhaustive - returns String, no else needed
        }
    }
    
    println(getStatusMessage(Status.Active))
    println(getStatusMessage(Status.Suspended("Violation")))
    
    println()
}

// ============================================================================
// SECTION 3: NON-EXHAUSTIVE WHEN (REQUIRES ELSE)
// ============================================================================

/**
 * Example 3: Non-Exhaustive When
 * When without subject requires else clause
 */
fun demonstrateNonExhaustiveWhen() {
    println("=== Example 3: Non-Exhaustive When ===")
    
    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }
    
    fun process(result: Result<String>) {
        // ❌ Non-exhaustive - requires else
        when {
            result is Result.Success -> println("Success")
            result is Result.Error -> println("Error")
            else -> println("Unknown")  // Required but unreachable
        }
        
        // ✅ Exhaustive - no else needed
        when (result) {
            is Result.Success -> println("Success: ${result.data}")
            is Result.Error -> println("Error: ${result.message}")
            // No else needed!
        }
    }
    
    process(Result.Success("Data"))
    
    println()
}

// ============================================================================
// SECTION 4: SEALED INTERFACE
// ============================================================================

/**
 * Example 4: Sealed Interface
 * More flexible than sealed class - can implement multiple interfaces
 */
fun demonstrateSealedInterface() {
    println("=== Example 4: Sealed Interface ===")
    
    sealed interface Loadable<out T> {
        data object Loading : Loadable<Nothing>
        data class Loaded<T>(val value: T) : Loadable<T>
        data class Failed(val error: Throwable) : Loadable<Nothing>
    }
    
    sealed interface Cacheable
    
    // Can implement multiple sealed interfaces
    data class CachedData<T>(val value: T) : Loadable<T>, Cacheable
    
    fun process(loadable: Loadable<String>) {
        when (loadable) {
            is Loadable.Loading -> println("Loading...")
            is Loadable.Loaded -> println("Loaded: ${loadable.value}")
            is Loadable.Failed -> println("Failed: ${loadable.error.message}")
            is CachedData -> println("Cached: ${loadable.value}")
        }
    }
    
    process(Loadable.Loading)
    process(Loadable.Loaded("Data"))
    process(CachedData("Cached"))
    
    println()
}

// ============================================================================
// SECTION 5: UI STATE MANAGEMENT
// ============================================================================

/**
 * Example 5: UI State with Sealed Class
 * Perfect for ViewModel state management
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

fun demonstrateUiState() {
    println("=== Example 5: UI State Management ===")
    
    fun render(state: UiState<List<String>>) {
        when (state) {
            is UiState.Loading -> println("Loading screen...")
            is UiState.Success -> {
                val (data) = state  // Destructuring
                println("Success screen with ${data.size} items")
            }
            is UiState.Error -> {
                val (message, cause) = state  // Destructuring
                println("Error screen: $message")
                cause?.printStackTrace()
            }
            is UiState.Empty -> println("Empty screen")
        }
    }
    
    render(UiState.Loading)
    render(UiState.Success(listOf("Item1", "Item2")))
    render(UiState.Error("Network error"))
    render(UiState.Empty)
    
    println()
}

// ============================================================================
// SECTION 6: NETWORK RESPONSE
// ============================================================================

/**
 * Example 6: Network Response with Sealed Class
 * Handles all possible network states
 */
sealed class NetworkResponse<out T> {
    data class Success<T>(val data: T, val statusCode: Int = 200) : NetworkResponse<T>()
    data class Error(val message: String, val statusCode: Int) : NetworkResponse<Nothing>()
    object Loading : NetworkResponse<Nothing>()
    object NoInternet : NetworkResponse<Nothing>()
    object Timeout : NetworkResponse<Nothing>()
}

fun demonstrateNetworkResponse() {
    println("=== Example 6: Network Response ===")
    
    fun handleResponse(response: NetworkResponse<String>) {
        when (response) {
            is NetworkResponse.Success -> {
                val (data, statusCode) = response
                println("Success ($statusCode): $data")
            }
            is NetworkResponse.Error -> {
                val (message, statusCode) = response
                println("Error ($statusCode): $message")
            }
            is NetworkResponse.Loading -> println("Loading...")
            is NetworkResponse.NoInternet -> println("No internet connection")
            is NetworkResponse.Timeout -> println("Request timeout")
        }
    }
    
    handleResponse(NetworkResponse.Success("Data", 200))
    handleResponse(NetworkResponse.Error("Not found", 404))
    handleResponse(NetworkResponse.NoInternet)
    
    println()
}

// ============================================================================
// SECTION 7: NAVIGATION EVENTS
// ============================================================================

/**
 * Example 7: Navigation Events
 * Type-safe navigation with sealed class
 */
sealed class NavigationEvent {
    data class NavigateToPost(val postId: Int) : NavigationEvent()
    data class NavigateToProfile(val userId: Int) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    data class ShowDialog(val message: String) : NavigationEvent()
}

fun demonstrateNavigationEvents() {
    println("=== Example 7: Navigation Events ===")
    
    fun handleNavigation(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToPost -> {
                val (postId) = event
                println("Navigate to post: $postId")
            }
            is NavigationEvent.NavigateToProfile -> {
                val (userId) = event
                println("Navigate to profile: $userId")
            }
            is NavigationEvent.NavigateBack -> {
                println("Navigate back")
            }
            is NavigationEvent.ShowDialog -> {
                val (message) = event
                println("Show dialog: $message")
            }
        }
    }
    
    handleNavigation(NavigationEvent.NavigateToPost(123))
    handleNavigation(NavigationEvent.NavigateBack)
    handleNavigation(NavigationEvent.ShowDialog("Hello"))
    
    println()
}

// ============================================================================
// SECTION 8: FORM VALIDATION
// ============================================================================

/**
 * Example 8: Form Validation
 * Sealed class for validation results
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}

sealed class FieldValidation {
    object Valid : FieldValidation()
    data class Invalid(val error: String) : FieldValidation()
}

fun demonstrateFormValidation() {
    println("=== Example 8: Form Validation ===")
    
    fun validateEmail(email: String): FieldValidation {
        return when {
            email.isBlank() -> FieldValidation.Invalid("Email is required")
            !email.contains("@") -> FieldValidation.Invalid("Invalid email format")
            else -> FieldValidation.Valid
        }
    }
    
    fun validateForm(email: String, password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        when (val emailResult = validateEmail(email)) {
            is FieldValidation.Invalid -> errors.add(emailResult.error)
            is FieldValidation.Valid -> {}
        }
        
        when {
            password.isBlank() -> errors.add("Password is required")
            password.length < 8 -> errors.add("Password must be at least 8 characters")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    when (val result = validateForm("test@example.com", "password123")) {
        is ValidationResult.Valid -> println("Form is valid")
        is ValidationResult.Invalid -> {
            val (errors) = result
            println("Form errors: ${errors.joinToString()}")
        }
    }
    
    println()
}

// ============================================================================
// SECTION 9: AUTHENTICATION STATE
// ============================================================================

/**
 * Example 9: Authentication State
 * Sealed class for auth flow
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

data class User(val id: Int, val name: String, val email: String)

fun demonstrateAuthState() {
    println("=== Example 9: Authentication State ===")
    
    fun renderAuthScreen(state: AuthState) {
        when (state) {
            is AuthState.Unauthenticated -> println("Show login screen")
            is AuthState.Authenticated -> {
                val (user) = state
                println("Show home screen for user: ${user.name}")
            }
            is AuthState.Loading -> println("Show loading screen")
            is AuthState.Error -> {
                val (message) = state
                println("Show error: $message")
            }
        }
    }
    
    renderAuthScreen(AuthState.Unauthenticated)
    renderAuthScreen(AuthState.Authenticated(User(1, "Alice", "alice@example.com")))
    renderAuthScreen(AuthState.Error("Invalid credentials"))
    
    println()
}

// ============================================================================
// SECTION 10: SEALED CLASS WITH SHARED STATE
// ============================================================================

/**
 * Example 10: Sealed Class with Shared Properties
 * All subtypes share common properties
 */
sealed class State {
    abstract val timestamp: Long
    
    data class Loading(override val timestamp: Long) : State()
    data class Success(override val timestamp: Long, val data: String) : State()
    data class Error(override val timestamp: Long, val message: String) : State()
}

fun demonstrateSharedState() {
    println("=== Example 10: Sealed Class with Shared State ===")
    
    val state1 = State.Loading(System.currentTimeMillis())
    val state2 = State.Success(System.currentTimeMillis(), "Data")
    
    fun process(state: State) {
        println("Timestamp: ${state.timestamp}")
        when (state) {
            is State.Loading -> println("Loading...")
            is State.Success -> println("Success: ${state.data}")
            is State.Error -> println("Error: ${state.message}")
        }
    }
    
    process(state1)
    process(state2)
    
    println()
}

// ============================================================================
// SECTION 11: NESTED SEALED CLASSES
// ============================================================================

/**
 * Example 11: Nested Sealed Classes
 * More complex hierarchy
 */
sealed class NetworkResult<T> {
    sealed class Success<T> : NetworkResult<T>() {
        data class Cached<T>(val data: T, val timestamp: Long) : Success<T>()
        data class Fresh<T>(val data: T) : Success<T>()
    }
    data class Error(val message: String) : NetworkResult<Nothing>()
}

fun demonstrateNestedSealedClasses() {
    println("=== Example 11: Nested Sealed Classes ===")
    
    fun process(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success.Cached -> {
                val (data, timestamp) = result
                println("Cached data: $data (timestamp: $timestamp)")
            }
            is NetworkResult.Success.Fresh -> {
                val (data) = result
                println("Fresh data: $data")
            }
            is NetworkResult.Error -> {
                val (message) = result
                println("Error: $message")
            }
        }
    }
    
    process(NetworkResult.Success.Cached("Data", System.currentTimeMillis()))
    process(NetworkResult.Success.Fresh("Fresh Data"))
    
    println()
}

// ============================================================================
// SECTION 12: COMPILER ENFORCEMENT DEMONSTRATION
// ============================================================================

/**
 * Example 12: Compiler Enforcement
 * Shows how compiler ensures exhaustive when
 */
fun demonstrateCompilerEnforcement() {
    println("=== Example 12: Compiler Enforcement ===")
    
    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
    
    // ✅ This compiles - exhaustive
    fun processComplete(result: Result<String>): String {
        return when (result) {
            is Result.Success -> "Success: ${result.data}"
            is Result.Error -> "Error: ${result.message}"
            is Result.Loading -> "Loading..."
            // No else needed - exhaustive!
        }
    }
    
    // ❌ This would NOT compile - missing Loading case
    /*
    fun processIncomplete(result: Result<String>): String {
        return when (result) {
            is Result.Success -> "Success"
            is Result.Error -> "Error"
            // Missing Loading case - compiler error!
        }
    }
    */
    
    println("Compiler ensures all cases are handled!")
    println()
}

// ============================================================================
// SECTION 13: SEALED CLASS VS ENUM
// ============================================================================

/**
 * Example 13: Sealed Class vs Enum
 * When to use each
 */
fun demonstrateSealedVsEnum() {
    println("=== Example 13: Sealed Class vs Enum ===")
    
    // Enum - fixed constants, singletons
    enum class Status {
        ACTIVE, INACTIVE, SUSPENDED
    }
    
    // Sealed class - can hold data, multiple instances
    sealed class StatusWithData {
        object Active : StatusWithData()
        object Inactive : StatusWithData()
        data class Suspended(val reason: String) : StatusWithData()
    }
    
    // Enum usage
    val enumStatus = Status.ACTIVE
    println("Enum: $enumStatus")
    
    // Sealed class usage
    val sealedStatus = StatusWithData.Suspended("Violation")
    when (sealedStatus) {
        is StatusWithData.Active -> println("Active")
        is StatusWithData.Inactive -> println("Inactive")
        is StatusWithData.Suspended -> println("Suspended: ${sealedStatus.reason}")
    }
    
    println()
}

// ============================================================================
// SECTION 14: REAL-WORLD ANDROID PATTERN
// ============================================================================

/**
 * Example 14: Complete Android Pattern
 * ViewModel state with sealed class
 */
sealed class PostUiState {
    object Loading : PostUiState()
    data class Success(val posts: List<String>) : PostUiState()
    data class Error(val message: String) : PostUiState()
    object Empty : PostUiState()
}

class PostViewModel {
    private var state: PostUiState = PostUiState.Loading
    
    fun loadPosts() {
        state = PostUiState.Loading
        
        // Simulate API call
        state = PostUiState.Success(listOf("Post 1", "Post 2"))
    }
    
    fun getState(): PostUiState = state
}

fun demonstrateAndroidPattern() {
    println("=== Example 14: Android Pattern ===")
    
    val viewModel = PostViewModel()
    viewModel.loadPosts()
    
    when (val state = viewModel.getState()) {
        is PostUiState.Loading -> println("Show loading")
        is PostUiState.Success -> {
            val (posts) = state
            println("Show posts: ${posts.size} items")
        }
        is PostUiState.Error -> {
            val (message) = state
            println("Show error: $message")
        }
        is PostUiState.Empty -> println("Show empty")
    }
    
    println()
}

// ============================================================================
// MAIN FUNCTION TO RUN ALL DEMONSTRATIONS
// ============================================================================

fun main() {
    println("=".repeat(60))
    println("Sealed Classes and Interfaces - Complete Demo")
    println("=".repeat(60))
    println()
    
    demonstrateBasicSealedClass()
    demonstrateExhaustiveWhen()
    demonstrateNonExhaustiveWhen()
    demonstrateSealedInterface()
    demonstrateUiState()
    demonstrateNetworkResponse()
    demonstrateNavigationEvents()
    demonstrateFormValidation()
    demonstrateAuthState()
    demonstrateSharedState()
    demonstrateNestedSealedClasses()
    demonstrateCompilerEnforcement()
    demonstrateSealedVsEnum()
    demonstrateAndroidPattern()
    
    println("=".repeat(60))
    println("KEY TAKEAWAYS:")
    println("=".repeat(60))
    println("1. Sealed classes restrict hierarchies - all subtypes known at compile-time")
    println("2. Exhaustive when ensures all cases handled - compiler enforces it")
    println("3. Sealed interfaces provide flexibility - multiple inheritance")
    println("4. Use for UI state, results, events - when all possibilities known")
    println("5. Benefits: type safety, exhaustive checking, better refactoring")
    println("=".repeat(60))
}
