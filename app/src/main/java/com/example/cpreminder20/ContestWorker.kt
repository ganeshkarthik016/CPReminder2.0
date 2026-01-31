package com.example.cpreminder20

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ContestWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Fetch upcoming contests
            val response = RetrofitInstance.api.getContestList(gym = false)
            val contests = response.result.filter { it.phase == "BEFORE" }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val currentTime = System.currentTimeMillis() / 1000 // Current time in seconds

            // 2. Loop through contests
            for (contest in contests) {
                // Calculate trigger time (Start Time - 30 mins)
                val triggerTimeSeconds = contest.startTimeSeconds - 1800

                // If the alarm time is in the future (and within the next 24 hours to be safe)
                if (triggerTimeSeconds > currentTime && triggerTimeSeconds < currentTime + 86400) {
                    scheduleAlarm(context, alarmManager, contest.id, contest.name, triggerTimeSeconds * 1000)
                    Log.d("CP_REMINDER", "Scheduled alarm for ${contest.name} at $triggerTimeSeconds")
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun scheduleAlarm(context: Context, alarmManager: AlarmManager, id: Int, name: String, timeMillis: Long) {
        val intent = Intent(context, ContestReceiver::class.java).apply {
            putExtra("ALARM_TITLE", "🏆 Contest in 30 Mins!")
            putExtra("ALARM_MESSAGE", "Get ready for: $name")
        }

        // Use the Contest ID as the RequestCode so we update existing alarms instead of creating duplicates
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the Exact Alarm
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Android 12+ requires explicit permission, which we added in Manifest
            e.printStackTrace()
        }
    }
}