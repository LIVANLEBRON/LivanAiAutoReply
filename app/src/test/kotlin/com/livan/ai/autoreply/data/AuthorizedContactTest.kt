package com.livan.ai.autoreply.data

import org.junit.Assert.*
import org.junit.Test

class AuthorizedContactTest {

    @Test
    fun testEffectivePromptWithCustomPrompt() {
        val contact = AuthorizedContact(
            name = "Amigo",
            personality = PersonalityPreset.SECA.name,
            customPrompt = "Mi prompt personalizado para este amigo"
        )

        assertEquals("Mi prompt personalizado para este amigo", contact.effectivePrompt("Global prompt"))
    }

    @Test
    fun testEffectivePromptFallbackToPreset() {
        val contact = AuthorizedContact(
            name = "Mamá",
            personality = PersonalityPreset.NATURAL.name,
            customPrompt = null
        )

        assertEquals(PersonalityPreset.NATURAL.defaultPrompt, contact.effectivePrompt("Global prompt"))
    }
}
