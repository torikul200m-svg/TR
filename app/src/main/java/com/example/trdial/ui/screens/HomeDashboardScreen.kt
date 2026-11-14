package com.example.trdial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallMissed
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsInputAntenna
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.trdial.calling.CallingMode
import com.example.trdial.calling.SipRegistrationState
import com.example.trdial.data.local.CallHistoryEntity
import com.example.trdial.data.local.ContactEntity
import com.example.trdial.data.local.SipAccountEntity
import com.example.trdial.data.local.UserProfileEntity
import com.example.trdial.ui.components.ContactAvatar
import com.example.trdial.ui.components.EngineBadgeChip
import com.example.trdial.ui.components.SectionHeader
import com.example.trdial.ui.components.StatusPill
import com.example.trdial.ui.components.TelecomCard
import com.example.trdial.ui.components.TrDialLogoBadge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TrBackground
import com.example.ui.theme.TrBlue
import com.example.ui.theme.TrCard
import com.example.ui.theme.TrCyan
import com.example.ui.theme.TrGreen
import com.example.ui.theme.TrRed
import com.example.ui.theme.TrSurface
import com.example.ui.theme.TrTextPrimary
import com.example.ui.theme.TrTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeDashboardScreen(
    currentUser: UserProfileEntity?,
    isOnline: Boolean,
    sipAccounts: List<SipAccountEntity>,
    sipRegistrationState: SipRegistrationState,
    favorites: List<ContactEntity>,
    recentCalls: List<CallHistoryEntity>,
    onOpenDialPad: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSipManager: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onQuickCallContact: (ContactEntity) -> Unit,
    onRedialHistory: (CallHistoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with TR Dial sip logo & Settings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrDialLogoBadge(size = 46.dp, showWordmark = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        label = stringResource(
                            if (isOnline) R.string.status_network_online else R.string.status_network_offline
                        ),
                        dotColor = if (isOnline) TrGreen else TrRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = TrTextPrimary
                        )
                    }
                }
            }
        }

        // User Profile & SIP Status Hero Card
        item {
            TelecomCard(
                onClick = onOpenProfile,
                containerColor = TrSurface,
                modifier = Modifier.testTag("dashboard_profile_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(
                        name = currentUser?.fullName ?: "TR User",
                        size = 52.dp,
                        accentColor = TrBlue
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.fullName ?: stringResource(R.string.dashboard_greeting),
                            style = MaterialTheme.typography.titleLarge,
                            color = TrTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentUser != null) {
                                "Peer ID: ${currentUser.appPeerId}"
                            } else {
                                stringResource(R.string.app_tagline)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFamily,
                            color = TrCyan
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TrBlue.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = stringResource(R.string.card_profile),
                            style = MaterialTheme.typography.labelMedium,
                            color = TrCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SIP Registration Status Strip
                val sipStatusLabel = when (sipRegistrationState) {
                    is SipRegistrationState.Registered -> "SIP: ${sipRegistrationState.sipUri}"
                    is SipRegistrationState.Registering -> "Registering ${sipRegistrationState.sipUri}…"
                    is SipRegistrationState.Error -> stringResource(R.string.status_sip_not_configured)
                    SipRegistrationState.NotConfigured, SipRegistrationState.Unregistered -> {
                        if (sipAccounts.isEmpty()) stringResource(R.string.status_sip_not_configured)
                        else stringResource(R.string.status_sip_ready_accounts, sipAccounts.size)
                    }
                }
                val sipDotColor = when (sipRegistrationState) {
                    is SipRegistrationState.Registered -> TrGreen
                    is SipRegistrationState.Registering -> TrCyan
                    is SipRegistrationState.Error -> TrRed
                    else -> if (sipAccounts.isEmpty()) TrRed else TrCyan
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TrCard)
                        .clickable(onClick = onOpenSipManager)
                        .padding(12.dp)
                        .testTag("dashboard_sip_status_card"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Rounded.SettingsInputAntenna,
                            contentDescription = null,
                            tint = sipDotColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sipStatusLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = stringResource(R.string.card_sip_accounts),
                        style = MaterialTheme.typography.labelMedium,
                        color = TrCyan
                    )
                }
            }
        }

        // Quick Dial CTA Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenDialPad)
                    .testTag("dashboard_quick_dial_card"),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1D4ED8), Color(0xFF0E7490))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.section_quick_dial),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stringResource(R.string.mode_app_to_app)} • ${stringResource(R.string.mode_sip_to_sip)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Dialpad,
                                    contentDescription = stringResource(R.string.nav_dial_pad),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Workspace Navigation Grid (Contacts, Favorites, SIP Accounts, Settings)
        item {
            SectionHeader(title = stringResource(R.string.section_quick_actions))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardTile(
                    icon = Icons.Rounded.Contacts,
                    label = stringResource(R.string.nav_contacts),
                    accent = TrBlue,
                    onClick = onOpenContacts,
                    modifier = Modifier.weight(1f).testTag("tile_contacts")
                )
                DashboardTile(
                    icon = Icons.Rounded.Star,
                    label = stringResource(R.string.nav_favorites),
                    accent = TrCyan,
                    onClick = onOpenFavorites,
                    modifier = Modifier.weight(1f).testTag("tile_favorites")
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardTile(
                    icon = Icons.Rounded.SettingsInputAntenna,
                    label = stringResource(R.string.card_sip_accounts),
                    accent = TrGreen,
                    onClick = onOpenSipManager,
                    modifier = Modifier.weight(1f).testTag("tile_sip_accounts")
                )
                DashboardTile(
                    icon = Icons.Rounded.Settings,
                    label = stringResource(R.string.nav_settings),
                    accent = Color(0xFF818CF8),
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f).testTag("tile_settings")
                )
            }
        }

        // Favorites Quick Strip
        if (favorites.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.nav_favorites),
                    actionText = stringResource(R.string.view_all),
                    onActionClick = onOpenFavorites
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favorites, key = { it.id }) { contact ->
                        TelecomCard(
                            onClick = { onQuickCallContact(contact) },
                            modifier = Modifier.width(148.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ContactAvatar(name = contact.name, size = 36.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TrTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = contact.sipUri.ifBlank { contact.phoneNumber.ifBlank { contact.appPeerId } },
                                style = MaterialTheme.typography.labelSmall,
                                color = TrTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Recent Calls Preview
        item {
            SectionHeader(
                title = stringResource(R.string.section_recent_calls),
                actionText = stringResource(R.string.view_all),
                onActionClick = onOpenHistory
            )
        }

        if (recentCalls.isEmpty()) {
            item {
                TelecomCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = TrTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.empty_recent_calls),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrTextSecondary
                        )
                    }
                }
            }
        } else {
            items(recentCalls, key = { it.id }) { entry ->
                RecentCallRow(entry = entry, onRedial = { onRedialHistory(entry) })
            }
        }
    }
}

@Composable
private fun DashboardTile(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TelecomCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TrTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentCallRow(
    entry: CallHistoryEntity,
    onRedial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val directionIcon = when (entry.direction) {
        "MISSED" -> Icons.Rounded.CallMissed
        "INCOMING" -> Icons.Rounded.CallReceived
        else -> Icons.Rounded.CallMade
    }
    val directionTint = when {
        entry.direction == "MISSED" -> TrRed
        entry.status == "UNCONFIGURED" -> Color(0xFFF59E0B)
        else -> TrGreen
    }
    val timeText = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))

    TelecomCard(
        onClick = onRedial,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = directionIcon,
                contentDescription = entry.direction,
                tint = directionTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.contactName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TrTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    EngineBadgeChip(mode = entry.callingMode)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.targetAddress} • ${entry.status} • $timeText",
                    style = MaterialTheme.typography.labelSmall,
                    color = TrTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRedial) {
                Icon(
                    imageVector = Icons.Rounded.Phone,
                    contentDescription = stringResource(R.string.action_call),
                    tint = TrCyan
                )
            }
        }
    }
}
