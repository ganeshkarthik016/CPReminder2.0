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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

        lifecycleScope.launch {
            if (PreferenceManager(this@MainActivity).isDailyCheckOn.first()) {
                DailyAlarmScheduler.schedule(this@MainActivity)
            } else {
                DailyAlarmScheduler.cancel(this@MainActivity)
            }
        }
        scheduleContestSync(this)

        setContent {
            ProfileScreen()
        }
    }

    private fun scheduleContestSync(context: Context) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        // Remove the former inexact 24-hour daily worker so it cannot run alongside
        // the local-time AlarmManager schedule after an app upgrade.
        workManager.cancelUniqueWork("DailyCPCheck")
        // Run this check once every 12 hours
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<ContestWorker>(12, TimeUnit.HOURS).build()

        workManager.enqueueUniquePeriodicWork(
            "ContestSyncWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
