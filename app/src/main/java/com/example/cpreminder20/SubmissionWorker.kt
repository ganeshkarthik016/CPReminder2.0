package com.example.cpreminder20

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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

        // 1. CHECK IF FEATURE IS ENABLED
        val isDailyOn = prefs.isDailyCheckOn.first()
        if (!isDailyOn) {
            return Result.success() // User turned this off, do nothing.
        }

        // 2. CHECK HANDLE
        val handle = prefs.getHandle.first() ?: return Result.failure()

        return try {
            // 3. FETCH SUBMISSIONS
            val response = RetrofitInstance.api.getUserSubmissions(handle)
            if (response.status == "OK") {
                val submissions = response.result

                // 4. CHECK IF SOLVED TODAY
                val todayStart = getStartOfDay()

                // Count submissions made AFTER 12:00 AM today
                val solvedTodayCount = submissions.count {
                    (it.creationTimeSeconds * 1000L) > todayStart
                }

                if (solvedTodayCount == 0) {
                    // ⚠️ DANGER: NO SUBMISSIONS FOUND! TRIGGER ALARM! ⚠️
                    triggerAlarm(context, handle)
                } else {
                    Log.d("SubmissionWorker", "Safe! User solved $solvedTodayCount problems today.")
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun triggerAlarm(context: Context, handle: String) {
        // Start the EXACT SAME Service we used for Contests
        // This ensures the "Stop" button works perfectly.
        val intent = Intent(context, AlarmService::class.java).apply {
            putExtra("TITLE", "⚠️ Maintain Your Streak!")
            putExtra("MESSAGE", "Hey $handle, you haven't solved any problems today!")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}