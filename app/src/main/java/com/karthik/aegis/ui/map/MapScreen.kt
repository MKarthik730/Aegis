package com.karthik.aegis.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SafeZone
import com.karthik.aegis.model.TrackedLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.ScaleBarOverlay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    familyMembers: List<FamilyMember>,
    familyLocations: Map<String, TrackedLocation>,
    safeZones: List<SafeZone>,
    currentUserUid: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedMember by remember { mutableStateOf<FamilyMember?>(null) }
    var selectedLocation by remember { mutableStateOf<TrackedLocation?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Initialize osmdroid config once
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", 0))
            userAgentValue = context.packageName
        }
    }

    // Compute default center
    val defaultCenter = remember(familyLocations, currentUserUid, familyMembers) {
        familyLocations[currentUserUid]?.let {
            GeoPoint(it.latitude, it.longitude)
        } ?: familyMembers.firstNotNullOfOrNull { member ->
            familyLocations[member.uid]?.let { GeoPoint(it.latitude, it.longitude) }
        } ?: GeoPoint(20.5937, 78.9629)
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
            // osmdroid MapView embedded via AndroidView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(defaultCenter)
                        zoomController.setVisibility(
                            org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                        )

                        // Scale bar
                        val scaleBar = ScaleBarOverlay(this)
                        scaleBar.setAlignBottom(true)
                        scaleBar.setAlignRight(true)
                        overlays.add(scaleBar)

                        mapViewRef.value = this
                    }
                },
                update = { mapView ->
                    // Clear old overlays except scale bar (index 0)
                    while (mapView.overlays.size > 1) {
                        mapView.overlays.removeAt(1)
                    }

                    // Draw safe zones as Polygon circles
                    safeZones.forEach { zone ->
                        val zoneGeo = GeoPoint(zone.latitude, zone.longitude)
                        val zoneColor = if (zone.isHome) 0xFF2979FF.toInt() else 0xFF00C853.toInt()

                        val polygon = Polygon().apply {
                            points = buildCirclePoints(zoneGeo, zone.radiusMeters)
                            fillColor = (zoneColor and 0x00FFFFFF) or (0x4D shl 24) // 30% opacity
                            strokeColor = zoneColor
                            strokeWidth = 3f
                            title = zone.name
                        }
                        mapView.overlays.add(polygon)
                    }

                    // Draw family member markers
                    familyMembers.forEach { member ->
                        val location = familyLocations[member.uid] ?: return@forEach
                        val isCurrentUser = member.uid == currentUserUid
                        val geo = GeoPoint(location.latitude, location.longitude)

                        val circleColor = when {
                            isCurrentUser -> 0xFF2196F3.toInt()
                            member.status == "SAFE" -> 0xFF4CAF50.toInt()
                            member.status == "UNSAFE" -> 0xFFF44336.toInt()
                            else -> 0xFF9E9E9E.toInt()
                        }
                        val size = if (isCurrentUser) 96 else 80
                        val label = if (isCurrentUser) "Me" else member.name
                            .split(" ")
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .joinToString("") { it.first().uppercase() }

                        val iconBitmap = createCircleBitmap(size, circleColor, label)

                        Marker(mapView).apply {
                            position = geo
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            icon = android.graphics.drawable.BitmapDrawable(
                                mapView.context.resources, iconBitmap
                            )
                            title = member.name
                            snippet = "Status: ${member.status} | Speed: ${location.speed.toInt()} m/s"
                            setInfoWindow(org.osmdroid.views.overlay.infowindow.MarkerInfoWindow(
                                org.osmdroid.library.R.layout.layout_bubble_infowindow, mapView
                            ))
                            setOnMarkerClickListener { _, _ ->
                                selectedMember = member
                                selectedLocation = location
                                true
                            }
                            mapView.overlays.add(this)
                        }
                    }

                    mapView.invalidate()
                }
            )

            // Legend overlay (top-right)
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
                    LegendRow(Color(0xFFF44336), "Unsafe")
                    LegendRow(Color(0xFF2196F3), "You")
                    LegendRow(Color(0xFF9E9E9E), "Unknown")
                }
            }

            // Online count (bottom-left)
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
                    Icon(
                        Icons.Default.People, "Members",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    val onlineCount = familyMembers.count { it.uid in familyLocations }
                    Text(
                        "$onlineCount/${familyMembers.size} online",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Bottom Sheet for selected member
    if (selectedMember != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                selectedMember = null
                selectedLocation = null
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            MemberDetailSheet(
                member = selectedMember!!,
                location = selectedLocation,
                onDismiss = {
                    selectedMember = null
                    selectedLocation = null
                }
            )
        }
    }

    // Map lifecycle
    DisposableEffect(Unit) {
        mapViewRef.value?.onResume()
        onDispose {
            mapViewRef.value?.onPause()
        }
    }
}

private fun createCircleBitmap(size: Int, color: Int, text: String): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Fill circle
    paint.color = color
    paint.style = Paint.Style.FILL
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    // White border
    paint.color = 0xFFFFFFFF.toInt()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)

    // Text
    paint.color = 0xFFFFFFFF.toInt()
    paint.style = Paint.Style.FILL
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textSize = size * 0.38f
    val yPos = (size / 2f) - ((paint.descent() + paint.ascent()) / 2f)
    canvas.drawText(text.ifEmpty { "?" }, size / 2f, yPos, paint)

    return bitmap
}

private fun buildCirclePoints(center: GeoPoint, radiusMeters: Double): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    val segments = 36
    for (i in 0 until segments) {
        val bearing = (360.0 / segments) * i
        points.add(center.destinationPoint(radiusMeters, bearing.toFloat()))
    }
    return points
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
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = when (member.status) {
                    "SAFE" -> Color(0xFF4CAF50)
                    "UNSAFE" -> Color(0xFFF44336)
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
                            "UNSAFE" -> Color(0xFFF44336)
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

        if (member.lastSeen > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Schedule, "Time", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                Text(getRelativeTime(member.lastSeen), fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
            }
        }

        location?.let { loc ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.DirectionsRun, "Mode", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                Text("Mode: ${loc.mode} | Alt: ${loc.altitude.toInt()}m", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
            }
        }

        Button(
            onClick = { /* Call member via platform intent */ },
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

private fun getRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Last seen: just now"
        diff < 3_600_000 -> "Last seen: ${diff / 60_000} min ago"
        diff < 86_400_000 -> "Last seen: ${diff / 3_600_000}h ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            "Last seen: ${sdf.format(Date(timestamp))}"
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
