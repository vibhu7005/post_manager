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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.demoapplication.service.MusicPlayerService
import com.example.demoapplication.service.PlayerService

class MainActivity : ComponentActivity() {

    var musicService : MusicPlayerService? = null
    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intent = Intent(this, MusicPlayerService::class.java)

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: android.content.ComponentName?, binder: android.os.IBinder?) {
                Log.d("MainActivity", "Service connected")
                musicService = (binder as MusicPlayerService.MusicPlayerBinder).getService()
            }

            override fun onServiceDisconnected(name: android.content.ComponentName?) {
                Log.d("MainActivity", "Service disconnected")
                musicService = null
            }
        }

        bindService(intent, serviceConnection!!, BIND_AUTO_CREATE)

        setContent {
            MaterialTheme {
                AudioPlayerScreen()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        serviceConnection?.let {
            unbindService(it)
        }
//        serviceConnection?.let { connection ->
//            unbindService(connection)

//        }
        musicService = null
    }

    @Composable
    fun AudioPlayerScreen() {
        var isPlaying by remember { mutableStateOf(false) }
        
        // Set up listeners when service connects
        LaunchedEffect(musicService) {
            musicService?.let { service ->
                isPlaying = service.isPlaying()
                service.setStateChangeListener { playing ->
                    isPlaying = playing
                }
                service.setPlaybackListener {
                    isPlaying = false
                }
            }
        }
        
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
                    onClick = {
                        val intent = Intent(this@MainActivity, MusicPlayerService::class.java)
                        startService(intent)
                        if (isPlaying) {
                            musicService?.pauseMusic()
                        } else {
                            musicService?.playMusic()
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(if (isPlaying) "Pause" else "Play")
                }

                Text(
                    text = if (isPlaying) "Playing..." else "Ready to play",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

