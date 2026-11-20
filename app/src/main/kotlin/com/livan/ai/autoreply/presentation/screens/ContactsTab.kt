package com.livan.ai.autoreply.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livan.ai.autoreply.data.AuthorizedContact
import com.livan.ai.autoreply.data.PersonalityPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsTab(
    contacts: List<AuthorizedContact>,
    onAddContact: (String, PersonalityPreset, Boolean, String?) -> Unit,
    onDeleteContact: (String) -> Unit,
    onToggleContact: (String, Boolean) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Añadir") },
                text = { Text("Añadir Contacto") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Contactos Autorizados",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "El bot SOLO responderá a los contactos de esta lista.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay contactos autorizados",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toca '+' para añadir el nombre exacto de un contacto en WhatsApp.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactItemCard(
                            contact = contact,
                            onToggle = { onToggleContact(contact.id, it) },
                            onDelete = { onDeleteContact(contact.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, preset, allowInGroups, customPrompt ->
                onAddContact(name, preset, allowInGroups, customPrompt)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ContactItemCard(
    contact: AuthorizedContact,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (contact.isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Estilo: ${contact.getPersonalityPreset().title}", fontSize = 11.sp) }
                    )

                    if (contact.allowInGroups) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Grupos", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Switch(
                    checked = contact.isEnabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, preset: PersonalityPreset, allowInGroups: Boolean, customPrompt: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(PersonalityPreset.NATURAL) }
    var allowInGroups by remember { mutableStateOf(false) }
    var customPrompt by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Contacto Autorizado") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Nombre exacto en WhatsApp") },
                    placeholder = { Text("Ej: Mamá, Juan Pérez, +1809...") },
                    isError = showError,
                    supportingText = {
                        if (showError) Text("El nombre no puede estar vacío", color = MaterialTheme.colorScheme.error)
                        else Text("Debe coincidir con el título de la notificación.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Personality dropdown
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPreset.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Personalidad asignada") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        PersonalityPreset.entries.forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(preset.title, fontWeight = FontWeight.SemiBold)
                                        Text(preset.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedPreset = preset
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedPreset == PersonalityPreset.PERSONALIZADA) {
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        label = { Text("Instrucciones personalizadas") },
                        placeholder = { Text("Ej: Respóndele con mucho cariño y dile que...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = allowInGroups,
                        onCheckedChange = { allowInGroups = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Permitir responder en grupos si escribe este contacto", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(
                            name,
                            selectedPreset,
                            allowInGroups,
                            if (selectedPreset == PersonalityPreset.PERSONALIZADA) customPrompt else null
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
