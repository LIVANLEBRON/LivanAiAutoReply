package com.livan.ai.autoreply.presentation

import com.livan.ai.autoreply.data.AppSettings
import org.junit.Assert.*
import org.junit.Test

class MainViewModelInitializationTest {

    @Test
    fun testSettingsAndPresetsInitialization() {
        val settings = AppSettings()
        assertNotNull(settings)
        assertEquals("lfm3:latest", settings.selectedModel)
        assertTrue(settings.isMasterEnabled)
        assertEquals("10.0.0.29", settings.serverHost)
        assertEquals(11434, settings.serverPort)
    }
}
