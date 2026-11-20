package com.livan.ai.autoreply.presentation.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livan.ai.autoreply.data.AppSettings
import com.livan.ai.autoreply.data.AuthorizedContact
import com.livan.ai.autoreply.data.HistoryItem
import com.livan.ai.autoreply.data.ReplyStatus
import com.livan.ai.autoreply.networking.OllamaTestResult

@Composable
fun DashboardTab(
    settings: AppSettings,
    contacts: List<AuthorizedContact>,
    history: List<HistoryItem>,
    isPermissionGranted: Boolean,
    ollamaStatus: OllamaTestResult?,
    isTestingOllama: Boolean,
    onToggleMaster: (Boolean) -> Unit,
    onTestOllama: () -> Unit,
    onNavigateToServer: () -> Unit,
    onNavigateToContacts: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val activeContactsCount = contacts.count { it.isEnabled }
    val totalProcessed = history.size
    val totalSent = history.count { it.status == ReplyStatus.SENT.name }
    val totalErrors = history.count {
        it.status == ReplyStatus.ERROR_OLLAMA.name || it.status == ReplyStatus.ERROR_NETWORK.name
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Permission Warning Banner if not granted
        if (!isPermissionGranted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Permiso de Notificaciones Requerido",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Para detectar y responder mensajes de WhatsApp, concede acceso a las notificaciones.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Conceder Permiso")
                        }
                    }
                }
            }
        }

        // 2. Master Switch Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (settings.isMasterEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (settings.isMasterEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                                )
                        )
                        Text(
                            text = if (settings.isMasterEnabled) "BOT ACTIVO" else "BOT EN PAUSA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (settings.isMasterEnabled)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (settings.isMasterEnabled)
                            "Respondiendo automáticamente a contactos autorizados."
                        else
                            "Respuestas automáticas detenidas.",
                        fontSize = 13.sp,
                        color = if (settings.isMasterEnabled)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                Switch(
                    checked = settings.isMasterEnabled,
                    onCheckedChange = onToggleMaster
                )
            }
        }

        // 3. Ollama Server Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Servidor",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Servidor Ollama (Tailscale)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    if (isTestingOllama) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = onTestOllama) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Probar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dirección: ${settings.serverHost}:${settings.serverPort}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Modelo: ${settings.selectedModel}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status pill
                val isOnline = ollamaStatus?.isSuccess == true
                Surface(
                    color = when {
                        ollamaStatus == null -> MaterialTheme.colorScheme.surfaceVariant
                        isOnline -> Color(0xFFE8F5E9)
                        else -> Color(0xFFFFEBEE)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        ollamaStatus == null -> Color.Gray
                                        isOnline -> Color(0xFF2E7D32)
                                        else -> Color(0xFFC62828)
                                    }
                                )
                        )
                        Text(
                            text = when {
                                ollamaStatus == null -> "Estado no verificado (Toca probar)"
                                isOnline -> "En línea (${ollamaStatus.latencyMs} ms)"
                                else -> "Desconectado: ${ollamaStatus.errorMessage ?: "Error"}"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                ollamaStatus == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                isOnline -> Color(0xFF1B5E20)
                                else -> Color(0xFFB71C1C)
                            }
                        )
                    }
                }
            }
        }

        // 4. Metrics Grid
        Text(
            text = "Métricas de Actividad",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Vigilados",
                value = "$activeContactsCount",
                subtitle = "de ${contacts.size} contactos",
                icon = Icons.Default.People,
                onClick = onNavigateToContacts
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Enviados",
                value = "$totalSent",
                subtitle = "respuestas IA",
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = {}
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Procesados",
                value = "$totalProcessed",
                subtitle = "mensajes totales",
                icon = Icons.Default.ChatBubble,
                onClick = {}
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Errores",
                value = "$totalErrors",
                subtitle = "fallos registrados",
                icon = Icons.Default.ErrorOutline,
                onClick = {}
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
