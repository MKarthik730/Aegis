package com.karthik.aegis.ui.contacts

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
import com.karthik.aegis.model.EmergencyContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<EmergencyContact>,
    uiState: ContactsUiState,
    onAddContact: (EmergencyContact) -> Unit,
    onRemoveContact: (String) -> Unit,
    onUpdateContact: (EmergencyContact) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContact?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Contact")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Error Message
            uiState.error?.let {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
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

            // Contacts List
            if (contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                "No contacts",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "No emergency contacts",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("Add One Now")
                            }
                        }
                    }
                }
            } else {
                items(contacts) { contact ->
                    ContactCard(
                        contact = contact,
                        onEdit = { editingContact = it },
                        onDelete = { onRemoveContact(it) }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingContact != null) {
        ContactDialog(
            contact = editingContact,
            onConfirm = { contact ->
                if (editingContact != null) {
                    onUpdateContact(contact)
                    editingContact = null
                } else {
                    onAddContact(contact)
                }
                showAddDialog = false
            },
            onDismiss = {
                showAddDialog = false
                editingContact = null
            }
        )
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onEdit: (EmergencyContact) -> Unit,
    onDelete: (String) -> Unit
) {
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
                color = if (contact.isPrimary) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        contact.name.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Contact Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        contact.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (contact.isPrimary) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "Primary",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(4.dp, 2.dp)
                            )
                        }
                    }
                }

                Text(
                    contact.phone,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                contact.relation.takeIf { it.isNotEmpty() }?.let {
                    Text(
                        "Relation: $it",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Actions
            IconButton(
                onClick = { onEdit(contact) }
            ) {
                Icon(Icons.Default.Edit, "Edit")
            }

            IconButton(
                onClick = { onDelete(contact.id) }
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ContactDialog(
    contact: EmergencyContact?,
    onConfirm: (EmergencyContact) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var relation by remember { mutableStateOf(contact?.relation ?: "") }
    var isPrimary by remember { mutableStateOf(contact?.isPrimary ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact != null) "Edit Contact" else "Add Contact") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Relation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it }
                    )
                    Text("Set as Primary Contact", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                        onConfirm(
                            EmergencyContact(
                                id = contact?.id ?: "",
                                name = name,
                                phone = phone,
                                relation = relation,
                                isPrimary = isPrimary,
                                fcmToken = contact?.fcmToken ?: ""
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
