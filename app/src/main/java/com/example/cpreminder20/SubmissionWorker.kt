package com.example.cpreminder20

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.util.Calendar

class SubmissionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = PreferenceManager(context)

        // 1. Check if we have a handle saved
        val handle = prefs.getHandle.first() ?: return Result.failure()

        return try {
            // 2. Check Codeforces API
            val response = RetrofitInstance.api.getUserSubmissions(handle)
            val submissions = response.result
            val startOfDay = getStartOfDaySeconds()
            val submissionsToday = submissions.count { it.creationTimeSeconds >= startOfDay }

            // 3. If lazy (0 submissions), TRIGGER THE LOUD ALARM
            if (submissionsToday == 0) {
                triggerLoudAlarm(context)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun triggerLoudAlarm(context: Context) {
        // We broadcast to ContestReceiver so it plays the 20s Ringtone!
        val intent = Intent(context, ContestReceiver::class.java).apply {
            putExtra("ALARM_TITLE", "⚠️ NO SUBMISSIONS TODAY!")
            putExtra("ALARM_MESSAGE", "You haven't solved a single problem. Get to work!")
        }
        context.sendBroadcast(intent)
    }

    private fun getStartOfDaySeconds(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis / 1000
    }
}