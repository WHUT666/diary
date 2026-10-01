package com.example.util

import android.content.Context
import android.content.SharedPreferences

object DailyReminderManager {
    private const val PREFS_NAME = "diary_daily_reminder_prefs"
    private const val KEY_ENABLED = "key_reminder_enabled"
    private const val KEY_HOUR = "key_reminder_hour"
    private const val KEY_MINUTE = "key_reminder_minute"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getReminderTime(context: Context): Pair<Int, Int> {
        val prefs = getPrefs(context)
        val hour = prefs.getInt(KEY_HOUR, 21) // 21:00 default
        val minute = prefs.getInt(KEY_MINUTE, 30) // 21:30 default
        return Pair(hour, minute)
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        getPrefs(context).edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
    }

    fun getFormattedTime(hour: Int, minute: Int): String {
        val hStr = hour.toString().padStart(2, '0')
        val mStr = minute.toString().padStart(2, '0')
        return "$hStr:$mStr"
    }
}
