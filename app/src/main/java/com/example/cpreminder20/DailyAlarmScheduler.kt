package com.example.cpreminder20

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar
import java.util.TimeZone

internal object DailyAlarmScheduler {
    private const val REQUEST_CODE = 1030
    private const val ACTION_DAILY_CHECK = "com.example.cpreminder20.DAILY_CHECK"

    fun nextOccurrence(nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val due = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return due.timeInMillis
    }

    fun schedule(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context)
        val triggerAt = nextOccurrence(nowMillis)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (e: SecurityException) {
            // Keep a best-effort daily check if exact-alarm access has not been granted.
            Log.w("DailyAlarmScheduler", "Exact alarm access unavailable; scheduling an inexact alarm", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, DailyAlarmReceiver::class.java).setAction(ACTION_DAILY_CHECK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
