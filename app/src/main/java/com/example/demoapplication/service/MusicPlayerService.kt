package com.example.demoapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.demoapplication.R

class MusicPlayerService : Service() {

    private var binder : IBinder? = MusicPlayerBinder()
    private var mediaPlayer : MediaPlayer? = null
    private var playbackListener: (() -> Unit)? = null
    
    companion object {
        const val CHANNEL_ID = "MUSIC_PLAYER_CHANNEL"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        mediaPlayer = MediaPlayer.create(this, R.raw.test_music)?.apply {
            setOnCompletionListener {
                Log.d("MusicPlayerService", "Music playback completed")
                // Reset MediaPlayer to beginning
                seekTo(0)
                // Notify listener that playback completed
                playbackListener?.invoke()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for music player foreground service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val isCurrentlyPlaying = mediaPlayer?.isPlaying ?: false
        
        // Create PendingIntents for play/pause actions
        val playPauseAction = if (isCurrentlyPlaying) {
            val pauseIntent = Intent(this, MusicPlayerService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getService(
                this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
        } else {
            val playIntent = Intent(this, MusicPlayerService::class.java).apply {
                action = ACTION_PLAY
            }
            val playPendingIntent = PendingIntent.getService(
                this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", playPendingIntent)
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(if (isCurrentlyPlaying) "Playing..." else "Paused")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(playPauseAction)
            .setOngoing(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .build()
    }

    fun playMusic() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                Log.d("MusicPlayerService", "Music started")
            }
        } ?: Log.e("MusicPlayerService", "MediaPlayer is null")
    }

    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                Log.d("MusicPlayerService", "Music paused")
            }
        } ?: Log.e("MusicPlayerService", "MediaPlayer is null")
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun setPlaybackListener(listener: (() -> Unit)?) {
        playbackListener = listener
    }


    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicPlayerService", "Service destroyed")
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                playMusic()
                updateNotification()
            }
            ACTION_PAUSE -> {
                pauseMusic()
                updateNotification()
            }
        }
        return START_NOT_STICKY
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}