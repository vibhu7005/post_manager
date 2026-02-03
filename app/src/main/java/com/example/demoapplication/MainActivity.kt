package com.example.demoapplication

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

// Declare CompositionLocal at top level (outside class)
val LocalTheme = compositionLocalOf { "Light" }
val LocalUser = compositionLocalOf<User?> { null }

data class User(val name: String, val email: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CompositionLocalDemo()
                }
            }
        }
    }


    @Composable
    fun CompositionLocalDemo() {
        var currentTheme by remember { mutableStateOf("Dark") }
        var currentUser by remember { mutableStateOf<User?>(User("John Doe", "john@example.com")) }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "CompositionLocal Demo",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Show default value access (without provider)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("0. Parent Code OUTSIDE Provider", style = MaterialTheme.typography.titleMedium)
                // ❌ Parent code OUTSIDE provider gets DEFAULT value
                Text("Parent theme: ${LocalTheme.current} (default)")  // "Light"
                Text("Parent user: ${LocalUser.current?.name ?: "null (default)"}")  // null
                Text(
                    "✓ Parent code OUTSIDE provider gets DEFAULT value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Show what happens when Child is called from outside provider
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("0.5. Child Called OUTSIDE Provider", style = MaterialTheme.typography.titleMedium)
                // Child called from OUTSIDE provider - gets default
                ChildOutsideProvider()
                Text(
                    "✓ Child called from OUTSIDE provider gets DEFAULT value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Divider()
            
            // Provide values using CompositionLocalProvider (OVERRIDES default for CHILDREN)
            CompositionLocalProvider(
                LocalTheme provides currentTheme,
                LocalUser provides currentUser
            ) {
                // ✅ Code INSIDE provider gets PROVIDED value
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inside Provider Scope", style = MaterialTheme.typography.titleSmall)
                    Text("Theme: ${LocalTheme.current} (provided)")  // Gets currentTheme
                    Text("User: ${LocalUser.current?.name ?: "null"}")  // Gets currentUser
                    Text(
                        "✓ Code INSIDE provider gets PROVIDED value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                // Example 1: Accessing theme
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Theme Provider", style = MaterialTheme.typography.titleMedium)
                    ThemedContent()
                    Button(
                        onClick = { currentTheme = if (currentTheme == "Dark") "Light" else "Dark" }
                    ) {
                        Text("Toggle Theme")
                    }
                }
                
                Divider()
                
                // Example 2: Accessing user
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. User Provider", style = MaterialTheme.typography.titleMedium)
                    UserProfile()
                    Button(
                        onClick = { 
                            currentUser = if (currentUser != null) null else User("Jane Doe", "jane@example.com")
                        }
                    ) {
                        Text(if (currentUser != null) "Logout" else "Login")
                    }
                }
                
                Divider()
                
                // Example 3: Nested providers (override)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Nested Providers", style = MaterialTheme.typography.titleMedium)
                    Text("Outer theme: ${LocalTheme.current}")
                    CompositionLocalProvider(LocalTheme provides "System") {
                        Text("Inner theme: ${LocalTheme.current} (overrides outer)")
                    }
                    Text("Back to outer theme: ${LocalTheme.current}")
                }
                
                Divider()
                
                // Example 4: Deep nesting (no prop drilling)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("4. No Prop Drilling", style = MaterialTheme.typography.titleMedium)
                    DeepNestedComponent()
                }
                
                Divider()
                
                // Example 5: LocalContext (Built-in, Framework Sets It)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("5. LocalContext (Built-in)", style = MaterialTheme.typography.titleMedium)
                    Text("Context type: ${LocalContext.current.javaClass.simpleName}")
                    Text("Package: ${LocalContext.current.packageName}")
                    Text(
                        "✓ Framework automatically provides LocalContext when setContent() is called",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = {
                            Toast.makeText(
                                LocalContext.current,
                                "LocalContext works!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Text("Test LocalContext")
                    }
                }
            }
        }
    }
    
    @Composable
    fun ThemedContent() {
        val theme = LocalTheme.current
        Surface(
            color = if (theme == "Dark") Color.DarkGray else Color.LightGray,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                "Current theme: $theme",
                modifier = Modifier.padding(16.dp),
                color = if (theme == "Dark") Color.White else Color.Black
            )
        }
    }
    
    @Composable
    fun UserProfile() {
        val user = LocalUser.current
        if (user != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Name: ${user.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Text("No user logged in", style = MaterialTheme.typography.bodyMedium)
        }
    }
    
    @Composable
    fun DeepNestedComponent() {
        // No need to pass theme/user through parameters!
        Level1()
    }
    
    @Composable
    fun Level1() {
        // Can access CompositionLocal directly
        val theme = LocalTheme.current
        Text("Level 1 - Theme: $theme")
        Level2()
    }
    
    @Composable
    fun Level2() {
        val theme = LocalTheme.current
        val user = LocalUser.current
        Text("Level 2 - Theme: $theme, User: ${user?.name ?: "None"}")
        Level3()
    }
    
    @Composable
    fun Level3() {
        val theme = LocalTheme.current
        Text("Level 3 - Theme: $theme (accessed directly, no prop drilling!)")
    }
    
    @Composable
    fun ChildOutsideProvider() {
        // ❌ Called from OUTSIDE provider - gets DEFAULT value
        val theme = LocalTheme.current  // "Light" (default)
        val user = LocalUser.current     // null (default)
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Child (outside): Theme = $theme, User = ${user?.name ?: "null"}")
            }
        }
    }

    @Composable
    fun ModifiersDemo() {
        var clickCount1 by remember { mutableStateOf(0) }
        var clickCount2 by remember { mutableStateOf(0) }
        var box1Size by remember { mutableStateOf("") }
        var box2Size by remember { mutableStateOf("") }
        val density = LocalDensity.current
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                "Clickable vs Padding Order Comparison",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Example 1: clickable THEN padding
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "❌ clickable() THEN padding()",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Red
                )
                Text("Clicks: $clickCount1 | Box size: $box1Size", style = MaterialTheme.typography.bodySmall)
                
                Box(
                    modifier = Modifier
                        .clickable { clickCount1++ }
                        .padding(20.dp)      // Padding INSIDE clickable area
                        .background(Color.Blue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                box1Size = "${coordinates.size.width.toDp()}x${coordinates.size.height.toDp()}"
                            }
                        }
                ) {
                    Text(
                        "Click me",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Blue
                    )
                }
                Text(
                    "• Clickable area = Box size (smaller)\n• Padding is INSIDE the clickable area",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Divider()
            
            // Example 2: padding THEN clickable
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "✅ padding() THEN clickable()",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Green
                )
                Text("Clicks: $clickCount2 | Box size: $box2Size", style = MaterialTheme.typography.bodySmall)
                
                Box(
                    modifier = Modifier
                        .padding(20.dp)      // Padding OUTSIDE
                        .clickable { clickCount2++ }  // Entire padded area is clickable
                        .background(Color.Green.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                box2Size = "${coordinates.size.width.toDp()}x${coordinates.size.height.toDp()}"
                            }
                        }
                ) {
                    Text(
                        "Click me",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Green
                    )
                }
                Text(
                    "• Clickable area = Box + padding (larger)\n• Padding is OUTSIDE, expands clickable area",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Key differences explanation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Key Differences:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Clickable Area: Different sizes", style = MaterialTheme.typography.bodyMedium)
                    Text("2. Visual: Similar IF no background", style = MaterialTheme.typography.bodyMedium)
                    Text("3. Background: Would cover different areas", style = MaterialTheme.typography.bodyMedium)
                    Text("4. UX: Larger clickable area = better UX", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    
    @Composable
    fun Divider() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
        )
    }


    suspend fun CoroutineScope.exec() {
        supervisorScope {
            delay(200)
            println("hello")
        }

        launch {
            delay(1300)
            println("nine")
        }

    }


    fun add(a: Int, b: Int) = a + b
}



