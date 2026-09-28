package com.example.cpreminder20

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

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
            val solvedToday = SubmissionChecker.hasAcceptedSubmissionToday(handle)
            if (!solvedToday) {
                triggerAlarm(context, handle)
            } else {
                Log.d("SubmissionWorker", "Safe! User has an accepted submission today.")
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

}
