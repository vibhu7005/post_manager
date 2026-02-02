package com.example.demoapplication

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.demoapplication.coroutines.coldFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        CoroutineScope {
//            coldFlow().collect {
//                println(it)
//            }
//
//            coldFlow().collect {
//                println(it)
//            }
//
//            delay(10000)
//        }


//        textView = findViewById(androidx.compose.ui.R.id.hide_in_inspector_tag)
        lifecycleScope.launch {
            exec()
            println("jerry")
        }
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Clean slate for new project
                }
            }
        }
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



