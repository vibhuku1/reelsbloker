package com.scrollstop.app.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollstop.app.model.ContentType
import com.scrollstop.app.service.ScrollStopAccessibilityService
import com.scrollstop.app.ui.theme.DarkBackground
import com.scrollstop.app.ui.theme.ScrollStopDanger
import com.scrollstop.app.ui.theme.ScrollStopTheme
import com.scrollstop.app.model.ThemeMode

/**
 * Blocking screen shown when Shorts or Reels are detected.
 * This is a separate Activity so it can be launched from the accessibility service.
 */
class BlockingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val contentTypeName = intent.getStringExtra(ScrollStopAccessibilityService.EXTRA_CONTENT_TYPE)
            ?: ContentType.YOUTUBE_SHORTS.name
        val contentType = runCatching { ContentType.valueOf(contentTypeName) }
            .getOrDefault(ContentType.YOUTUBE_SHORTS)
        val appPackage = intent.getStringExtra(ScrollStopAccessibilityService.EXTRA_APP_PACKAGE) ?: ""

        setContent {
            ScrollStopTheme(themeMode = ThemeMode.DARK) {
                BlockingScreen(
                    contentType = contentType,
                    appPackage = appPackage,
                    onBackToFocus = { finish() }
                )
            }
        }
    }
}

@Composable
fun BlockingScreen(
    contentType: ContentType,
    appPackage: String,
    onBackToFocus: () -> Unit
) {
    val appName = when (appPackage) {
        "com.google.android.youtube" -> "YouTube"
        "com.instagram.android" -> "Instagram"
        else -> appPackage
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "SCROLLSTOP",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            // Block icon
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                tint = ScrollStopDanger,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "STOP SCROLLING!",
                style = MaterialTheme.typography.headlineMedium,
                color = ScrollStopDanger,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "\"Go back to work.\nYour goals are more important\nthan endless scrolling.\"",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow(label = "App", value = appName)
                    InfoRow(label = "Blocked", value = contentType.displayName)
                    InfoRow(label = "Status", value = "Protection Active ✓")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CTA button
            Button(
                onClick = onBackToFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Back to Focus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
