package com.livan.ai.autoreply.data

import kotlinx.serialization.Serializable
import java.util.UUID

enum class ReplyStatus(val displayName: String) {
    SENT("Enviado"),
    IGNORED_FILTER("Ignorado por filtro"),
    IGNORED_GROUP("Grupo no autorizado"),
    RATE_LIMITED("Límite de velocidad"),
    ERROR_NETWORK("Error de red"),
    ERROR_OLLAMA("Error de Ollama"),
    PENDING_CONFIRMATION("Pendiente de confirmación")
}

@Serializable
data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String,
    val incomingMessage: String,
    val replyMessage: String? = null,
    val status: String = ReplyStatus.SENT.name,
    val errorMessage: String? = null,
    val latencyMs: Long = 0L,
    val modelUsed: String? = null
) {
    fun getReplyStatus(): ReplyStatus {
        return try {
            ReplyStatus.valueOf(status)
        } catch (_: Exception) {
            ReplyStatus.SENT
        }
    }
}
