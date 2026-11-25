package com.scrollstop.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrollstop.app.model.ContentType
import com.scrollstop.app.ui.theme.ScrollStopDanger
import com.scrollstop.app.ui.theme.ScrollStopSuccess
import com.scrollstop.app.viewmodel.MainUiState
import com.scrollstop.app.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(viewModel: MainViewModel, uiState: MainUiState) {
    val scrollState = rememberScrollState()
    val stats = uiState.statistics

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Today's stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatsBox("Shorts", stats.todayShortsBlocked.toString(), color = MaterialTheme.colorScheme.primary)
                    VerticalDivider(modifier = Modifier.height(60.dp))
                    StatsBox("Reels", stats.todayReelsBlocked.toString(), color = MaterialTheme.colorScheme.secondary)
                    VerticalDivider(modifier = Modifier.height(60.dp))
                    StatsBox("Total", stats.todayBlocked.toString(), color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        // Weekly stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("This Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatsBox("Shorts", stats.weekShortsBlocked.toString(), color = MaterialTheme.colorScheme.primary)
                    VerticalDivider(modifier = Modifier.height(60.dp))
                    StatsBox("Reels", stats.weekReelsBlocked.toString(), color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // All-time stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("All Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatsBox("Shorts", stats.totalShortsBlocked.toString(), color = MaterialTheme.colorScheme.primary)
                    VerticalDivider(modifier = Modifier.height(60.dp))
                    StatsBox("Reels", stats.totalReelsBlocked.toString(), color = MaterialTheme.colorScheme.secondary)
                    VerticalDivider(modifier = Modifier.height(60.dp))
                    StatsBox("Total", stats.totalBlocked.toString(), color = MaterialTheme.colorScheme.tertiary)
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\u23F1 Est. ${stats.estimatedMinutesSaved} min saved*",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ScrollStopSuccess,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "* Estimates are based on ~3 min average session avoided",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Recent events
        val recentSlice = uiState.recentEvents.take(10)
        if (recentSlice.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Recent Blocks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    recentSlice.forEachIndexed { index, event ->
                        RecentEventRow(event)
                        if (index < recentSlice.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatsBox(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecentEventRow(event: com.scrollstop.app.model.BlockEvent) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateStr = remember(event.timestamp) { dateFormat.format(Date(event.timestamp)) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = event.contentType.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (event.contentType == ContentType.YOUTUBE_SHORTS) ScrollStopDanger
                else MaterialTheme.colorScheme.secondary
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
