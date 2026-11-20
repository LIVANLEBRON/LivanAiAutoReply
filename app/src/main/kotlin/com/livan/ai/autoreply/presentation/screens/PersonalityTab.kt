package com.livan.ai.autoreply.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livan.ai.autoreply.data.AppSettings
import com.livan.ai.autoreply.data.PersonalityPreset
import kotlin.math.roundToInt

@Composable
fun PersonalityTab(
    settings: AppSettings,
    simulationReply: String?,
    isSimulating: Boolean,
    onUpdateSettings: (AppSettings) -> Unit,
    onSimulate: (testMessage: String, preset: PersonalityPreset, customPrompt: String, temperature: Float) -> Unit
) {
    val scrollState = rememberScrollState()

    var selectedPreset by remember(settings.defaultPersonality) {
        mutableStateOf(PersonalityPreset.fromName(settings.defaultPersonality))
    }
    var promptText by remember(settings.customSystemPrompt) {
        mutableStateOf(settings.customSystemPrompt)
    }
    var temperature by remember(settings.temperature) {
        mutableFloatStateOf(settings.temperature)
    }
    var testInput by remember { mutableStateOf("Hola, ¿dónde estás? Necesito que me ayudes con algo rápido.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personalidad y Estilo de Respuesta",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Define cómo debe responder la IA cuando reciba mensajes en WhatsApp.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Preset selector cards
        Text(
            text = "Perfiles Predefinidos",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PersonalityPreset.entries.forEach { preset ->
                val isSelected = preset == selectedPreset
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPreset = preset
                            promptText = preset.defaultPrompt
                            onUpdateSettings(
                                settings.copy(
                                    defaultPersonality = preset.name,
                                    customSystemPrompt = preset.defaultPrompt
                                )
                            )
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                promptText = preset.defaultPrompt
                                onUpdateSettings(
                                    settings.copy(
                                        defaultPersonality = preset.name,
                                        customSystemPrompt = preset.defaultPrompt
                                    )
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = preset.title,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.description,
                                fontSize = 12.sp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Editable System Prompt Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instrucciones de Personalidad (System Prompt)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = {
                            promptText = selectedPreset.defaultPrompt
                            onUpdateSettings(settings.copy(customSystemPrompt = promptText))
                        }
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Restablecer a plantilla")
                    }
                }

                OutlinedTextField(
                    value = promptText,
                    onValueChange = {
                        promptText = it
                        onUpdateSettings(settings.copy(customSystemPrompt = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    placeholder = { Text("Escribe las instrucciones que recibirá Ollama...") }
                )
            }
        }

        // Temperature Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Temperatura del Modelo",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${((temperature * 100).roundToInt() / 100f)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Valores bajos (0.2) = respuestas más directas y consistentes. Valores altos (0.8) = respuestas más creativas y variadas.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = temperature,
                    onValueChange = {
                        temperature = it
                        onUpdateSettings(settings.copy(temperature = it))
                    },
                    valueRange = 0.0f..1.0f,
                    steps = 10
                )
            }
        }

        // Interactive Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulador de Conversación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Text(
                    text = "Envía un mensaje de prueba a Ollama para verificar cómo respondería con este estilo.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )

                OutlinedTextField(
                    value = testInput,
                    onValueChange = { testInput = it },
                    label = { Text("Mensaje recibido de prueba") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        onSimulate(testInput, selectedPreset, promptText, temperature)
                    },
                    enabled = !isSimulating && testInput.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isSimulating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generando...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar Respuesta")
                    }
                }

                if (simulationReply != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Respuesta simulada de Ollama:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = simulationReply,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
