package com.scrollstop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrollstop.app.ui.navigation.ScrollStopNavHost
import com.scrollstop.app.ui.theme.ScrollStopTheme
import com.scrollstop.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(900)
                showSplash = false
            }

            ScrollStopTheme(themeMode = uiState.settings.themeMode) {
                if (showSplash) {
                    SplashScreen()
                } else {
                    ScrollStopNavHost(
                        viewModel = viewModel,
                        uiState = uiState,
                        hasCompletedOnboarding = uiState.settings.hasCompletedOnboarding
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh accessibility state when returning from Settings
        viewModel.refreshAccessibilityState()
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.reelstoper_logo),
                contentDescription = "ReelStoper Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(136.dp)
                    .clip(RoundedCornerShape(28.dp))
            )
            Text(
                text = "ReelStoper",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stay focused. Block Shorts & Reels.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
