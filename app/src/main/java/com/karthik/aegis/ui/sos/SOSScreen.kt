package com.karthik.aegis.ui.sos

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karthik.aegis.model.SOSAlert
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    uiState: SOSUiState,
    activeAlerts: List<SOSAlert>,
    onTriggerSOS: (String) -> Unit,
    onResolveSOS: (String) -> Unit,
    onSendSafe: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    var showReasonSheet by remember { mutableStateOf(false) }
    var countdownActive by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableIntStateOf(5) }

    // Pulsing animation for the SOS button
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency SOS", fontWeight = FontWeight.Bold) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Active alerts as compact chips
                if (activeAlerts.isNotEmpty()) {
                    ActiveAlertChips(activeAlerts, onResolveSOS)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Selected reason display
                if (selectedReason.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { showReasonSheet = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Reason", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(selectedReason, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Main pulsating circular SOS button
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (!countdownActive) {
                        // Outer ripple rings
                        Surface(
                            modifier = Modifier.size(240.dp),
                            shape = CircleShape,
                            color = Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Ripple ring 1
                                Surface(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .scale(pulseScale),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                ) {}
                                // Ripple ring 2
                                Surface(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .scale(pulseScale * 0.85f),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                ) {}
                                // Main SOS button
                                Surface(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clickable(enabled = !uiState.isLoading) {
                                            if (selectedReason.isEmpty()) {
                                                showReasonSheet = true
                                            } else {
                                                countdownActive = true
                                                countdownValue = 5
                                            }
                                        },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    shadowElevation = 12.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (uiState.isLoading) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(40.dp),
                                                strokeWidth = 3.dp
                                            )
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    "SOS",
                                                    fontSize = 38.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    letterSpacing = 6.sp
                                                )
                                                Text(
                                                    "TAP TO SEND",
                                                    fontSize = 9.sp,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Countdown state
                        Surface(
                            modifier = Modifier.size(240.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Sending in...",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        "$countdownValue",
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { countdownActive = false }) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Countdown LaunchedEffect
                LaunchedEffect(countdownActive) {
                    while (countdownValue > 0 && countdownActive) {
                        kotlinx.coroutines.delay(1000)
                        if (!countdownActive) break
                        countdownValue--
                    }
                    if (countdownValue <= 0 && countdownActive) {
                        onTriggerSOS(selectedReason.ifEmpty { "Emergency SOS" })
                        countdownActive = false
                        countdownValue = 5
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Select reason text button (if no reason selected)
                if (selectedReason.isEmpty() && !countdownActive) {
                    TextButton(onClick = { showReasonSheet = true }) {
                        Icon(Icons.Default.List, "Select", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select SOS Reason", fontWeight = FontWeight.Medium)
                    }
                }

                // Change reason (if reason is selected)
                if (selectedReason.isNotEmpty() && !countdownActive) {
                    TextButton(onClick = { showReasonSheet = true }) {
                        Icon(Icons.Default.Edit, "Change", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Reason", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // I'm Safe button - small text style
                TextButton(
                    onClick = onSendSafe,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        "Safe",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "I'm Safe Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }

                // Status/Error Messages
                uiState.message?.let {
                    Text(
                        it,
                        fontSize = 13.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                uiState.error?.let {
                    Text(
                        it,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }

    // Reason Bottom Sheet
    if (showReasonSheet) {
        ReasonBottomSheet(
            selectedReason = selectedReason,
            onSelectReason = { reason ->
                selectedReason = reason
                showReasonSheet = false
            },
            onDismiss = { showReasonSheet = false }
        )
    }
}

@Composable
private fun ReasonBottomSheet(
    selectedReason: String,
    onSelectReason: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sosReasons = listOf(
        "Accident/Crash",
        "Fall/Injury",
        "Medical Emergency",
        "Personal Safety",
        "General Emergency",
        "Other"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Select SOS Reason",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            sosReasons.forEach { reason ->
                Surface(
                    onClick = { onSelectReason(reason) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedReason == reason)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            reason,
                            fontSize = 15.sp,
                            fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal
                        )
                        if (selectedReason == reason) {
                            Icon(
                                Icons.Default.Check,
                                "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveAlertChips(
    alerts: List<SOSAlert>,
    onResolve: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Active Alerts",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(alerts.size) { index ->
                val alert = alerts[index]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning, "Alert",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "${alert.senderName}: ${alert.reason}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 150.dp)
                        )
                        IconButton(
                            onClick = { onResolve(alert.uid) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Default.Close, "Resolve",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
