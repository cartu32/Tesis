package com.example.comunicationwearmobile.models

import android.content.Context

object SpNotificationCounter {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_COUNT = "notification_count"

    fun increment(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_COUNT, count).apply()
        return count
    }

    fun decrement(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = (prefs.getInt(KEY_COUNT, 0) - 1).coerceAtLeast(0)
        prefs.edit().putInt(KEY_COUNT, count).apply()
        return count
    }

    fun getCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COUNT, 0)
    }
}
