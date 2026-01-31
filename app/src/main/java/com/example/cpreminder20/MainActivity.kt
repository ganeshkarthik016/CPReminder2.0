package com.example.cpreminder20

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- SAFETY LOCK: FORCE STOP ALARM ON OPEN ---
        // This ensures the alarm never rings just because you opened the app.
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP"
        }
        startService(stopIntent)

        // Request Permissions (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        scheduleDailyCheck(this)
        scheduleContestSync(this)

        setContent {
            ProfileScreen()
        }
    }

    private fun scheduleDailyCheck(context: Context) {
        val workManager = androidx.work.WorkManager.getInstance(context)

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        // Set Execution time to 10:30 PM
        dueDate.set(Calendar.HOUR_OF_DAY, 22)
        dueDate.set(Calendar.MINUTE, 30)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyWorkRequest = androidx.work.PeriodicWorkRequestBuilder<SubmissionWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DailyCPCheck",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    private fun scheduleContestSync(context: Context) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        // Run this check once every 12 hours
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<ContestWorker>(12, TimeUnit.HOURS).build()

        workManager.enqueueUniquePeriodicWork(
            "ContestSyncWork",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }
}