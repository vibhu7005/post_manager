package com.example.demoapplication

import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.demoapplication.service.MusicPlayerService
import com.example.demoapplication.service.PlayerService

class MainActivity : ComponentActivity() {

    var musicService : MusicPlayerService? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val intent = Intent(this, MusicPlayerService::class.java)

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: android.content.ComponentName?, binder: android.os.IBinder?) {
                Log.d("MainActivity", "Service connected")
                musicService = (binder as MusicPlayerService.MusicPlayerBinder).getService()
            }

            override fun onServiceDisconnected(name: android.content.ComponentName?) {
                Log.d("MainActivity", "Service disconnected")
            }
        }

        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        
        setContent {
            MaterialTheme {
                AudioPlayerScreen()
            }
        }
    }

    @Composable
    fun AudioPlayerScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Audio Player",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { musicService?.playMusic() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Play")
                }

                Text(
                    text = "Ready to play",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

