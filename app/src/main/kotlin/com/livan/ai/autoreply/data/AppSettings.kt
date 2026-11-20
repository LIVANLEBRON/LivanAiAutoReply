package com.livan.ai.autoreply.data

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val isMasterEnabled: Boolean = true,
    val serverHost: String = "100.89.243.4",
    val serverPort: Int = 11434,
    val apiEndpoint: String = "/api/chat",
    val selectedModel: String = "lfm3:latest",
    val timeoutSeconds: Int = 30,
    val temperature: Float = 0.7f,
    val defaultPersonality: String = PersonalityPreset.NATURAL.name,
    val customSystemPrompt: String = PersonalityPreset.NATURAL.defaultPrompt,
    val maxContextMessages: Int = 5,
    val maxRepliesPerMinute: Int = 5,
    val cooldownSecondsPerContact: Int = 10,
    val ignoreGroupsGlobally: Boolean = true,
    val requireManualConfirmation: Boolean = false,
    val replyPrefix: String = ""
) {
    fun getEffectiveSystemPrompt(): String {
        val preset = PersonalityPreset.fromName(defaultPersonality)
        return if (preset == PersonalityPreset.PERSONALIZADA && customSystemPrompt.isNotBlank()) {
            customSystemPrompt
        } else if (customSystemPrompt.isNotBlank()) {
            customSystemPrompt
        } else {
            preset.defaultPrompt
        }
    }

    fun buildFullUrl(): String {
        val cleanHost = serverHost.trim().removeSuffix("/")
        val formattedHost = if (!cleanHost.startsWith("http://") && !cleanHost.startsWith("https://")) {
            "http://$cleanHost"
        } else {
            cleanHost
        }
        val cleanEndpoint = if (apiEndpoint.startsWith("/")) apiEndpoint else "/$apiEndpoint"
        return "$formattedHost:$serverPort$cleanEndpoint"
    }

    fun buildBaseUrl(): String {
        val cleanHost = serverHost.trim().removeSuffix("/")
        val formattedHost = if (!cleanHost.startsWith("http://") && !cleanHost.startsWith("https://")) {
            "http://$cleanHost"
        } else {
            cleanHost
        }
        return "$formattedHost:$serverPort"
    }
}
