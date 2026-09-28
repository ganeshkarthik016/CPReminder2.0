package com.example.cpreminder20

import java.util.Calendar

internal object SubmissionChecker {
    suspend fun hasAcceptedSubmissionToday(handle: String): Boolean {
        val todayStart = startOfLocalDay()
        var from = 1
        var hasAccepted = false
        var hasMoreToday = true
        while (hasMoreToday && !hasAccepted) {
            val response = RetrofitInstance.api.getUserSubmissions(handle, from, 1000)
            if (response.status != "OK") throw IllegalStateException("Codeforces returned ${response.status}")
            val page = response.result
            hasAccepted = hasAcceptedSubmissionToday(page, todayStart)
            hasMoreToday = page.size == 1000 &&
                page.last().creationTimeSeconds * 1000L >= todayStart
            from += page.size
        }
        return hasAccepted
    }

    fun hasAcceptedSubmissionToday(submissions: List<Submission>, todayStartMillis: Long): Boolean =
        submissions.any {
            it.creationTimeSeconds * 1000L >= todayStartMillis && it.verdict == "OK"
        }

    private fun startOfLocalDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
