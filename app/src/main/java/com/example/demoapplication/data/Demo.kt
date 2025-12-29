package com.example.demoapplication.data

import kotlinx.coroutines.delay
import kotlin.reflect.typeOf

class Demo {
    val x = arrayOf(1,2)
    val f : Int? = null
    val message = System.currentTimeMillis()
        get() = field + 1
}

// Sealed interface for operations (no shared constructor data)
sealed interface Operation {
    data class Add(val a: Int, val b: Int) : Operation
    data class Subtract(val a: Int, val b: Int) : Operation
    data class Multiply(val a: Int, val b: Int) : Operation
    data class Divide(val a: Int, val b: Int) : Operation
}

// Regular interface - ANYONE can implement this!
interface RegularOperation {
    data class Add(val a: Int, val b: Int) : RegularOperation
    data class Subtract(val a: Int, val b: Int) : RegularOperation
    // Someone else can add more types later!
}

// Sealed interface for UI states
sealed interface UiState {
    object Loading : UiState
    data class Success(val data: String) : UiState
    data class Error(val message: String) : UiState
    object Empty : UiState
}

// Example 1: Shared constructor data - ALL events need userId and timestamp
sealed class UserEvent(val userId: String, val timestamp: Long) {
    class Login(userId: String, val deviceId: String) : UserEvent(userId, System.currentTimeMillis())
    class Logout(userId: String, val reason: String) : UserEvent(userId, System.currentTimeMillis())
    class Purchase(userId: String, val amount: Double, val itemId: String) : UserEvent(userId, System.currentTimeMillis())
    
    // Shared behavior using the common data
    fun getEventAge(): Long = System.currentTimeMillis() - timestamp
    fun belongsToUser(id: String): Boolean = userId == id
}

// Example 2: Database operations - ALL need connection info
sealed class DatabaseQuery(val connectionId: String, val retryCount: Int = 0) {
    class SelectUser(connectionId: String, val userId: Int) : DatabaseQuery(connectionId)
    class InsertOrder(connectionId: String, val orderId: Int, val data: String) : DatabaseQuery(connectionId)
    class UpdateProfile(connectionId: String, val profileId: Int) : DatabaseQuery(connectionId, retryCount = 3)
    
    fun shouldRetry(): Boolean = retryCount > 0
    fun getConnectionInfo(): String = "Connection: $connectionId, Retries: $retryCount"
}

// Counter-example: This should be INTERFACE because no shared data needed
sealed interface Shape {  // ✅ Interface - no shared constructor data
    data class Circle(val radius: Double) : Shape
    data class Rectangle(val width: Double, val height: Double) : Shape
    data class Triangle(val base: Double, val height: Double) : Shape
}

// Sealed interface for payment methods
sealed interface PaymentMethod {
    data class CreditCard(val number: String, val expiryDate: String) : PaymentMethod
    data class PayPal(val email: String) : PaymentMethod
    data class BankTransfer(val accountNumber: String) : PaymentMethod
    object Cash : PaymentMethod
}

// Exhaustive when expressions with sealed classes
fun calculate(operation: Operation): Number {
    return when (operation) {
        is Operation.Add -> operation.a + operation.b
        is Operation.Subtract -> operation.a - operation.b
        is Operation.Multiply -> operation.a * operation.b
        is Operation.Divide -> operation.a.toDouble() / operation.b
        // No else clause needed - compiler ensures exhaustiveness
        else -> {
            3
        }
    }
}

fun handleUiState(state: UiState) {
    when (state) {
        is UiState.Loading -> println("Loading...")
        is UiState.Success -> println("Success: ${state.data}")
        is UiState.Error -> println("Error: ${state.message}")
        is UiState.Empty -> println("No data available")
        // Exhaustive - all cases covered
    }
}

fun processUserEvent(event: UserEvent): String {
    return when (event) {
        is UserEvent.Login -> "User ${event.userId} logged in from device ${event.deviceId} at ${event.timestamp}"
        is UserEvent.Logout -> "User ${event.userId} logged out: ${event.reason} at ${event.timestamp}"
        is UserEvent.Purchase -> "User ${event.userId} purchased item ${event.itemId} for $${event.amount} at ${event.timestamp}"
    }
}

fun processPayment(method: PaymentMethod): String {
    return when (method) {
        is PaymentMethod.CreditCard -> "Processing card ending in ${method.number.takeLast(4)}"
        is PaymentMethod.PayPal -> "Processing PayPal payment for ${method.email}"
        is PaymentMethod.BankTransfer -> "Processing bank transfer from ${method.accountNumber}"
        is PaymentMethod.Cash -> "Cash payment received"
        // Exhaustive when with sealed interface
    }
}

