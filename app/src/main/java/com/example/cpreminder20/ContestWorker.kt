package com.example.cpreminder20

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ContestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        return try {
            // 1. Fetch Contests from Codeforces
            val response = RetrofitInstance.api.getContestList()
            if (response.status == "OK") {
                val contests = response.result

                // 2. Filter: Only upcoming contests
                val upcomingContests = contests.filter { it.phase == "BEFORE" }

                for (contest in upcomingContests) {
                    // Codeforces time is in Seconds, convert to Milliseconds
                    val startTimeMillis = contest.startTimeSeconds * 1000L

                    // 3. Set Alarm for 30 MINUTES BEFORE start
                    val triggerTime = startTimeMillis - (30 * 60 * 1000)

                    // Only schedule if the time is in the future
                    if (triggerTime > System.currentTimeMillis()) {
                        scheduleAlarm(context, contest.name, triggerTime, contest.id)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun scheduleAlarm(context: Context, name: String, triggerTime: Long, contestId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Prepare the Intent to trigger 'ContestReceiver'
        val intent = Intent(context, ContestReceiver::class.java).apply {
            putExtra("ALARM_TITLE", "🏆 Contest in 30 Mins!")
            putExtra("ALARM_MESSAGE", "Get ready for: $name")
        }

        // 2. Create a Unique PendingIntent using the Contest ID
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            contestId, // Unique ID ensures one alarm per contest
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Schedule the Exact Alarm
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("ContestWorker", "Scheduled alarm for $name at $triggerTime")
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}