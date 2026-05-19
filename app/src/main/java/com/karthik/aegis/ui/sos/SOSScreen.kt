package com.karthik.aegis.ui.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var showReasonDialog by remember { mutableStateOf(false) }

    val sosReasons = listOf(
        "Accident/Crash",
        "Fall/Injury",
        "Medical Emergency",
        "Personal Safety",
        "General Emergency",
        "Other"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency SOS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SOS Trigger Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            "SOS",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "EMERGENCY SOS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Send alert to all emergency contacts",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Reason Selection
            item {
                OutlinedButton(
                    onClick = { showReasonDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedReason.isNotEmpty()) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.Edit, "Select Reason", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedReason.ifEmpty { "Select Reason..." })
                }

                if (showReasonDialog) {
                    AlertDialog(
                        onDismissRequest = { showReasonDialog = false },
                        title = { Text("Select SOS Reason") },
                        text = {
                            LazyColumn {
                                items(sosReasons) { reason ->
                                    TextButton(
                                        onClick = {
                                            selectedReason = reason
                                            showReasonDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(reason, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showReasonDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            // Trigger SOS Button
            item {
                Button(
                    onClick = {
                        val reason = selectedReason.ifEmpty { "Emergency SOS" }
                        onTriggerSOS(reason)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        SosOutlineIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("TRIGGER SOS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Safe Notification
            item {
                Button(
                    onClick = onSendSafe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.CheckCircle, "Safe", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I'm Safe Now", fontWeight = FontWeight.Bold)
                }
            }

            // Status Messages
            uiState.message?.let {
                item {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            it,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            uiState.error?.let {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            it,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Active Alerts
            if (activeAlerts.isNotEmpty()) {
                item {
                    Text(
                        "Active Alerts (${activeAlerts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(activeAlerts) { alert ->
                    SOSAlertCard(alert, onResolve = { onResolveSOS(alert.uid) })
                }
            }
        }
    }
}

@Composable
private fun SOSAlertCard(alert: SOSAlert, onResolve: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.status) {
                "ACTIVE" -> MaterialTheme.colorScheme.errorContainer
                "RESOLVED" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        alert.senderName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        alert.reason,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (alert.status == "ACTIVE") {
                    Button(
                        onClick = onResolve,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Resolved", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// Icon that may not exist, fallback
@Composable
private fun SosOutlineIcon() {
    Icon(Icons.Default.Warning, "SOS")
}
