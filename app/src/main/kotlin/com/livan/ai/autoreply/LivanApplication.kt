package com.livan.ai.autoreply

import android.app.Application
import android.util.Log

class LivanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("LivanAI", "CRASH in thread ${thread.name}: ${throwable.message}", throwable)
            try {
                val prefs = getSharedPreferences("livan_crash_log", MODE_PRIVATE)
                prefs.edit()
                    .putString("last_crash", "${throwable::class.java.simpleName}: ${throwable.message}\n${throwable.stackTraceToString()}")
                    .putLong("crash_time", System.currentTimeMillis())
                    .commit()
            } catch (e: Exception) {
                Log.e("LivanAI", "Failed to persist crash", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
