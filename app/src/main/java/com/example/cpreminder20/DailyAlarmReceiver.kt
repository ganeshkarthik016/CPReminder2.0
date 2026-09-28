package com.example.cpreminder20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        // Keep the next wall-clock alarm armed and start the service while the
        // exact-alarm broadcast exemption is active. The service checks the API.
        DailyAlarmScheduler.schedule(appContext)
        val serviceIntent = Intent(appContext, AlarmService::class.java).apply {
            action = AlarmService.ACTION_CHECK_STREAK
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(serviceIntent)
        } else {
            appContext.startService(serviceIntent)
        }
    }
}
