package com.example.cpreminder20

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ContestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // --- STOP LOGIC (Priority #1) ---
        if (intent.action == "ACTION_STOP_ALARM") {
            // 1. Kill the sound
            AlarmUtils.stopSound()

            // 2. Kill the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001)

            // 3. Feedback
            Toast.makeText(context, "Alarm Stopped", Toast.LENGTH_SHORT).show()
            return
        }

        // --- START LOGIC ---
        val pendingResult = goAsync()
        val prefs = PreferenceManager(context)
        val goAhead = runBlocking { prefs.isAlarmEnabled.first() }

        if (goAhead) {
            val title = intent.getStringExtra("ALARM_TITLE") ?: "🏆 Contest in 30 Mins!"
            val message = intent.getStringExtra("ALARM_MESSAGE") ?: "Get ready!"

            // 1. Play Sound
            AlarmUtils.playSound(context)

            // 2. Show Notification
            showNotification(context, title, message)

            // 3. Auto-stop after 20 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                AlarmUtils.stopSound()
                pendingResult.finish()
            }, 20000)
        } else {
            pendingResult.finish()
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "contest_alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "CP Alarms", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // A. Intent for the "Stop" Button
        val stopIntent = Intent(context, ContestReceiver::class.java).apply {
            action = "ACTION_STOP_ALARM" // <--- We call OURSELVES with this command
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            888, // Unique Request Code
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // B. Intent for Clicking the Body (Opens App)
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent) // Click body -> Open App
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP ALARM", stopPendingIntent) // Click button -> Stop Sound
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        notificationManager.notify(1001, notification)
    }
}