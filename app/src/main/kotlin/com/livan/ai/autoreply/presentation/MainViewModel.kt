package com.livan.ai.autoreply.presentation

import android.app.Application
import android.content.ComponentName
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.livan.ai.autoreply.data.AppSettings
import com.livan.ai.autoreply.data.AuthorizedContact
import com.livan.ai.autoreply.data.HistoryItem
import com.livan.ai.autoreply.data.HistoryRepository
import com.livan.ai.autoreply.data.PersonalityPreset
import com.livan.ai.autoreply.data.PreferencesManager
import com.livan.ai.autoreply.networking.OllamaClient
import com.livan.ai.autoreply.networking.OllamaTestResult
import com.livan.ai.autoreply.services.LivanNotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager.getInstance(application)
    private val historyRepository = HistoryRepository.getInstance(application)
    private val ollamaClient = OllamaClient()

    val settings: StateFlow<AppSettings> = preferencesManager.settings
    val contacts: StateFlow<List<AuthorizedContact>> = preferencesManager.contacts
    val history: StateFlow<List<HistoryItem>> = historyRepository.history

    private val _isNotificationPermissionGranted = MutableStateFlow(false)
    val isNotificationPermissionGranted: StateFlow<Boolean> = _isNotificationPermissionGranted.asStateFlow()

    private val _ollamaStatus = MutableStateFlow<OllamaTestResult?>(null)
    val ollamaStatus: StateFlow<OllamaTestResult?> = _ollamaStatus.asStateFlow()

    private val _isTestingOllama = MutableStateFlow(false)
    val isTestingOllama: StateFlow<Boolean> = _isTestingOllama.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _simulationReply = MutableStateFlow<String?>(null)
    val simulationReply: StateFlow<String?> = _simulationReply.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    init {
        checkNotificationPermission()
        testOllamaConnection()
    }

    fun checkNotificationPermission(): Boolean {
        val context = getApplication<Application>()
        val isGranted = try {
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        } catch (_: Exception) {
            try {
                val cn = ComponentName(context, LivanNotificationListenerService::class.java)
                val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                flat != null && flat.contains(cn.flattenToString())
            } catch (_: Exception) {
                false
            }
        }
        _isNotificationPermissionGranted.value = isGranted
        return isGranted
    }

    fun toggleMasterSwitch(enabled: Boolean) {
        preferencesManager.setMasterEnabled(enabled)
    }

    fun updateSettings(newSettings: AppSettings) {
        preferencesManager.updateSettings(newSettings)
    }

    fun addContact(
        name: String,
        personality: PersonalityPreset,
        allowInGroups: Boolean,
        customPrompt: String? = null,
        relationshipContext: String? = null
    ) {
        val cleanName = name.trim()
        val isDevora = cleanName.contains("devora", ignoreCase = true) || cleanName.contains("debora", ignoreCase = true)
        val finalContext = when {
            !relationshipContext.isNullOrBlank() -> relationshipContext.trim()
            isDevora -> com.livan.ai.autoreply.data.DEVORA_CONTEXT
            else -> null
        }
        val finalPersonality = if (isDevora && personality == PersonalityPreset.NATURAL) {
            PersonalityPreset.INDIFERENTE.name
        } else {
            personality.name
        }

        val contact = AuthorizedContact(
            name = cleanName,
            isEnabled = true,
            personality = finalPersonality,
            customPrompt = customPrompt?.trim()?.ifBlank { null },
            relationshipContext = finalContext,
            allowInGroups = allowInGroups
        )
        preferencesManager.addContact(contact)
    }

    fun updateContact(contact: AuthorizedContact) {
        preferencesManager.updateContact(contact)
    }

    fun deleteContact(contactId: String) {
        preferencesManager.deleteContact(contactId)
    }

    fun toggleContact(contactId: String, enabled: Boolean) {
        preferencesManager.toggleContactEnabled(contactId, enabled)
    }

    fun testOllamaConnection() {
        viewModelScope.launch {
            _isTestingOllama.value = true
            val result = ollamaClient.testConnectionAndFetchModels(settings.value)
            _ollamaStatus.value = result
            if (result.isSuccess) {
                if (result.resolvedHost != null && result.resolvedHost != settings.value.serverHost) {
                    updateSettings(settings.value.copy(serverHost = result.resolvedHost))
                }
                if (result.models.isNotEmpty()) {
                    _availableModels.value = result.models
                    if (settings.value.selectedModel.isBlank() || !result.models.contains(settings.value.selectedModel)) {
                        val targetModel = if (result.models.contains("lfm3:latest")) "lfm3:latest" else result.models.first()
                        updateSettings(settings.value.copy(selectedModel = targetModel))
                    }
                }
            }
            _isTestingOllama.value = false
        }
    }

    fun autoConnectIfDisconnected() {
        if (_ollamaStatus.value == null || !_ollamaStatus.value!!.isSuccess) {
            testOllamaConnection()
        }
    }

    fun simulateChat(testMessage: String, personality: PersonalityPreset, customPrompt: String, temperature: Float) {
        if (testMessage.isBlank()) return
        viewModelScope.launch {
            _isSimulating.value = true
            _simulationReply.value = null

            val dummyContact = AuthorizedContact(
                name = "Contacto de Prueba",
                personality = personality.name,
                customPrompt = customPrompt.ifBlank { null }
            )
            val effectivePrompt = dummyContact.buildHierarchicalPrompt(settings.value)

            val tempSettings = settings.value.copy(temperature = temperature)
            val result = ollamaClient.generateChatReply(
                settings = tempSettings,
                systemPrompt = effectivePrompt,
                userMessage = testMessage
            )

            if (result.isSuccess) {
                _simulationReply.value = result.getOrNull()
            } else {
                _simulationReply.value = "Error: ${result.exceptionOrNull()?.localizedMessage ?: "Fallo al conectar"}"
            }
            _isSimulating.value = false
        }
    }

    fun clearHistory() {
        historyRepository.clearHistory()
    }
}
