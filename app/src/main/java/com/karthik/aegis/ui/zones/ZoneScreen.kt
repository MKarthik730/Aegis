package com.karthik.aegis.ui.zones

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.karthik.aegis.model.SafeZone
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.ScaleBarOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneScreen(
    uiState: ZoneUiState,
    safeZones: List<SafeZone>,
    onAddZone: (SafeZone) -> Unit,
    onRemoveZone: (String) -> Unit,
    onUpdateZone: (SafeZone) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddSheet by remember { mutableStateOf(false) }
    var longPressLatLng by remember { mutableStateOf<GeoPoint?>(null) }
    var editingZone by remember { mutableStateOf<SafeZone?>(null) }

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Initialize osmdroid config once
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", 0))
            userAgentValue = context.packageName
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safe Zones", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Map section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(12.0)
                                controller.setCenter(GeoPoint(12.9716, 77.5946))
                                zoomController.setVisibility(
                                    org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                                )

                                // Scale bar
                                val scaleBar = ScaleBarOverlay(this)
                                scaleBar.setAlignBottom(true)
                                scaleBar.setAlignRight(true)
                                overlays.add(scaleBar)

                                // Long-press handler via MapEventsOverlay
                                val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                                    override fun longPressHelper(p: GeoPoint?): Boolean {
                                        p?.let {
                                            longPressLatLng = it
                                            editingZone = null
                                            showAddSheet = true
                                        }
                                        return true
                                    }
                                })
                                overlays.add(eventsOverlay)

                                mapViewRef.value = this
                            }
                        },
                        update = { mapView ->
                            // Clear old overlays except scale bar (index 0) and events overlay (index 1)
                            while (mapView.overlays.size > 2) {
                                mapView.overlays.removeAt(2)
                            }

                            // Draw existing zones as Polygon circles
                            safeZones.forEach { zone ->
                                val zoneGeo = GeoPoint(zone.latitude, zone.longitude)
                                val zoneColor = when (zone.type) {
                                    "HOME" -> 0xFF2979FF.toInt()
                                    "SCHOOL" -> 0xFFFFA000.toInt()
                                    "WORK" -> 0xFF7C4DFF.toInt()
                                    else -> 0xFF00C853.toInt()
                                }

                                Polygon().apply {
                                    points = buildCirclePoints(zoneGeo, zone.radiusMeters)
                                    fillColor = (zoneColor and 0x00FFFFFF) or (0x4D shl 24) // 30% opacity
                                    strokeColor = zoneColor
                                    strokeWidth = 3f
                                    title = zone.name
                                    mapView.overlays.add(this)
                                }
                            }

                            mapView.invalidate()
                        }
                    )

                    // Hint overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TouchApp, "Tap", modifier = Modifier.size(16.dp))
                            Text("Long-press on map to add a zone", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Zone list header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Your Zones", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (safeZones.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "${safeZones.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Status messages
            uiState.message?.let {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, "OK", tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Text(it, fontSize = 13.sp, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            uiState.error?.let {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Zone cards
            if (safeZones.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddLocation, "No zones",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No safe zones yet", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.outline)
                            Text("Long-press on the map to add one", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                        }
                    }
                }
            } else {
                items(safeZones) { zone ->
                    ZoneCard(
                        zone = zone,
                        onEdit = { editingZone = it; showAddSheet = true },
                        onDelete = { onRemoveZone(it.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add / Edit Zone Bottom Sheet
    if (showAddSheet) {
        ZoneAddEditSheet(
            zone = editingZone,
            geoPoint = longPressLatLng,
            onConfirm = { zone ->
                if (editingZone != null) onUpdateZone(zone) else onAddZone(zone)
                showAddSheet = false
                longPressLatLng = null
                editingZone = null
            },
            onDismiss = {
                showAddSheet = false
                longPressLatLng = null
                editingZone = null
            }
        )
    }

    // Map lifecycle
    DisposableEffect(Unit) {
        mapViewRef.value?.onResume()
        onDispose {
            mapViewRef.value?.onPause()
        }
    }
}

@Composable
private fun ZoneCard(
    zone: SafeZone,
    onEdit: (SafeZone) -> Unit,
    onDelete: (String) -> Unit
) {
    val zoneColor = when (zone.type) {
        "HOME" -> Color(0xFF2979FF)
        "SCHOOL" -> Color(0xFFFFA000)
        "WORK" -> Color(0xFF7C4DFF)
        else -> Color(0xFF00C853)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = zoneColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (zone.type) {
                            "HOME" -> Icons.Default.Home
                            "SCHOOL" -> Icons.Default.School
                            "WORK" -> Icons.Default.Business
                            else -> Icons.Default.Place
                        }, "Zone", tint = zoneColor, modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(zone.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = zoneColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            zone.type, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = zoneColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("${zone.radiusMeters.toInt()}m radius", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (zone.notifyOnEnter) Text("↩️ Enter", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    if (zone.notifyOnExit) Text("🚪 Exit", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            FilledTonalIconButton(onClick = { onEdit(zone) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
            }
            FilledTonalIconButton(
                onClick = { onDelete(zone.id) },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ZoneAddEditSheet(
    zone: SafeZone?,
    geoPoint: GeoPoint?,
    onConfirm: (SafeZone) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(zone?.name ?: "") }
    var radius by remember { mutableFloatStateOf((zone?.radiusMeters ?: 150.0).toFloat()) }
    var type by remember { mutableStateOf(zone?.type ?: "SAFE") }
    var notifyEnter by remember { mutableStateOf(zone?.notifyOnEnter ?: true) }
    var notifyExit by remember { mutableStateOf(zone?.notifyOnExit ?: true) }

    val zoneTypes = listOf("HOME", "SCHOOL", "WORK", "SAFE")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (zone != null) "Edit Zone" else "Add Safe Zone",
                fontSize = 20.sp, fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Zone Name") },
                leadingIcon = { Icon(Icons.Default.Edit, "Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text("Radius: ${radius.toInt()}m", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 50f..500f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Zone Type", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                zoneTypes.forEach { zoneType ->
                    FilterChip(
                        onClick = { type = zoneType },
                        label = { Text(zoneType, fontSize = 12.sp) },
                        selected = type == zoneType
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notify on Enter", fontSize = 14.sp)
                Switch(checked = notifyEnter, onCheckedChange = { notifyEnter = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notify on Exit", fontSize = 14.sp)
                Switch(checked = notifyExit, onCheckedChange = { notifyExit = it })
            }

            if (geoPoint != null) {
                Text(
                    "Location: ${String.format("%.4f", geoPoint.latitude)}, ${String.format("%.4f", geoPoint.longitude)}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.outline
                )
            }

            Button(
                onClick = {
                    val lat = zone?.latitude ?: geoPoint?.latitude ?: 0.0
                    val lng = zone?.longitude ?: geoPoint?.longitude ?: 0.0
                    onConfirm(
                        SafeZone(
                            id = zone?.id ?: "",
                            name = name.ifEmpty { "Safe Zone" },
                            latitude = lat,
                            longitude = lng,
                            radiusMeters = radius.toDouble(),
                            type = type,
                            notifyOnEnter = notifyEnter,
                            notifyOnExit = notifyExit
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = name.isNotEmpty()
            ) {
                Icon(
                    if (zone != null) Icons.Default.Save else Icons.Default.AddLocation,
                    "Save", modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (zone != null) "Update Zone" else "Add Zone", fontWeight = FontWeight.Bold)
            }
        }
    }
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
