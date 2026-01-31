package com.example.cpreminder20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ContestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 1. Get the message from the Alarm schedule
        val title = intent.getStringExtra("ALARM_TITLE") ?: "Contest Alarm"
        val message = intent.getStringExtra("ALARM_MESSAGE") ?: "Time to code!"

        Log.d("ContestReceiver", "Alarm Received! Starting Service...")

        // 2. Prepare to start the Alarm Service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
        }

        // 3. Start the Service (Safe for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}