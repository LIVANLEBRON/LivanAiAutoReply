package com.livan.ai.autoreply.presentation

import com.livan.ai.autoreply.data.AppSettings
import org.junit.Assert.*
import org.junit.Test

class MainViewModelInitializationTest {

    @Test
    fun testSettingsAndPresetsInitialization() {
        val settings = AppSettings()
        assertNotNull(settings)
        assertEquals("llama3.2", settings.selectedModel)
        assertFalse(settings.isMasterEnabled)
    }
}
