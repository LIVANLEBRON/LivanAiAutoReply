package com.livan.ai.autoreply.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _settings: MutableStateFlow<AppSettings>
    val settings: StateFlow<AppSettings>

    private val _contacts: MutableStateFlow<List<AuthorizedContact>>
    val contacts: StateFlow<List<AuthorizedContact>>

    init {
        val initialSettings = loadSettings()
        _settings = MutableStateFlow(initialSettings)
        settings = _settings.asStateFlow()

        val initialContacts = loadContacts()
        _contacts = MutableStateFlow(initialContacts)
        contacts = _contacts.asStateFlow()
    }

    private fun loadSettings(): AppSettings {
        val raw = prefs.getString(KEY_SETTINGS, null)
        val loaded = if (raw != null) {
            try {
                json.decodeFromString<AppSettings>(raw)
            } catch (_: Exception) {
                AppSettings()
            }
        } else {
            AppSettings()
        }
        // Auto-migrate old provisional settings to current live Tailscale IP
        var migrated = if (loaded.serverHost == "100.64.0.1" || loaded.serverHost == "10.0.0.29" || loaded.serverPort == 3000) {
            loaded.copy(
                serverHost = "100.89.243.4",
                serverPort = 11434,
                selectedModel = if (loaded.selectedModel == "llama3.2" || loaded.selectedModel == "lfm3:latest" || loaded.selectedModel == "ivan:latest") "livan:latest" else loaded.selectedModel,
                isMasterEnabled = true
            )
        } else if (loaded.selectedModel == "ivan:latest") {
            loaded.copy(selectedModel = "livan:latest")
        } else {
            loaded
        }

        // Migrate old assistant-style system prompt to real human WhatsApp prompt
        if (migrated.customSystemPrompt.contains("Eres un asistente conversacional", ignoreCase = true)) {
            migrated = migrated.copy(
                customSystemPrompt = PersonalityPreset.NATURAL.defaultPrompt,
                temperature = 0.4f
            )
        }

        if (migrated != loaded) {
            prefs.edit().putString(KEY_SETTINGS, json.encodeToString(migrated)).apply()
        }
        return migrated
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit().putString(KEY_SETTINGS, json.encodeToString(newSettings)).apply()
    }

    fun setMasterEnabled(enabled: Boolean) {
        updateSettings(_settings.value.copy(isMasterEnabled = enabled))
    }

    private fun loadContacts(): List<AuthorizedContact> {
        val raw = prefs.getString(KEY_CONTACTS, null)
        val list = if (raw != null) {
            try {
                json.decodeFromString<List<AuthorizedContact>>(raw)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        // Always ensure Devora is configured with her explicit individual context and Indifferente filter
        val hasDevora = list.any { it.name.contains("Devora", ignoreCase = true) || it.name.contains("Debora", ignoreCase = true) }
        val updatedList = if (!hasDevora) {
            val devoraContact = AuthorizedContact(
                name = "Devora",
                isEnabled = true,
                personality = PersonalityPreset.INDIFERENTE.name,
                relationshipContext = DEVORA_CONTEXT
            )
            list + devoraContact
        } else {
            list.map { contact ->
                if (contact.name.contains("Devora", ignoreCase = true) || contact.name.contains("Debora", ignoreCase = true)) {
                    val p = if (contact.personality == PersonalityPreset.NATURAL.name) PersonalityPreset.INDIFERENTE.name else contact.personality
                    val c = if (contact.relationshipContext.isNullOrBlank()) DEVORA_CONTEXT else contact.relationshipContext
                    contact.copy(personality = p, relationshipContext = c)
                } else {
                    contact
                }
            }
        }

        if (updatedList != list) {
            prefs.edit().putString(KEY_CONTACTS, json.encodeToString(updatedList)).apply()
        }
        return updatedList
    }

    private fun saveContacts(list: List<AuthorizedContact>) {
        _contacts.value = list
        prefs.edit().putString(KEY_CONTACTS, json.encodeToString(list)).apply()
    }

    fun addContact(contact: AuthorizedContact) {
        val current = _contacts.value.toMutableList()
        // Replace if already exists with same name or id
        current.removeAll { it.id == contact.id || it.name.equals(contact.name.trim(), ignoreCase = true) }
        current.add(contact)
        saveContacts(current)
    }

    fun updateContact(contact: AuthorizedContact) {
        val current = _contacts.value.map { if (it.id == contact.id) contact else it }
        saveContacts(current)
    }

    fun deleteContact(contactId: String) {
        val current = _contacts.value.filterNot { it.id == contactId }
        saveContacts(current)
    }

    fun toggleContactEnabled(contactId: String, enabled: Boolean) {
        val current = _contacts.value.map {
            if (it.id == contactId) it.copy(isEnabled = enabled) else it
        }
        saveContacts(current)
    }

    fun findAuthorizedContact(senderName: String): AuthorizedContact? {
        val trimmed = senderName.trim()
        return _contacts.value.firstOrNull { contact ->
            contact.isEnabled && (
                contact.name.equals(trimmed, ignoreCase = true) ||
                trimmed.contains(contact.name, ignoreCase = true) ||
                contact.name.contains(trimmed, ignoreCase = true)
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "livan_ai_autoreply_prefs"
        private const val KEY_SETTINGS = "key_app_settings"
        private const val KEY_CONTACTS = "key_authorized_contacts"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
        }
    }
}
