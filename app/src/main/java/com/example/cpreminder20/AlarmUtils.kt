package com.example.cpreminder20

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager

object AlarmUtils {
    private var currentRingtone: Ringtone? = null

    fun playSound(context: Context) {
        try {
            // 1. Always stop any existing sound first
            stopSound()

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            currentRingtone = RingtoneManager.getRingtone(context, alarmUri)

            // 2. Force it to play as an "Alarm" so it's loud
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                currentRingtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }

            currentRingtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSound() {
        try {
            if (currentRingtone != null) {
                if (currentRingtone!!.isPlaying) {
                    currentRingtone!!.stop()
                }
                currentRingtone = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}