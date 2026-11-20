package com.livan.ai.autoreply.presentation.screens

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livan.ai.autoreply.data.AppSettings

@Composable
fun SettingsTab(
    settings: AppSettings,
    isNotificationPermissionGranted: Boolean,
    onUpdateSettings: (AppSettings) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var repliesPerMinute by remember(settings.maxRepliesPerMinute) {
        mutableStateOf(settings.maxRepliesPerMinute.toString())
    }
    var cooldownSeconds by remember(settings.cooldownSecondsPerContact) {
        mutableStateOf(settings.cooldownSecondsPerContact.toString())
    }
    var prefix by remember(settings.replyPrefix) {
        mutableStateOf(settings.replyPrefix)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Control y Configuración General",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // 1. Android Permissions & Service Health
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permisos del Sistema Android",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                // Notification Access
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Acceso a Notificaciones", fontWeight = FontWeight.Medium)
                        Text(
                            text = if (isNotificationPermissionGranted) "Concedido (Operativo)" else "No concedido (Requerido)",
                            fontSize = 12.sp,
                            color = if (isNotificationPermissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            context.startActivity(intent)
                        }
                    ) {
                        Text(if (isNotificationPermissionGranted) "Configurar" else "Permitir")
                    }
                }

                Divider()

                // Battery Optimization Exemption
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Optimización de Batería", fontWeight = FontWeight.Medium)
                        Text(
                            text = "Evita que Android cierre el servicio en segundo plano.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    ) {
                        Text("Desactivar límite")
                    }
                }
            }
        }

        // 2. Safety & Anti-Spam Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Límites de Seguridad y Anti-Bucle",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                // Global Rate Limit
                OutlinedTextField(
                    value = repliesPerMinute,
                    onValueChange = {
                        repliesPerMinute = it
                        it.toIntOrNull()?.let { rpm ->
                            onUpdateSettings(settings.copy(maxRepliesPerMinute = rpm))
                        }
                    },
                    label = { Text("Máximo de respuestas por minuto (Global)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cooldown per contact
                OutlinedTextField(
                    value = cooldownSeconds,
                    onValueChange = {
                        cooldownSeconds = it
                        it.toIntOrNull()?.let { cd ->
                            onUpdateSettings(settings.copy(cooldownSecondsPerContact = cd))
                        }
                    },
                    label = { Text("Tiempo de espera mínimo por contacto (segundos)") },
                    supportingText = { Text("Evita responder múltiples veces seguidas a la misma persona.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Reply Prefix
                OutlinedTextField(
                    value = prefix,
                    onValueChange = {
                        prefix = it
                        onUpdateSettings(settings.copy(replyPrefix = it))
                    },
                    label = { Text("Prefijo opcional en la respuesta") },
                    placeholder = { Text("Ej: [Livan AI] o dejar vacío") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()

                // Ignore groups toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ignorar grupos globalmente", fontWeight = FontWeight.Medium)
                        Text(
                            text = "No responder a grupos salvo autorización específica por contacto.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.ignoreGroupsGlobally,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(ignoreGroupsGlobally = it))
                        }
                    )
                }

                Divider()

                // Manual confirmation toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Simulación / Confirmación", fontWeight = FontWeight.Medium)
                        Text(
                            text = "Genera la respuesta pero la deja en estado 'Pendiente' en el historial sin enviarla por WhatsApp.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.requireManualConfirmation,
                        onCheckedChange = {
                            onUpdateSettings(settings.copy(requireManualConfirmation = it))
                        }
                    )
                }
            }
        }

        // 3. Privacy & Tailscale Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Privacidad y Red Tailscale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "• Las comunicaciones viajan cifradas dentro de tu red privada Tailscale (100.x.x.x) directamente entre tu teléfono y tu PC con Ollama.\n" +
                            "• No se utilizan servidores intermediarios de terceros ni servicios en la nube.\n" +
                            "• No se almacenan credenciales ni tokens en el código fuente.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reset to Defaults Button
        OutlinedButton(
            onClick = {
                onUpdateSettings(AppSettings())
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Restore, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restablecer todos los ajustes por defecto")
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
