package com.karthik.aegis.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SOSAlert
import com.karthik.aegis.model.TrackedLocation
import com.karthik.aegis.viewmodel.HomeUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    familyMembers: List<FamilyMember>,
    familyLocations: Map<String, TrackedLocation>,
    activeAlerts: List<SOSAlert>,
    isTrackingEnabled: Boolean,
    userName: String,
    onNavigateToSOS: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToZones: () -> Unit,
    onSignOut: () -> Unit,
    onToggleTracking: () -> Unit
) {
    // Greeting based on time of day
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // Pulsing animation for SOS FAB when there are active alerts
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (activeAlerts.isNotEmpty()) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "$greeting, ${userName.take(12)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(6.dp),
                                shape = CircleShape,
                                color = if (isTrackingEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                            ) {}
                            Text(
                                if (isTrackingEnabled) "Tracking Active" else "Tracking Off",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTracking) {
                        Icon(
                            if (isTrackingEnabled) Icons.Default.LocationOn else Icons.Default.LocationOff,
                            "Toggle Tracking",
                            tint = if (isTrackingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, "Sign Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier.scale(
                    if (activeAlerts.isNotEmpty()) fabScale else 1f
                )
            ) {
                FloatingActionButton(
                    onClick = onNavigateToSOS,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        "SOS",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Active Alerts Banner
            if (activeAlerts.isNotEmpty()) {
                item {
                    AlertBanner(activeAlerts)
                }
            }

            // 2x2 Quick Action Grid
            item {
                QuickActionGrid(
                    onNavigateToSOS = onNavigateToSOS,
                    onNavigateToMap = onNavigateToMap,
                    onNavigateToContacts = onNavigateToContacts,
                    onNavigateToZones = onNavigateToZones,
                    activeAlerts = activeAlerts.size
                )
            }

            // Family Members Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "👨‍👩‍👧‍👦 Family Members",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (familyMembers.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "${familyMembers.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Member Cards
            if (familyMembers.isEmpty()) {
                item {
                    EmptyFamilyState()
                }
            } else {
                items(familyMembers) { member ->
                    FamilyMemberCard(
                        member = member,
                        location = familyLocations[member.uid],
                        isOnline = familyLocations.containsKey(member.uid),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun AlertBanner(alerts: List<SOSAlert>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                "Alert",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "🚨 ${alerts.size} Active Alert${if (alerts.size > 1) "s" else ""}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                alerts.take(2).forEach { alert ->
                    Text(
                        "${alert.senderName}: ${alert.reason}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Button(
                onClick = { /* Navigate to SOS */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickActionGrid(
    onNavigateToSOS: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToZones: () -> Unit,
    activeAlerts: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // SOS
            QuickActionItem(
                icon = Icons.Default.Warning,
                label = "SOS",
                color = MaterialTheme.colorScheme.error,
                badge = if (activeAlerts > 0) "$activeAlerts" else null,
                onClick = onNavigateToSOS,
                modifier = Modifier.weight(1f)
            )
            // Map
            QuickActionItem(
                icon = Icons.Default.Map,
                label = "Map",
                color = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToMap,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Contacts
            QuickActionItem(
                icon = Icons.Default.Contacts,
                label = "Contacts",
                color = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToContacts,
                modifier = Modifier.weight(1f)
            )
            // Zones
            QuickActionItem(
                icon = Icons.Default.AddLocation,
                label = "Safe Zones",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onNavigateToZones,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box {
                    Icon(
                        icon,
                        label,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                    // Badge overlay for SOS
                    if (badge != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-4).dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyFamilyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.People,
                        "No members",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No family members yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                "Add family members to see them on the map",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    location: TrackedLocation?,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar with online/offline dot
            Box {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = when (member.status) {
                        "SAFE" -> Color(0xFF4CAF50)
                        "UNSAFE" -> Color(0xFFFF3D3D)
                        else -> MaterialTheme.colorScheme.primary
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            member.name.firstOrNull()?.toString() ?: "?",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Online/Offline dot
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp),
                    shape = CircleShape,
                    color = if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                    border = null
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {}
                    }
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    member.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (member.status) {
                            "SAFE" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            "UNSAFE" -> Color(0xFFFF3D3D).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            member.status.ifEmpty { "UNKNOWN" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (member.status) {
                                "SAFE" -> Color(0xFF4CAF50)
                                "UNSAFE" -> Color(0xFFFF3D3D)
                                else -> MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Online/Offline text
                    Text(
                        if (isOnline) "Online" else "Offline",
                        fontSize = 10.sp,
                        color = if (isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                    )
                }

                // Last seen time
                if (member.lastSeen > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            "Time",
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Last seen: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(member.lastSeen))}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Call button
            FilledTonalIconButton(
                onClick = { /* Call member */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Call,
                    "Call",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
