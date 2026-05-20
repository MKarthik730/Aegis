package com.karthik.aegis.ui.map

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SafeZone
import com.karthik.aegis.model.TrackedLocation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    familyMembers: List<FamilyMember>,
    familyLocations: Map<String, TrackedLocation>,
    safeZones: List<SafeZone>,
    selectedMember: FamilyMember?,
    selectedMemberLocation: TrackedLocation?,
    onSelectMember: (FamilyMember) -> Unit,
    onClearSelection: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    // Build member location items
    val memberMapItems = familyMembers.mapNotNull { member ->
        familyLocations[member.uid]?.let { loc ->
            MemberMapItem(member, LatLng(loc.latitude, loc.longitude))
        }
    }

    // Default camera centered on first member or default location
    val defaultCenter = if (memberMapItems.isNotEmpty()) {
        val avgLat = memberMapItems.map { it.latLng.latitude }.average()
        val avgLng = memberMapItems.map { it.latLng.longitude }.average()
        LatLng(avgLat, avgLng)
    } else {
        LatLng(12.9716, 77.5946)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 11f)
    }

    // Open bottom sheet when a member is selected
    LaunchedEffect(selectedMember) {
        if (selectedMember != null) {
            showSheet = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Map", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                onMapClick = {
                    showSheet = false
                    onClearSelection()
                }
            ) {
                // Family member markers with initials
                memberMapItems.forEach { item ->
                    val initials = item.member.name
                        .split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }

                    MarkerInfoWindowContent(
                        state = MarkerState(position = item.latLng),
                        title = item.member.name,
                        snippet = item.member.status,
                        onClick = {
                            onSelectMember(item.member)
                            showSheet = true
                            false // let info window show
                        }
                    ) {
                        // Custom marker with initials
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (item.member.status) {
                                        "SAFE" -> Color(0xFF4CAF50)
                                        "UNSAFE" -> Color(0xFFFF3D3D)
                                        else -> Color(0xFF2979FF)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                initials,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Safe zones as circles on the map
                safeZones.forEach { zone ->
                    val zoneColor = when (zone.type) {
                        "HOME" -> Color(0xFF2979FF)
                        "SCHOOL" -> Color(0xFFFFA000)
                        "WORK" -> Color(0xFF7C4DFF)
                        else -> Color(0xFF00C853)
                    }
                    val latLng = LatLng(zone.latitude, zone.longitude)
                    Circle(
                        center = latLng,
                        radius = zone.radiusMeters,
                        fillColor = zoneColor.copy(alpha = 0.15f),
                        strokeColor = zoneColor.copy(alpha = 0.6f),
                        strokeWidth = 3f
                    )
                }
            }

            // Legend overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Legend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LegendRow(Color(0xFF4CAF50), "Safe")
                    LegendRow(Color(0xFFFF3D3D), "Unsafe")
                    LegendRow(Color(0xFF2979FF), "Member")
                }
            }

            // Member count overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.People, "Members", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "${memberMapItems.size}/${familyMembers.size} online",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Bottom Sheet for selected member
    if (showSheet && selectedMember != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                onClearSelection()
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            MemberDetailSheet(
                member = selectedMember,
                location = selectedMemberLocation,
                onCall = { /* Call member */ },
                onDismiss = {
                    showSheet = false
                    onClearSelection()
                }
            )
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MemberDetailSheet(
    member: FamilyMember,
    location: TrackedLocation?,
    onCall: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Member header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(64.dp),
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
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(member.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = when (member.status) {
                            "SAFE" -> Color(0xFF4CAF50)
                            "UNSAFE" -> Color(0xFFFF3D3D)
                            else -> Color(0xFF9E9E9E)
                        }
                    ) {}
                    Text(
                        member.status.ifEmpty { "UNKNOWN" },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider()

        // Location details
        location?.let { loc ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Latitude", String.format("%.4f", loc.latitude))
                DetailItem("Longitude", String.format("%.4f", loc.longitude))
                DetailItem("Speed", "${loc.speed.toInt()} m/s")
            }
        }

        // Last seen
        if (member.lastSeen > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Schedule, "Time", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                Text(
                    "Last seen: ${SimpleDateFormat(\"MMM dd, HH:mm\", Locale.getDefault()).format(Date(member.lastSeen))}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Call button
        Button(
            onClick = onCall,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Call, "Call", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Call ${member.name}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private data class MemberMapItem(
    val member: FamilyMember,
    val latLng: LatLng
)
