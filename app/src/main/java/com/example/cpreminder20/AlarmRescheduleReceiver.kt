package com.example.cpreminder20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy

class AlarmRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED
            )) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (PreferenceManager(appContext).isDailyCheckOn.first()) {
                    DailyAlarmScheduler.schedule(appContext)
                }
                androidx.work.WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                    "ContestSyncWork",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    androidx.work.PeriodicWorkRequestBuilder<ContestWorker>(12, java.util.concurrent.TimeUnit.HOURS).build()
                )
                androidx.work.WorkManager.getInstance(appContext).enqueueUniqueWork(
                    "ContestSyncAfterRestart",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<ContestWorker>().build()
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
