package com.livan.ai.autoreply.notifications

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput

data class ParsedNotification(
    val sbn: StatusBarNotification,
    val packageName: String,
    val sender: String,
    val messageText: String,
    val isGroup: Boolean,
    val replyAction: NotificationCompat.Action?,
    val deduplicationKey: String
)

object NotificationParser {

    private val WHATSAPP_PACKAGES = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b"
    )

    fun isWhatsAppNotification(sbn: StatusBarNotification): Boolean {
        return WHATSAPP_PACKAGES.contains(sbn.packageName)
    }

    fun parse(context: Context, sbn: StatusBarNotification): ParsedNotification? {
        val notification = sbn.notification ?: return null

        // 1. Skip ongoing, group summary or non-content notifications
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return null
        }
        if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) {
            return null
        }

        val extras: Bundle = notification.extras ?: return null

        // 2. Extract Sender
        var sender = extras.getString(NotificationCompat.EXTRA_CONVERSATION_TITLE)
        if (sender.isNullOrBlank()) {
            val titleSeq = extras.getCharSequence(Notification.EXTRA_TITLE)
            sender = titleSeq?.toString()?.trim()
        }
        if (sender.isNullOrBlank()) {
            return null
        }

        // 3. Extract Message Content
        var message: String? = null
        val bigText = extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT)
        if (!bigText.isNullOrBlank()) {
            message = bigText.toString().trim()
        } else {
            val text = extras.getCharSequence(NotificationCompat.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                message = text.toString().trim()
            }
        }

        // If still empty or contains system placeholders
        if (message.isNullOrBlank()) {
            return null
        }

        // 4. Group Conversation Detection
        val messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        val isGroup = messagingStyle?.isGroupConversation == true ||
                extras.getBoolean(NotificationCompat.EXTRA_IS_GROUP_CONVERSATION) ||
                (extras.getCharSequence(NotificationCompat.EXTRA_CONVERSATION_TITLE) != null &&
                        extras.getCharSequence(Notification.EXTRA_TITLE) != null &&
                        extras.getCharSequence(NotificationCompat.EXTRA_CONVERSATION_TITLE) != extras.getCharSequence(Notification.EXTRA_TITLE))

        // If group message, WhatsApp often puts "Sender Name: Message content" in EXTRA_TEXT
        var actualSender = sender
        var actualMessage = message
        if (isGroup && message.contains(": ")) {
            val split = message.split(": ", limit = 2)
            if (split.size == 2 && split[0].isNotBlank()) {
                actualSender = split[0].trim()
                actualMessage = split[1].trim()
            }
        }

        // Filter out empty messages after cleanup
        if (actualMessage.isBlank()) {
            return null
        }

        // 5. Find RemoteInput Reply Action
        val replyAction = findReplyAction(notification)

        // 6. Generate Deduplication Key
        // Based on sender, message content and notification post time
        val deduplicationKey = "${sbn.packageName}_${actualSender}_${actualMessage.hashCode()}"

        return ParsedNotification(
            sbn = sbn,
            packageName = sbn.packageName,
            sender = actualSender,
            messageText = actualMessage,
            isGroup = isGroup,
            replyAction = replyAction,
            deduplicationKey = deduplicationKey
        )
    }

    private fun findReplyAction(notification: Notification): NotificationCompat.Action? {
        val wearExtender = NotificationCompat.WearableExtender(notification)
        for (act in wearExtender.actions) {
            if (hasFreeFormRemoteInput(act)) {
                return act
            }
        }

        val actionCount = NotificationCompat.getActionCount(notification)
        for (i in 0 until actionCount) {
            val action = NotificationCompat.getAction(notification, i)
            if (action != null && hasFreeFormRemoteInput(action)) {
                return action
            }
        }
        return null
    }

    private fun hasFreeFormRemoteInput(action: NotificationCompat.Action): Boolean {
        val remoteInputs = action.remoteInputs ?: return false
        return remoteInputs.any { it.allowFreeFormInput }
    }
}
