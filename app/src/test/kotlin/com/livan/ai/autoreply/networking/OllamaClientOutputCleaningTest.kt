package com.livan.ai.autoreply.networking

import org.junit.Assert.assertEquals
import org.junit.Test

class OllamaClientOutputCleaningTest {

    @Test
    fun testStripThinkingTags() {
        val raw = "<think>The user said hello in Spanish</think>Klk dime a ver"
        assertEquals("Klk dime a ver", OllamaClient.cleanModelOutput(raw))
    }

    @Test
    fun testStripPrefixesAndQuotes() {
        val raw = "\"Livana: Ya voy saliendo para allá\""
        assertEquals("Ya voy saliendo para allá", OllamaClient.cleanModelOutput(raw))
    }

    @Test
    fun testStripCustomerServiceBoilerplate() {
        val raw = "Estoy en la casa. ¿En qué más te puedo ayudar hoy?"
        assertEquals("Estoy en la casa.", OllamaClient.cleanModelOutput(raw))
    }
}
