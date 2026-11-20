package com.livan.ai.autoreply.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.livan.ai.autoreply.ui.theme.LivanAIAutoReplyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LivanAIAutoReplyTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh notification permission status when returning from settings
        viewModel.checkNotificationPermission()
    }
}
