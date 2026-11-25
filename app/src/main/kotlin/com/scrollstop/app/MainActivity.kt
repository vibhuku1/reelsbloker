package com.scrollstop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.scrollstop.app.ui.navigation.ScrollStopNavHost
import com.scrollstop.app.ui.theme.ScrollStopTheme
import com.scrollstop.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            ScrollStopTheme(themeMode = uiState.settings.themeMode) {
                ScrollStopNavHost(
                    viewModel = viewModel,
                    uiState = uiState,
                    hasCompletedOnboarding = uiState.settings.hasCompletedOnboarding
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh accessibility state when returning from Settings
        viewModel.refreshAccessibilityState()
    }
}
