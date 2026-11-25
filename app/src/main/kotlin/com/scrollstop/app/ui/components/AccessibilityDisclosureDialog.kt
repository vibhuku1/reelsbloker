package com.scrollstop.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Prominent Accessibility API Disclosure dialog required by Google Play policy
 * before directing the user to Android Accessibility Settings.
 */
@Composable
fun AccessibilityDisclosureDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Accessibility Service Disclosure",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ReelStoper uses the Android AccessibilityService API to detect when YouTube Shorts or Instagram Reels are opened and automatically navigate back.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Only monitors UI structure inside YouTube and Instagram.\n" +
                           "• Never reads passwords, DMs, search inputs, or personal content.\n" +
                           "• No internet access — zero data is collected, transmitted, or shared.\n" +
                           "• You can disable this service anytime in Android Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("I Agree & Open Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
