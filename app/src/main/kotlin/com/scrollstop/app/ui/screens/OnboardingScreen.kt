package com.scrollstop.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scrollstop.app.R
import com.scrollstop.app.ui.theme.ScrollStopSuccess
import com.scrollstop.app.utils.AccessibilityUtils

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val totalPages = 4

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                repeat(totalPages) { index ->
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (index == currentPage) 32.dp else 16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index == currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            // Page content
            AnimatedContent(
                targetState = currentPage,
                label = "onboarding_page"
            ) { page ->
                when (page) {
                    0 -> OnboardingPage0()
                    1 -> OnboardingPage1()
                    2 -> OnboardingPage2(onOpenSettings = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    })
                    else -> OnboardingPage3(context = context, onComplete = onComplete)
                }
            }

            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (currentPage == 0) Arrangement.End else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0 && currentPage < 3) {
                    TextButton(onClick = { currentPage-- }) {
                        Text("Back")
                    }
                }
                if (currentPage < 3) {
                    Button(
                        onClick = { currentPage++ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (currentPage == 2) "I enabled it" else "Next")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage0() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.reelstoper_logo),
            contentDescription = "ReelStoper Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Text(
            "Welcome to\nReelStoper",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Block YouTube Shorts and Instagram Reels without blocking the rest of these apps.\n\nStay focused on what matters.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingPage1() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Default.Accessibility, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Text(
            "Why Accessibility Permission?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OnboardingInfoRow(Icons.Default.Visibility, "What we access",
                    "We read the UI structure of YouTube and Instagram to detect Shorts/Reels screens")
                OnboardingInfoRow(Icons.Default.Block, "What we block",
                    "Only YouTube Shorts and Instagram Reels - nothing else")
                OnboardingInfoRow(Icons.Default.Lock, "What we DON'T do",
                    "We never read messages, passwords, or personal content. Zero data leaves your device.")
                OnboardingInfoRow(Icons.Default.Storage, "Where data goes",
                    "All statistics are stored locally on your device only")
            }
        }
    }
}

@Composable
private fun OnboardingPage2(onOpenSettings: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Default.Settings, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Text(
            "Enable the Accessibility Service",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SetupStep(step = 1, text = "Tap the button below to open Accessibility Settings")
                SetupStep(step = 2, text = "Find \"Downloaded apps\" or \"Installed services\"")
                SetupStep(step = 3, text = "Tap \"ReelStoper - Shorts & Reels Blocker\"")
                SetupStep(step = 4, text = "Toggle it ON and confirm the permission dialog")
                SetupStep(step = 5, text = "Return to this app and tap \"I enabled it\"")
            }
        }
        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open Accessibility Settings")
        }
        Text(
            "ReelStoper cannot enable this automatically - Android requires you to do it manually for your privacy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingPage3(context: android.content.Context, onComplete: () -> Unit) {
    val isEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(context)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        if (isEnabled) {
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                tint = ScrollStopSuccess, modifier = Modifier.size(80.dp))
            Text(
                "You're all set!",
                style = MaterialTheme.typography.headlineLarge,
                color = ScrollStopSuccess,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "ReelStoper is now protecting you from YouTube Shorts and Instagram Reels.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Icon(Icons.Default.Warning, contentDescription = null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(80.dp))
            Text(
                "Service not detected",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Go back and make sure you enabled the ReelStoper service in Accessibility Settings.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) ScrollStopSuccess else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isEnabled) "Start Using ReelStoper" else "Continue Anyway")
        }
    }
}

@Composable
private fun OnboardingInfoRow(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 1.dp)
        )
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SetupStep(step: Int, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$step",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Text(text, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp))
    }
}
