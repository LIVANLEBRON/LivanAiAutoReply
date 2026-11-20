package com.livan.ai.autoreply.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.livan.ai.autoreply.domain.ReplyEngine
import com.livan.ai.autoreply.notifications.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LivanNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var replyEngine: ReplyEngine

    override fun onCreate() {
        super.onCreate()
        replyEngine = ReplyEngine(applicationContext)
        Log.d(TAG, "LivanNotificationListenerService creado correctamente.")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "LivanNotificationListenerService destruido.")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "NotificationListener conectado con éxito por Android.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "NotificationListener desconectado.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Check if it is a WhatsApp notification
        if (!NotificationParser.isWhatsAppNotification(sbn)) {
            return
        }

        val parsed = NotificationParser.parse(applicationContext, sbn) ?: return

        serviceScope.launch {
            try {
                replyEngine.processNotification(parsed)
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando notificación de ${parsed.sender}: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "LivanNotifService"
    }
}
