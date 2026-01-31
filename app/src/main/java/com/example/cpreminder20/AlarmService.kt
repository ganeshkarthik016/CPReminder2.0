package com.example.cpreminder20

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    // CHANGED ID: V4 ensures a fresh start for the notification settings
    private val CHANNEL_ID = "CHANNEL_FINAL_V4"

    private val autoStopHandler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable { stopAlarm() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        // 1. HANDLE STOP BUTTON CLICK
        if (action == "STOP") {
            stopAlarm()
            return START_NOT_STICKY
        }

        val title = intent?.getStringExtra("TITLE") ?: "Alarm"
        val message = intent?.getStringExtra("MESSAGE") ?: "Wake up!"

        // 2. CREATE THE NOTIFICATION (This is the feature you wanted!)
        val notification = createNotification(title, message)

        // 3. START FOREGROUND (Android 14 Safe)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } catch (e: Exception) {
                startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)
        }

        playAlarm()

        return START_STICKY
    }

    private fun playAlarm() {
        stopAlarm()
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            // Auto-stop after 20 seconds
            autoStopHandler.postDelayed(autoStopRunnable, 20000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        autoStopHandler.removeCallbacks(autoStopRunnable)
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "Alarm Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    // --- HERE IS THE NOTIFICATION LOGIC YOU WANTED ---
    private fun createNotification(title: String, message: String): android.app.Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "CP Alarm Service", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Loud alarm for contests"
                enableVibration(true)
                setSound(null, null) // Silent because MediaPlayer plays the sound
            }
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, AlarmService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 500, 1000)) // Force Pop-up
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP ALARM", stopPendingIntent) // <--- HERE IT IS
            .build()
    }
}