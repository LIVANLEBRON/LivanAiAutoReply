package com.livan.ai.autoreply.networking

import com.livan.ai.autoreply.data.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class OllamaTestResult(
    val isSuccess: Boolean,
    val latencyMs: Long,
    val models: List<String> = emptyList(),
    val errorMessage: String? = null
)

class OllamaClient {

    private fun buildHttpClient(timeoutSeconds: Int): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    suspend fun generateChatReply(
        settings: AppSettings,
        systemPrompt: String,
        userMessage: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val client = buildHttpClient(settings.timeoutSeconds)
        val url = settings.buildFullUrl()

        try {
            val rootJson = JSONObject().apply {
                put("model", settings.selectedModel)
                put("stream", false)

                val options = JSONObject().apply {
                    put("temperature", settings.temperature.toDouble())
                }
                put("options", options)

                val messagesArray = JSONArray()

                // System prompt
                if (systemPrompt.isNotBlank()) {
                    messagesArray.put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt.trim())
                    })
                }

                // Any recent conversation context
                conversationHistory.takeLast(settings.maxContextMessages).forEach { (role, content) ->
                    messagesArray.put(JSONObject().apply {
                        put("role", role)
                        put("content", content)
                    })
                }

                // Incoming user message
                messagesArray.put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage.trim())
                })

                put("messages", messagesArray)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("User-Agent", "LivanAIAutoReply/1.0 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string().orEmpty()
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}. $errorBody")
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(IOException("Respuesta vacía del servidor Ollama"))

                val responseJson = JSONObject(responseBody)

                // Standard Ollama chat response: { "message": { "role": "assistant", "content": "..." } }
                if (responseJson.has("message")) {
                    val messageObj = responseJson.getJSONObject("message")
                    val content = messageObj.optString("content", "").trim()
                    if (content.isNotBlank()) {
                        return@withContext Result.success(content)
                    }
                }

                // Fallback for /api/generate format: { "response": "..." }
                if (responseJson.has("response")) {
                    val content = responseJson.optString("response", "").trim()
                    if (content.isNotBlank()) {
                        return@withContext Result.success(content)
                    }
                }

                Result.failure(IOException("No se pudo extraer 'message.content' de la respuesta JSON"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnectionAndFetchModels(settings: AppSettings): OllamaTestResult = withContext(Dispatchers.IO) {
        val client = buildHttpClient(settings.timeoutSeconds.coerceAtMost(10))
        val baseUrl = settings.buildBaseUrl()
        val tagsUrl = "$baseUrl/api/tags"

        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(tagsUrl)
                .get()
                .header("User-Agent", "LivanAIAutoReply/1.0 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                if (!response.isSuccessful) {
                    return@withContext OllamaTestResult(
                        isSuccess = false,
                        latencyMs = latency,
                        errorMessage = "Error HTTP ${response.code}: ${response.message}"
                    )
                }

                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val modelsList = mutableListOf<String>()

                if (json.has("models")) {
                    val modelsArray = json.getJSONArray("models")
                    for (i in 0 until modelsArray.length()) {
                        val modelObj = modelsArray.getJSONObject(i)
                        val name = modelObj.optString("name", "")
                        if (name.isNotBlank()) {
                            modelsList.add(name)
                        }
                    }
                }

                OllamaTestResult(
                    isSuccess = true,
                    latencyMs = latency,
                    models = modelsList
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val readableMsg = when {
                e.message?.contains("Failed to connect", ignoreCase = true) == true ->
                    "No se pudo conectar a $baseUrl. Revisa la IP, el puerto y que Tailscale esté activo."
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Tiempo de espera agotado al conectar con $baseUrl."
                else -> e.localizedMessage ?: e.javaClass.simpleName
            }
            OllamaTestResult(
                isSuccess = false,
                latencyMs = latency,
                errorMessage = readableMsg
            )
        }
    }
}
