package com.livan.ai.autoreply.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput

object NotificationResponder {

    fun sendReply(
        context: Context,
        action: NotificationCompat.Action,
        replyText: String
    ): Boolean {
        val remoteInputs = action.remoteInputs ?: return false
        val replyRemoteInput = remoteInputs.firstOrNull { it.allowFreeFormInput } ?: return false

        val intent = Intent()
        val bundle = Bundle()
        bundle.putCharSequence(replyRemoteInput.resultKey, replyText)
        RemoteInput.addResultsToIntent(arrayOf(replyRemoteInput), intent, bundle)

        val actionIntent = action.actionIntent ?: return false

        return try {
            actionIntent.send(context, 0, intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
