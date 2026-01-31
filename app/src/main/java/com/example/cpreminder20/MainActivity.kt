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

        // STOP ALARM ON APP OPEN
        val stopIntent = Intent(this, AlarmService::class.java).apply { action = "STOP" }
        startService(stopIntent)

        // Permission Request
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

        dueDate.set(Calendar.HOUR_OF_DAY, 22) // 10 PM
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
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<ContestWorker>(12, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork("ContestSyncWork", androidx.work.ExistingPeriodicWorkPolicy.UPDATE, syncRequest)
    }
}