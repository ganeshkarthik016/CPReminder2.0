package com.example.cpreminder20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (PreferenceManager(appContext).isDailyCheckOn.first()) {
                    // Recalculate from the current local wall clock after every firing.
                    DailyAlarmScheduler.schedule(appContext)
                    val request = OneTimeWorkRequestBuilder<SubmissionWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                    WorkManager.getInstance(appContext).enqueueUniqueWork(
                        workNameForToday(), ExistingWorkPolicy.KEEP, request
                    )
                } else {
                    DailyAlarmScheduler.cancel(appContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    internal fun workNameForToday(): String {
        val today = Calendar.getInstance()
        return "DailyCPCheck-${today.get(Calendar.YEAR)}-${today.get(Calendar.DAY_OF_YEAR)}"
    }
}
