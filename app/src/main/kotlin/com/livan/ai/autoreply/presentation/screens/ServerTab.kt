package com.livan.ai.autoreply.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livan.ai.autoreply.data.AppSettings
import com.livan.ai.autoreply.networking.OllamaTestResult
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerTab(
    settings: AppSettings,
    ollamaStatus: OllamaTestResult?,
    isTestingOllama: Boolean,
    availableModels: List<String>,
    onUpdateSettings: (AppSettings) -> Unit,
    onTestOllama: () -> Unit
) {
    val scrollState = rememberScrollState()

    var host by remember(settings.serverHost) { mutableStateOf(settings.serverHost) }
    var port by remember(settings.serverPort) { mutableStateOf(settings.serverPort.toString()) }
    var endpoint by remember(settings.apiEndpoint) { mutableStateOf(settings.apiEndpoint) }
    var model by remember(settings.selectedModel) { mutableStateOf(settings.selectedModel) }
    var timeoutSeconds by remember(settings.timeoutSeconds) { mutableIntStateOf(settings.timeoutSeconds) }
    var maxContext by remember(settings.maxContextMessages) { mutableIntStateOf(settings.maxContextMessages) }

    var isModelDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configuración del Servidor Ollama",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Conecta la app a tu PC con Ollama a través de tu red privada Tailscale (ej: 100.x.x.x) o red local.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Connection Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    ollamaStatus == null -> MaterialTheme.colorScheme.surfaceVariant
                    ollamaStatus.isSuccess -> Color(0xFFE8F5E9)
                    else -> Color(0xFFFFEBEE)
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = when {
                            ollamaStatus == null -> Icons.Default.Share
                            ollamaStatus.isSuccess -> Icons.Default.CheckCircle
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when {
                            ollamaStatus == null -> MaterialTheme.colorScheme.primary
                            ollamaStatus.isSuccess -> Color(0xFF2E7D32)
                            else -> Color(0xFFC62828)
                        }
                    )
                    Column {
                        Text(
                            text = when {
                                ollamaStatus == null -> "Conexión no verificada"
                                ollamaStatus.isSuccess -> "Conexión Exitosa (${ollamaStatus.latencyMs} ms)"
                                else -> "Fallo de Conexión"
                            },
                            fontWeight = FontWeight.Bold,
                            color = when {
                                ollamaStatus == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                ollamaStatus.isSuccess -> Color(0xFF1B5E20)
                                else -> Color(0xFFB71C1C)
                            }
                        )
                        if (ollamaStatus != null && !ollamaStatus.isSuccess) {
                            Text(
                                text = ollamaStatus.errorMessage ?: "Error desconocido",
                                fontSize = 12.sp,
                                color = Color(0xFFC62828)
                            )
                        } else if (ollamaStatus?.isSuccess == true) {
                            Text(
                                text = "Modelos detectados: ${ollamaStatus.models.size}",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        // Host & Port Form
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
                OutlinedTextField(
                    value = host,
                    onValueChange = {
                        host = it
                        onUpdateSettings(settings.copy(serverHost = it))
                    },
                    label = { Text("IP / Host del Servidor (Tailscale o LAN)") },
                    placeholder = { Text("100.64.0.1 o mi-pc.tailscale.net") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = {
                            port = it
                            val intPort = it.toIntOrNull() ?: settings.serverPort
                            onUpdateSettings(settings.copy(serverPort = intPort))
                        },
                        label = { Text("Puerto") },
                        placeholder = { Text("3000 o 11434") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = {
                            endpoint = it
                            onUpdateSettings(settings.copy(apiEndpoint = it))
                        },
                        label = { Text("Endpoint") },
                        placeholder = { Text("/api/chat") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Text(
                    text = "URL Completa: ${settings.buildFullUrl()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Model Selection Card
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
                Text(
                    text = "Modelo de IA",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                ExposedDropdownMenuBox(
                    expanded = isModelDropdownExpanded,
                    onExpandedChange = { isModelDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = {
                            model = it
                            onUpdateSettings(settings.copy(selectedModel = it))
                        },
                        label = { Text("Nombre del Modelo") },
                        placeholder = { Text("llama3.2, mistral, gemma2...") },
                        trailingIcon = {
                            if (availableModels.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    if (availableModels.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = isModelDropdownExpanded,
                            onDismissRequest = { isModelDropdownExpanded = false }
                        ) {
                            availableModels.forEach { modelName ->
                                DropdownMenuItem(
                                    text = { Text(modelName) },
                                    onClick = {
                                        model = modelName
                                        onUpdateSettings(settings.copy(selectedModel = modelName))
                                        isModelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Si ya tienes modelos descargados en tu Ollama, pulsa el botón inferior para consultar /api/tags y listarlos aquí.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Advanced Options: Timeout & Context
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
                // Timeout
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tiempo de espera (Timeout)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("${timeoutSeconds}s", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = timeoutSeconds.toFloat(),
                        onValueChange = {
                            timeoutSeconds = it.roundToInt()
                            onUpdateSettings(settings.copy(timeoutSeconds = timeoutSeconds))
                        },
                        valueRange = 5f..60f,
                        steps = 10
                    )
                }

                // Max Context Messages
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mensajes de contexto previo", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("$maxContext", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = maxContext.toFloat(),
                        onValueChange = {
                            maxContext = it.roundToInt()
                            onUpdateSettings(settings.copy(maxContextMessages = maxContext))
                        },
                        valueRange = 1f..15f,
                        steps = 13
                    )
                }
            }
        }

        // Test & Fetch Button
        Button(
            onClick = onTestOllama,
            enabled = !isTestingOllama,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isTestingOllama) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Comprobando /api/tags...")
            } else {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Probar Conexión y Consultar Modelos")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
