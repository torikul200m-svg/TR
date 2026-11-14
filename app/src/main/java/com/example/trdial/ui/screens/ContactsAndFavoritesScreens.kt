package com.example.trdial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.trdial.calling.CallingMode
import com.example.trdial.data.local.ContactEntity
import com.example.trdial.ui.components.ContactAvatar
import com.example.trdial.ui.components.TelecomCard
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TrBackground
import com.example.ui.theme.TrBlue
import com.example.ui.theme.TrCyan
import com.example.ui.theme.TrGreen
import com.example.ui.theme.TrRed
import com.example.ui.theme.TrSurface
import com.example.ui.theme.TrTextPrimary
import com.example.ui.theme.TrTextSecondary

@Composable
fun ContactsScreen(
    contacts: List<ContactEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSaveContact: (ContactEntity) -> Unit,
    onDeleteContact: (ContactEntity) -> Unit,
    onToggleFavorite: (ContactEntity) -> Unit,
    onCallContact: (ContactEntity, CallingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingContact by remember { mutableStateOf<ContactEntity?>(null) }
    var showContactDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TrBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingContact = null
                    showContactDialog = true
                },
                containerColor = TrBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("contacts_add_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_contact))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.contacts_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TrTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(stringResource(R.string.search_contacts_hint), color = TrTextSecondary) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = TrCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = TrTextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = trTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contacts_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = TrCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.empty_contacts),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TrTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactItemCard(
                            contact = contact,
                            onToggleFavorite = { onToggleFavorite(contact) },
                            onEdit = {
                                editingContact = contact
                                showContactDialog = true
                            },
                            onDelete = { onDeleteContact(contact) },
                            onCallSip = { onCallContact(contact, CallingMode.SIP_TO_SIP) },
                            onCallApp = { onCallContact(contact, CallingMode.APP_TO_APP) }
                        )
                    }
                }
            }
        }
    }

    if (showContactDialog) {
        ContactEditorDialog(
            initial = editingContact,
            onDismiss = { showContactDialog = false },
            onSave = { contact ->
                onSaveContact(contact)
                showContactDialog = false
            }
        )
    }
}

@Composable
fun FavoritesScreen(
    favorites: List<ContactEntity>,
    onToggleFavorite: (ContactEntity) -> Unit,
    onCallContact: (ContactEntity, CallingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TrTextPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = TrCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.empty_favorites),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TrTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(favorites, key = { it.id }) { contact ->
                    TelecomCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ContactAvatar(name = contact.name, size = 46.dp, accentColor = TrCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TrTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = contact.sipUri.ifBlank {
                                        contact.phoneNumber.ifBlank { contact.appPeerId }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = TrCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { onToggleFavorite(contact) }) {
                                Icon(Icons.Rounded.Star, contentDescription = "Unfavorite", tint = Color(0xFFFBBF24))
                            }
                            if (contact.appPeerId.isNotBlank()) {
                                IconButton(onClick = { onCallContact(contact, CallingMode.APP_TO_APP) }) {
                                    Icon(Icons.Rounded.Hub, contentDescription = "App-to-App Call", tint = TrGreen)
                                }
                            }
                            IconButton(onClick = { onCallContact(contact, CallingMode.SIP_TO_SIP) }) {
                                Icon(Icons.Rounded.Call, contentDescription = "SIP Call", tint = TrBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactItemCard(
    contact: ContactEntity,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCallSip: () -> Unit,
    onCallApp: () -> Unit
) {
    TelecomCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(name = contact.name, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TrTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (contact.phoneNumber.isNotBlank()) {
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = TrTextSecondary
                    )
                }
                if (contact.sipUri.isNotBlank()) {
                    Text(
                        text = "SIP: ${contact.sipUri}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (contact.appPeerId.isNotBlank()) {
                    Text(
                        text = "Peer: ${contact.appPeerId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (contact.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (contact.isFavorite) Color(0xFFFBBF24) else TrTextSecondary
                )
            }
            IconButton(onClick = onCallSip) {
                Icon(Icons.Rounded.Call, contentDescription = "Call SIP", tint = TrBlue)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (contact.appPeerId.isNotBlank()) {
                TextButton(onClick = onCallApp) {
                    Icon(Icons.Rounded.Hub, contentDescription = null, tint = TrGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.mode_app_to_app), color = TrGreen)
                }
            }
            TextButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = null, tint = TrCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.action_edit), color = TrCyan)
            }
            TextButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = TrRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.action_delete), color = TrRed)
            }
        }
    }
}

@Composable
private fun ContactEditorDialog(
    initial: ContactEntity?,
    onDismiss: () -> Unit,
    onSave: (ContactEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phoneNumber ?: "") }
    var sipUri by remember { mutableStateOf(initial?.sipUri ?: "") }
    var peerId by remember { mutableStateOf(initial?.appPeerId ?: "") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TrSurface,
        title = {
            Text(
                text = stringResource(if (initial == null) R.string.add_contact else R.string.edit_contact),
                color = TrTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text(stringResource(R.string.field_contact_name)) },
                    isError = nameError,
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("contact_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.field_phone_number)) },
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("contact_phone_input")
                )
                OutlinedTextField(
                    value = sipUri,
                    onValueChange = { sipUri = it },
                    label = { Text(stringResource(R.string.field_sip_uri)) },
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("contact_sip_input")
                )
                OutlinedTextField(
                    value = peerId,
                    onValueChange = { peerId = it },
                    label = { Text(stringResource(R.string.field_app_peer_id)) },
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("contact_peer_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onSave(
                            ContactEntity(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                phoneNumber = phone.trim(),
                                sipUri = sipUri.trim(),
                                appPeerId = peerId.trim(),
                                isFavorite = initial?.isFavorite ?: false
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TrBlue),
                modifier = Modifier.testTag("contact_save_button")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TrTextSecondary)
            }
        }
    )
}
