package com.livan.ai.autoreply.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livan.ai.autoreply.data.HistoryItem
import com.livan.ai.autoreply.data.ReplyStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryTab(
    history: List<HistoryItem>,
    onClearHistory: () -> Unit
) {
    var filterType by remember { mutableStateOf("TODOS") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredHistory = remember(history, filterType) {
        when (filterType) {
            "ENVIADOS" -> history.filter { it.status == ReplyStatus.SENT.name }
            "ERRORES" -> history.filter {
                it.status == ReplyStatus.ERROR_OLLAMA.name ||
                it.status == ReplyStatus.ERROR_NETWORK.name ||
                it.status == ReplyStatus.RATE_LIMITED.name
            }
            "IGNORADOS" -> history.filter {
                it.status == ReplyStatus.IGNORED_FILTER.name ||
                it.status == ReplyStatus.IGNORED_GROUP.name
            }
            else -> history
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historial de Actividad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${history.size} eventos registrados",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (history.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpiar Historial",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = filterType == "TODOS",
                onClick = { filterType = "TODOS" },
                label = { Text("Todos (${history.size})", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterType == "ENVIADOS",
                onClick = { filterType = "ENVIADOS" },
                label = { Text("Enviados", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterType == "ERRORES",
                onClick = { filterType = "ERRORES" },
                label = { Text("Errores", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterType == "IGNORADOS",
                onClick = { filterType = "IGNORADOS" },
                label = { Text("Filtros", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay registros en esta categoría",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredHistory, key = { it.id }) { item ->
                    HistoryItemCard(item)
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("¿Limpiar todo el historial?") },
            text = { Text("Esta acción eliminará todos los registros de mensajes y respuestas guardados.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Limpiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(item: HistoryItem) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss - dd/MM", Locale.getDefault()) }
    val formattedTime = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }
    val status = item.getReplyStatus()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.sender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Incoming message
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📥 \"${item.incomingMessage}\"",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Reply or error message
            if (!item.replyMessage.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🤖 \"${item.replyMessage}\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (!item.errorMessage.isNullOrBlank()) {
                Text(
                    text = "⚠️ ${item.errorMessage}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (status) {
                        ReplyStatus.SENT -> Color(0xFFE8F5E9)
                        ReplyStatus.RATE_LIMITED, ReplyStatus.IGNORED_GROUP, ReplyStatus.IGNORED_FILTER -> Color(0xFFFFF3E0)
                        else -> Color(0xFFFFEBEE)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (status) {
                            ReplyStatus.SENT -> Color(0xFF2E7D32)
                            ReplyStatus.RATE_LIMITED, ReplyStatus.IGNORED_GROUP, ReplyStatus.IGNORED_FILTER -> Color(0xFFE65100)
                            else -> Color(0xFFC62828)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (item.latencyMs > 0) {
                    Text(
                        text = "${item.latencyMs} ms · ${item.modelUsed ?: ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
