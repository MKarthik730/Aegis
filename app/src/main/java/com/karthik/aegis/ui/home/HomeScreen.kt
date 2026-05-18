package com.karthik.aegis.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SOSAlert
import com.karthik.aegis.model.TrackedLocation
import com.karthik.aegis.service.LocationTrackingService
import com.karthik.aegis.utils.DistanceUtils
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    familyMembers: List<FamilyMember>,
    familyLocations: Map<String, TrackedLocation>,
    activeAlerts: List<SOSAlert>,
    onNavigateToSOS: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: HomeViewModel,
    context: Context
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aegis • Family Safety") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        // Corrected LogOut to Logout
                        Icon(Icons.Default.Logout, "Sign Out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSOS,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(Icons.Default.Warning, "SOS", tint = Color.White)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Alerts Section
            if (activeAlerts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "🚨 ACTIVE ALERTS (${activeAlerts.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            activeAlerts.forEach { alert ->
                                Text(
                                    "${alert.senderName}: ${alert.reason}",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToSOS,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Warning, "SOS", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SOS")
                    }

                    Button(
                        onClick = onNavigateToContacts,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Person, "Contacts", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contacts")
                    }

                    Button(
                        onClick = {
                            LocationTrackingService.startTracking(context, LocationTrackingService.MODE_ACTIVE)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, "Track", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Track")
                    }
                }
            }

            // Family Members Section
            item {
                Text(
                    "👨‍👩‍👧‍👦 Family (${familyMembers.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(familyMembers) { member ->
                FamilyMemberCard(member, familyLocations[member.uid])
            }

            if (familyMembers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                // Changed PersonAdd to Person to ensure compatibility if index is stale
                                Icons.Default.Person,
                                "No members",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "No family members yet",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyMemberCard(member: FamilyMember, location: TrackedLocation?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = when (member.status) {
                    "SAFE" -> Color(0xFF4CAF50)
                    "UNSAFE" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        member.name.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Member Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    member.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Status: ${member.status}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                location?.let {
                    val speed = it.speed.roundToInt()
                    Text(
                        "Speed: ${speed}m/s | Mode: ${it.mode}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Call Button
            IconButton(
                onClick = { /* Start call to member */ }
            ) {
                Icon(Icons.Default.Call, "Call")
            }
        }
    }
}
