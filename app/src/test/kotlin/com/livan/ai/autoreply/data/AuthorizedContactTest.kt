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

    @Test
    fun testHierarchicalPromptContainsIvanIdentityAndScienceInterests() {
        val settings = AppSettings()
        val contact = AuthorizedContact(
            name = "Carlos",
            personality = PersonalityPreset.NATURAL.name
        )
        val prompt = contact.buildHierarchicalPrompt(settings)
        assertTrue(prompt.contains("Eres Iván"))
        assertTrue(prompt.contains("ciencia"))
        assertTrue(prompt.contains("economía"))
        assertTrue(prompt.contains("REGLA DE PREVALENCIA"))
    }

    @Test
    fun testHierarchicalPromptForDevoraIncludesIndifferenceAndDistance() {
        val settings = AppSettings()
        val devora = AuthorizedContact(
            name = "Devora",
            personality = PersonalityPreset.NATURAL.name
        )
        val prompt = devora.buildHierarchicalPrompt(settings)
        assertTrue(prompt.contains("Eres Iván"))
        assertTrue(prompt.contains("Devora te dijo que no era tu amiga"))
        assertTrue(prompt.contains("INDIFERENTE Y DISTANTE"))
        assertTrue(prompt.contains("interés por recuperar la amistad", ignoreCase = true))
    }

    @Test
    fun testHierarchicalPromptForSecoFilter() {
        val settings = AppSettings()
        val contact = AuthorizedContact(
            name = "Pedro",
            personality = PersonalityPreset.SECA.name
        )
        val prompt = contact.buildHierarchicalPrompt(settings)
        assertTrue(prompt.contains("FILTRO INDIVIDUAL ACTIVO: SECO Y MINIMALISTA"))
        assertTrue(prompt.contains("monosílabos"))
    }
}