fun main() {
    println("=== SEALED CLASSES AND INTERFACES DEMO ===")
    
    // Sealed class example
    println("\n1. Mathematical Operations:")
    val operations = listOf(
        Operation.Add(5, 3),
        Operation.Subtract(10, 4),
        Operation.Multiply(4, 7),
        Operation.Divide(15, 3)
    )
    
    operations.forEach { op ->
        val result = calculate(op)
        println("${op::class.simpleName}: $result")
    }
    
    // UI State example
    println("\n2. UI State Management:")
    val states = listOf(
        UiState.Loading,
        UiState.Success("User data loaded successfully"),
        UiState.Error("Network timeout"),
        UiState.Empty
    )
    
    states.forEach { state ->
        handleUiState(state)
    }
    
    // User event example (showing shared constructor data)
    println("\n3. User Event Tracking:")
    val events = listOf(
        UserEvent.Login("user123", "device456"),
        UserEvent.Purchase("user123", 29.99, "item789"),
        UserEvent.Logout("user123", "manual logout")
    )
    
    events.forEach { event ->
        println(processUserEvent(event))
        println("  -> Event age: ${event.getEventAge()}ms")
        println("  -> Belongs to user123: ${event.belongsToUser("user123")}")
    }
    
    // Payment method example
    println("\n4. Payment Processing:")
    val payments = listOf(
        PaymentMethod.CreditCard("1234567890123456", "12/25"),
        PaymentMethod.PayPal("user@example.com"),
        PaymentMethod.BankTransfer("ACC123456789"),
        PaymentMethod.Cash
    )
    
    payments.forEach { payment ->
        println(processPayment(payment))
    }
    
    // Demonstrating type safety
    println("\n5. Type Safety Benefits:")
    val currentState: UiState = UiState.Success("Data")
    
    // Smart casting after when expression
    val message = when (currentState) {
        is UiState.Loading -> "Please wait..."
        is UiState.Success -> "Got: ${currentState.data}" // Smart cast to Success
        is UiState.Error -> "Error: ${currentState.message}" // Smart cast to Error
        is UiState.Empty -> "Nothing to show"
    }
    println("Current message: $message")
}

inline fun <reified T> emptyListOf(): Array<T> {
    return emptyArray<T>()
}


inline fun <reified T> checkType(obj: Any): Boolean = obj is T


fun <T> isInstanceOf(obj: T): Boolean {
    return obj is String
}


inline fun addaa(op: (a: Int, b: Int) -> Int): Int {
    return op(5, 6)
}

fun <T> MutableList<T>.customFilter(predicate: (T) -> Boolean) {
    var index = 0
    while (index in 0..this.size - 1) {
        if (predicate(this[index])) {
            removeAt(index)
        } else {
            index++;
        }
    }
}


infix fun String.concat(string: String): String {
    val map = { 1 to 2; 3 to 4; 5 to 4 }

    return this + string
}


open class Animal {
    open fun walk() {
        println("Animal is walking")
    }
}


class Dog : Animal() {
    override fun walk() {
        println("Dog is walking")
    }
}

class Car(override val color: String) : Vehicle() {
    internal val tyres = 4
    override fun horsepower() {
        println("Car horsepower is 150")
    }
}

abstract class Vehicle {
    val cyclinders = 2

    abstract val color: String
    abstract fun horsepower()

    fun startEngine() {
        println("Engine started")
    }
}

interface VehicleInerface {
    fun horsepower()
    fun startEngine() {
        println("Engine started from interface")
    }
}

// Thread safety example
class SharedCounter {
    val list: MutableList<Int> by lazy {mutableListOf()}  // val but NOT thread safe!
    var count: Int = 0  // var - definitely not thread safe
}

fun threadSafetyExample() {
    val sharedObject = SharedCounter()

    // Thread 1
    Thread {
        repeat(10000000) {
            sharedObject.list.add(it)  // Race condition!
            sharedObject.count++       // Race condition!
        }
        println("Thread 1 finished")
    }.start()
    
    // Thread 2  
    Thread {
       repeat(10000000) {
            sharedObject.list.add(it)  // Race condition!
            sharedObject.count++       // Race condition!
        }
        println("Thread 2 finished")
    }.start()
    
    // Wait and check results
    Thread.sleep(2000)
    println("List size: ${sharedObject.list.size}")  // Should be 2000, but might be less
    println("Count: ${sharedObject.count}")           // Should be 2000, but might be less
}


