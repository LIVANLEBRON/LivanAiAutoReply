package com.livan.ai.autoreply.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HistoryRepository(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private fun loadHistory(): List<HistoryItem> {
        val raw = prefs.getString(KEY_HISTORY, null)
        return if (raw != null) {
            try {
                json.decodeFromString<List<HistoryItem>>(raw)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun persist(items: List<HistoryItem>) {
        _history.value = items
        prefs.edit().putString(KEY_HISTORY, json.encodeToString(items)).apply()
    }

    @Synchronized
    fun addEvent(item: HistoryItem) {
        val current = _history.value.toMutableList()
        current.add(0, item) // most recent on top
        // Keep max 200 items in history
        val trimmed = if (current.size > 200) current.take(200) else current
        persist(trimmed)
    }

    fun clearHistory() {
        persist(emptyList())
    }

    companion object {
        private const val PREFS_NAME = "livan_history_prefs"
        private const val KEY_HISTORY = "key_history_items"

        @Volatile
        private var instance: HistoryRepository? = null

        fun getInstance(context: Context): HistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: HistoryRepository(context).also { instance = it }
            }
        }
    }
}
