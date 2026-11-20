package com.livan.ai.autoreply.data

import org.junit.Assert.*
import org.junit.Test

class AppSettingsTest {

    @Test
    fun testUrlBuildingWithTailscaleIpAndPort() {
        val settings = AppSettings(
            serverHost = "100.85.12.34",
            serverPort = 3000,
            apiEndpoint = "/api/chat"
        )

        assertEquals("http://100.85.12.34:3000/api/chat", settings.buildFullUrl())
        assertEquals("http://100.85.12.34:3000", settings.buildBaseUrl())
    }

    @Test
    fun testUrlBuildingWithHttpPrefixAndSlashEndpoint() {
        val settings = AppSettings(
            serverHost = "http://100.85.12.34/",
            serverPort = 11434,
            apiEndpoint = "api/chat"
        )

        assertEquals("http://100.85.12.34:11434/api/chat", settings.buildFullUrl())
        assertEquals("http://100.85.12.34:11434", settings.buildBaseUrl())
    }
}
