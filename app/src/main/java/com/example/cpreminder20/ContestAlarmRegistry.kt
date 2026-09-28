package com.example.cpreminder20

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

internal object ContestAlarmRegistry {
    private const val PREFS = "scheduled_contest_alarms"
    private const val IDS = "contest_ids"

    fun pendingIntent(context: Context, contestId: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        contestId,
        Intent(context, ContestReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun remember(context: Context, contestId: Int) {
        val ids = storedIds(context).toMutableSet().apply { add(contestId) }
        save(context, ids)
    }

    fun reconcile(context: Context, upcomingIds: Set<Int>) {
        val staleIds = storedIds(context) - upcomingIds
        staleIds.forEach { cancel(context, it) }
        save(context, upcomingIds)
    }

    fun cancelAll(context: Context) {
        storedIds(context).forEach { cancel(context, it) }
        save(context, emptySet())
    }

    private fun cancel(context: Context, id: Int) {
        val pendingIntent = pendingIntent(context, id)
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun storedIds(context: Context): Set<Int> = context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet(IDS, emptySet())
        ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()

    private fun save(context: Context, ids: Set<Int>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putStringSet(IDS, ids.map { it.toString() }.toSet()).apply()
    }
}
