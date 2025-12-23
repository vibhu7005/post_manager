package com.example.demoapplication

import android.media.MediaPlayer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                AudioPlayerScreen()
            }
        }
    }
}

@Composable
fun AudioPlayerScreen() {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    
    DisposableEffect(context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.short_test).apply {
            setOnCompletionListener {
                isPlaying = false
                Log.d("AudioPlayer", "Audio playback completed")
            }
            setOnPreparedListener {
                Log.d("AudioPlayer", "MediaPlayer prepared successfully")
            }
            setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                isPlaying = false
                true
            }
        }
        
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    
    fun playAudio() {
        mediaPlayer?.let { player ->
            try {
                if (!player.isPlaying) {
                    player.start()
                    isPlaying = true
                    Log.d("AudioPlayer", "Audio playback started")
                } else {
                    Log.d("AudioPlayer", "Audio is already playing")
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error starting playback: ${e.message}")
            }
        } ?: Log.e("AudioPlayer", "MediaPlayer is null")
    }
    
    fun pauseAudio() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                    isPlaying = false
                    Log.d("AudioPlayer", "Audio playback paused")
                } else {
                    Log.d("AudioPlayer", "Audio is not playing")
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error pausing playback: ${e.message}")
            }
        } ?: Log.e("AudioPlayer", "MediaPlayer is null")
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
            
            if (isPlaying) {
                Button(
                    onClick = { pauseAudio() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Pause")
                }
            } else {
                Button(
                    onClick = { playAudio() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Play")
                }
            }
            
            Text(
                text = if (isPlaying) "Playing audio..." else "Audio stopped",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "File: sample_audio.mp3",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}