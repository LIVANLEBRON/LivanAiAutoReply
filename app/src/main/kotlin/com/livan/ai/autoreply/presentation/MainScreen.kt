package com.livan.ai.autoreply.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.livan.ai.autoreply.presentation.screens.*

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Inicio", Icons.Default.Dashboard),
    CONTACTS("Contactos", Icons.Default.People),
    PERSONALITY("Personalidad", Icons.Default.Psychology),
    SERVER("Servidor", Icons.Default.Dns),
    HISTORY("Historial", Icons.Default.History),
    SETTINGS("Ajustes", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val isPermissionGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val ollamaStatus by viewModel.ollamaStatus.collectAsStateWithLifecycle()
    val isTestingOllama by viewModel.isTestingOllama.collectAsStateWithLifecycle()
    val availableModels by viewModel.availableModels.collectAsStateWithLifecycle()
    val simulationReply by viewModel.simulationReply.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Livan AI AutoReply",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    Switch(
                        checked = settings.isMasterEnabled,
                        onCheckedChange = { viewModel.toggleMasterSwitch(it) },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                NavigationTab.DASHBOARD -> DashboardTab(
                    settings = settings,
                    contacts = contacts,
                    history = history,
                    isPermissionGranted = isPermissionGranted,
                    ollamaStatus = ollamaStatus,
                    isTestingOllama = isTestingOllama,
                    onToggleMaster = { viewModel.toggleMasterSwitch(it) },
                    onTestOllama = { viewModel.testOllamaConnection() },
                    onNavigateToServer = { selectedTab = NavigationTab.SERVER },
                    onNavigateToContacts = { selectedTab = NavigationTab.CONTACTS }
                )
                NavigationTab.CONTACTS -> ContactsTab(
                    contacts = contacts,
                    onAddContact = { name, preset, allowInGroups, customPrompt ->
                        viewModel.addContact(name, preset, allowInGroups, customPrompt)
                    },
                    onDeleteContact = { viewModel.deleteContact(it) },
                    onToggleContact = { id, enabled -> viewModel.toggleContact(id, enabled) }
                )
                NavigationTab.PERSONALITY -> PersonalityTab(
                    settings = settings,
                    simulationReply = simulationReply,
                    isSimulating = isSimulating,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onSimulate = { msg, preset, prompt, temp ->
                        viewModel.simulateChat(msg, preset, prompt, temp)
                    }
                )
                NavigationTab.SERVER -> ServerTab(
                    settings = settings,
                    ollamaStatus = ollamaStatus,
                    isTestingOllama = isTestingOllama,
                    availableModels = availableModels,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onTestOllama = { viewModel.testOllamaConnection() }
                )
                NavigationTab.HISTORY -> HistoryTab(
                    history = history,
                    onClearHistory = { viewModel.clearHistory() }
                )
                NavigationTab.SETTINGS -> SettingsTab(
                    settings = settings,
                    isNotificationPermissionGranted = isPermissionGranted,
                    onUpdateSettings = { viewModel.updateSettings(it) }
                )
            }
        }
    }
}
