package com.livan.ai.autoreply.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AuthorizedContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isEnabled: Boolean = true,
    val personality: String = PersonalityPreset.NATURAL.name,
    val customPrompt: String? = null,
    val allowInGroups: Boolean = false
) {
    fun getPersonalityPreset(): PersonalityPreset = PersonalityPreset.fromName(personality)

    fun effectivePrompt(globalDefaultPrompt: String): String {
        return if (!customPrompt.isNullOrBlank()) {
            customPrompt
        } else {
            getPersonalityPreset().defaultPrompt
        }
    }
}
