package com.example.demoapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.demoapplication.worker.DummyWorker

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.d(TAG, "MainActivity created")

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WorkManagerScreen()
                }
            }
        }
    }
    
    @Composable
    fun WorkManagerScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "WorkManager Demo",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Button(
                onClick = {
                    startDummyWork()
                }
            ) {
                Text("Start Dummy Work")
            }
            
            Button(
                onClick = {
                    startDummyWorkWithConstraints()
                }
            ) {
                Text("Start Work with Network Constraint")
            }
            
            Button(
                onClick = {
                    cancelAllWork()
                }
            ) {
                Text("Cancel All Work")
            }
        }
    }
    
    private fun startDummyWork() {
        Log.d(TAG, "Starting periodic work...")
        
        val periodicRequest = PeriodicWorkRequestBuilder<DummyWorker>(
            repeatInterval = 15, // 15 minutes (minimum)
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
        .setInputData(workDataOf("task_name" to "periodic-task"))
        .addTag("periodic-work")
        .build()
        
        WorkManager.getInstance(this).enqueue(periodicRequest)
        Log.d(TAG, "Periodic work enqueued with ID: ${periodicRequest.id}")
    }
    
    private fun startDummyWorkWithConstraints() {
        Log.d(TAG, "Starting dummy work with network constraint...")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<DummyWorker>()
            .setConstraints(constraints)
            .addTag("constrained-work")
            .build()
        
        WorkManager.getInstance(this).enqueue(workRequest)
        Log.d(TAG, "Constrained work enqueued with ID: ${workRequest.id}")
    }
    
    private fun cancelAllWork() {
        Log.d(TAG, "Cancelling all work...")
        WorkManager.getInstance(this).cancelAllWork()
    }
}

