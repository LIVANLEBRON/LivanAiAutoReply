package com.livan.ai.autoreply.data

import org.junit.Assert.*
import org.junit.Test

class PersonalityPresetTest {

    @Test
    fun testAllRequiredPresetsExist() {
        val presets = PersonalityPreset.entries.map { it.title }
        assertTrue(presets.contains("Seca"))
        assertTrue(presets.contains("Indiferente"))
        assertTrue(presets.contains("Sarcástica"))
        assertTrue(presets.contains("Cortante"))
        assertTrue(presets.contains("Natural"))
        assertTrue(presets.contains("Personalizada"))
    }

    @Test
    fun testDefaultPromptsAreNotEmpty() {
        PersonalityPreset.entries.forEach { preset ->
            assertTrue(preset.defaultPrompt.isNotBlank())
            assertTrue(preset.description.isNotBlank())
        }
    }

    @Test
    fun testFromNameFallback() {
        assertEquals(PersonalityPreset.SECA, PersonalityPreset.fromName("SECA"))
        assertEquals(PersonalityPreset.NATURAL, PersonalityPreset.fromName("non_existent"))
    }
}
