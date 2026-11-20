package com.livan.ai.autoreply.domain

import android.content.Context
import com.livan.ai.autoreply.data.AppSettings
import com.livan.ai.autoreply.data.HistoryItem
import com.livan.ai.autoreply.data.HistoryRepository
import com.livan.ai.autoreply.data.PreferencesManager
import com.livan.ai.autoreply.data.ReplyStatus
import com.livan.ai.autoreply.networking.OllamaClient
import com.livan.ai.autoreply.notifications.NotificationResponder
import com.livan.ai.autoreply.notifications.ParsedNotification
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.LinkedList

class ReplyEngine(
    private val context: Context,
    private val preferencesManager: PreferencesManager = PreferencesManager.getInstance(context),
    private val historyRepository: HistoryRepository = HistoryRepository.getInstance(context),
    private val ollamaClient: OllamaClient = OllamaClient()
) {
    private val mutex = Mutex()

    // Deduplication and Anti-Loop caches
    private val processedKeys = Collections.synchronizedMap(LinkedHashMap<String, Long>())
    private val recentlySentTexts = LinkedList<String>()
    private val contactLastReplyTime = HashMap<String, Long>()
    private val globalReplyTimestamps = LinkedList<Long>()
    private val contactConversations = java.util.concurrent.ConcurrentHashMap<String, MutableList<Pair<String, String>>>()

    suspend fun processNotification(parsed: ParsedNotification): Boolean {
        val settings = preferencesManager.settings.value

        // 1. Master Switch check
        if (!settings.isMasterEnabled) {
            return false
        }

        // 2. RemoteInput Action presence
        val replyAction = parsed.replyAction ?: return false

        // 3. Notification Age check (ignore older than 2 minutes)
        val now = System.currentTimeMillis()
        if (now - parsed.sbn.postTime > 120_000L) {
            return false
        }

        // 4. Authorized Contact check
        val contact = preferencesManager.findAuthorizedContact(parsed.sender) ?: return false
        if (!contact.isEnabled) {
            return false
        }

        // 5. Group chat check
        if (parsed.isGroup && (settings.ignoreGroupsGlobally && !contact.allowInGroups)) {
            historyRepository.addEvent(
                HistoryItem(
                    sender = parsed.sender,
                    incomingMessage = parsed.messageText,
                    status = ReplyStatus.IGNORED_GROUP.name,
                    errorMessage = "Mensaje de grupo descartado por configuración"
                )
            )
            return false
        }

        mutex.withLock {
            // 6. Deduplication check
            if (processedKeys.containsKey(parsed.deduplicationKey)) {
                return false
            }
            processedKeys[parsed.deduplicationKey] = now
            if (processedKeys.size > 200) {
                val oldest = processedKeys.keys.firstOrNull()
                if (oldest != null) processedKeys.remove(oldest)
            }

            // 7. Anti-Loop Protection
            // Check if the incoming message is identical to one we just sent
            val isEcho = recentlySentTexts.any { sent ->
                sent.equals(parsed.messageText.trim(), ignoreCase = true) ||
                parsed.messageText.contains(sent.take(40), ignoreCase = true)
            }
            if (isEcho) {
                return false
            }

            // 8. Rate Limiting: per-contact cooldown
            val lastContactReply = contactLastReplyTime[contact.id] ?: 0L
            val cooldownMs = settings.cooldownSecondsPerContact * 1000L
            if (now - lastContactReply < cooldownMs) {
                historyRepository.addEvent(
                    HistoryItem(
                        sender = parsed.sender,
                        incomingMessage = parsed.messageText,
                        status = ReplyStatus.RATE_LIMITED.name,
                        errorMessage = "Cooldown activo para ${parsed.sender} (${settings.cooldownSecondsPerContact}s)"
                    )
                )
                return false
            }

            // 9. Rate Limiting: global replies per minute
            cleanOldTimestamps(now)
            if (globalReplyTimestamps.size >= settings.maxRepliesPerMinute) {
                historyRepository.addEvent(
                    HistoryItem(
                        sender = parsed.sender,
                        incomingMessage = parsed.messageText,
                        status = ReplyStatus.RATE_LIMITED.name,
                        errorMessage = "Límite global alcanzado (${settings.maxRepliesPerMinute}/min)"
                    )
                )
                return false
            }
        }

        // 10. Generate Reply with Ollama
        val systemPrompt = contact.buildHierarchicalPrompt(settings)
        val historyList = contactConversations.getOrPut(contact.id) { mutableListOf() }
        val recentHistory = synchronized(historyList) {
            historyList.takeLast(settings.maxContextMessages).toList()
        }

        val startTime = System.currentTimeMillis()
        val result = ollamaClient.generateChatReply(
            settings = settings,
            systemPrompt = systemPrompt,
            userMessage = parsed.messageText,
            conversationHistory = recentHistory
        )

        val latency = System.currentTimeMillis() - startTime

        if (result.isFailure) {
            val error = result.exceptionOrNull()
            historyRepository.addEvent(
                HistoryItem(
                    sender = parsed.sender,
                    incomingMessage = parsed.messageText,
                    status = ReplyStatus.ERROR_OLLAMA.name,
                    errorMessage = error?.localizedMessage ?: "Error desconocido al consultar Ollama",
                    latencyMs = latency,
                    modelUsed = settings.selectedModel
                )
            )
            return false
        }

        val rawReply = result.getOrNull()?.trim()
        if (rawReply.isNullOrBlank()) {
            historyRepository.addEvent(
                HistoryItem(
                    sender = parsed.sender,
                    incomingMessage = parsed.messageText,
                    status = ReplyStatus.ERROR_OLLAMA.name,
                    errorMessage = "Ollama devolvió una respuesta vacía",
                    latencyMs = latency,
                    modelUsed = settings.selectedModel
                )
            )
            return false
        }

        // Apply optional prefix
        val finalReply = if (settings.replyPrefix.isNotBlank()) {
            "${settings.replyPrefix.trim()} $rawReply"
        } else {
            rawReply
        }

        // Check if manual confirmation is required
        if (settings.requireManualConfirmation) {
            historyRepository.addEvent(
                HistoryItem(
                    sender = parsed.sender,
                    incomingMessage = parsed.messageText,
                    replyMessage = finalReply,
                    status = ReplyStatus.PENDING_CONFIRMATION.name,
                    latencyMs = latency,
                    modelUsed = settings.selectedModel
                )
            )
            return true
        }

        // 11. Send Reply via Notification RemoteInput
        val sendSuccess = NotificationResponder.sendReply(context, replyAction, finalReply)

        if (sendSuccess) {
            mutex.withLock {
                val sentTime = System.currentTimeMillis()
                contactLastReplyTime[contact.id] = sentTime
                globalReplyTimestamps.add(sentTime)

                // Add to anti-loop cache
                recentlySentTexts.add(finalReply)
                if (recentlySentTexts.size > 50) {
                    recentlySentTexts.removeFirst()
                }

                // Add to multi-turn conversation memory
                synchronized(historyList) {
                    historyList.add("user" to parsed.messageText)
                    historyList.add("assistant" to finalReply)
                    if (historyList.size > 20) {
                        historyList.subList(0, historyList.size - 20).clear()
                    }
                }
            }

            historyRepository.addEvent(
                HistoryItem(
                    sender = parsed.sender,
                    incomingMessage = parsed.messageText,
                    replyMessage = finalReply,
                    status = ReplyStatus.SENT.name,
                    latencyMs = latency,
                    modelUsed = settings.selectedModel
                )
            )
            return true
        } else {
            historyRepository.addEvent(
                HistoryItem(
                    sender = parsed.sender,
                    incomingMessage = parsed.messageText,
                    replyMessage = finalReply,
                    status = ReplyStatus.ERROR_NETWORK.name,
                    errorMessage = "Fallo al enviar RemoteInput a WhatsApp",
                    latencyMs = latency,
                    modelUsed = settings.selectedModel
                )
            )
            return false
        }
    }

    private fun cleanOldTimestamps(now: Long) {
        val oneMinuteAgo = now - 60_000L
        globalReplyTimestamps.removeAll { it < oneMinuteAgo }
    }
}
