package com.example.trdial.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.RingVolume
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.trdial.data.local.SipAccountEntity
import com.example.trdial.data.local.UserProfileEntity
import com.example.trdial.data.local.UserSettings
import com.example.trdial.ui.components.ContactAvatar
import com.example.trdial.ui.components.StatusPill
import com.example.trdial.ui.components.TelecomCard
import com.example.trdial.ui.components.TrDialLogoBadge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TrBackground
import com.example.ui.theme.TrBlue
import com.example.ui.theme.TrBorder
import com.example.ui.theme.TrCard
import com.example.ui.theme.TrCyan
import com.example.ui.theme.TrGreen
import com.example.ui.theme.TrRed
import com.example.ui.theme.TrSurface
import com.example.ui.theme.TrTextPrimary
import com.example.ui.theme.TrTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserProfileEntity?,
    sipAccounts: List<SipAccountEntity>,
    onBack: () -> Unit,
    onUpdateProfile: (fullName: String, extension: String) -> Unit,
    onOpenSipManager: () -> Unit,
    onSignOut: () -> Unit
) {
    BackHandler { onBack() }

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TrBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title), color = TrTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = TrTextPrimary)
                    }
                },
                actions = {
                    if (currentUser != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.action_edit), tint = TrCyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TelecomCard(modifier = Modifier.widthIn(max = 560.dp), containerColor = TrSurface) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ContactAvatar(
                        name = currentUser?.fullName ?: "TR User",
                        size = 64.dp,
                        accentColor = TrCyan
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = currentUser?.fullName ?: "TR Dial sip Operator",
                            style = MaterialTheme.typography.titleLarge,
                            color = TrTextPrimary
                        )
                        Text(
                            text = currentUser?.email ?: "local@trdial.sip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StatusPill(
                            label = "${stringResource(R.string.profile_peer_id)}: ${currentUser?.appPeerId ?: "TR-LOCAL"}",
                            dotColor = TrGreen
                        )
                    }
                }
            }

            TelecomCard(modifier = Modifier.widthIn(max = 560.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_security_shield),
                        contentDescription = "Local security shield",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.profile_security_status),
                            style = MaterialTheme.typography.titleMedium,
                            color = TrTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.profile_security_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrTextSecondary
                        )
                    }
                }
            }

            TelecomCard(
                onClick = onOpenSipManager,
                modifier = Modifier.widthIn(max = 560.dp)
            ) {
                Text(
                    text = stringResource(R.string.card_sip_accounts),
                    style = MaterialTheme.typography.titleMedium,
                    color = TrTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${sipAccounts.size} SIP account(s) stored securely",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrCyan
                )
            }

            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_sign_out_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrRed)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_sign_out))
            }
        }
    }

    if (showEditDialog && currentUser != null) {
        var name by remember { mutableStateOf(currentUser.fullName) }
        var ext by remember { mutableStateOf(currentUser.phoneExtension) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = TrSurface,
            title = { Text(stringResource(R.string.action_edit), color = TrTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.field_full_name)) },
                        colors = trTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ext,
                        onValueChange = { ext = it },
                        label = { Text(stringResource(R.string.field_extension)) },
                        colors = trTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(name, ext)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrBlue)
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = TrTextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBack: (() -> Unit)? = null,
    onLanguageSelected: (String) -> Unit,
    onPreferredModeSelected: (String) -> Unit,
    onEchoCancellationChanged: (Boolean) -> Unit,
    onNoiseSuppressionChanged: (Boolean) -> Unit,
    onProximityChanged: (Boolean) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit,
    onPreviewIncomingCall: () -> Unit,
    onPreviewActiveCall: () -> Unit,
    onOpenAboutDeveloper: () -> Unit
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> micGranted = granted }

    Scaffold(
        containerColor = TrBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = TrTextPrimary) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back")) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = TrTextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Language Selector (English, Bengali, Arabic RTL)
            TelecomCard(modifier = Modifier.widthIn(max = 580.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Language, contentDescription = null, tint = TrCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_language),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                val languages = listOf(
                    "en" to stringResource(R.string.lang_english),
                    "bn" to stringResource(R.string.lang_bengali),
                    "ar" to stringResource(R.string.lang_arabic)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    languages.forEach { (code, label) ->
                        val selected = settings.languageCode == code
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onLanguageSelected(code) }
                                .testTag("settings_lang_$code"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) TrBlue.copy(alpha = 0.22f) else TrSurface,
                            border = BorderStroke(1.dp, if (selected) TrCyan else TrBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, color = TrTextPrimary, style = MaterialTheme.typography.bodyLarge)
                                if (selected) {
                                    StatusPill(label = code.uppercase(), dotColor = TrCyan)
                                }
                            }
                        }
                    }
                }
            }

            // Calling Preferences (DataStore)
            TelecomCard(modifier = Modifier.widthIn(max = 580.dp)) {
                Text(
                    text = stringResource(R.string.settings_calling_prefs),
                    style = MaterialTheme.typography.titleMedium,
                    color = TrTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.settings_default_mode),
                    style = MaterialTheme.typography.labelLarge,
                    color = TrTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "APP_TO_APP" to stringResource(R.string.mode_app_to_app),
                        "SIP_TO_SIP" to stringResource(R.string.mode_sip_to_sip)
                    ).forEach { (modeKey, modeLabel) ->
                        val selected = settings.preferredCallingMode == modeKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onPreferredModeSelected(modeKey) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) TrBlue.copy(alpha = 0.22f) else TrSurface,
                            border = BorderStroke(1.dp, if (selected) TrCyan else TrBorder)
                        ) {
                            Text(
                                text = modeLabel,
                                color = if (selected) TrCyan else TrTextSecondary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                SettingToggleRow(
                    label = stringResource(R.string.settings_echo_cancel),
                    checked = settings.echoCancellation,
                    onCheckedChange = onEchoCancellationChanged
                )
                SettingToggleRow(
                    label = stringResource(R.string.settings_noise_suppress),
                    checked = settings.noiseSuppression,
                    onCheckedChange = onNoiseSuppressionChanged
                )
                SettingToggleRow(
                    label = stringResource(R.string.settings_proximity),
                    checked = settings.autoProximitySpeaker,
                    onCheckedChange = onProximityChanged
                )
                SettingToggleRow(
                    label = stringResource(R.string.settings_wifi_only),
                    checked = settings.wifiOnlyCalls,
                    onCheckedChange = onWifiOnlyChanged
                )
            }

            // Permissions & Call UI Preview Diagnostics
            TelecomCard(modifier = Modifier.widthIn(max = 580.dp)) {
                Text(
                    text = stringResource(R.string.settings_permissions),
                    style = MaterialTheme.typography.titleMedium,
                    color = TrTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = null,
                            tint = if (micGranted) TrGreen else TrRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(if (micGranted) R.string.perm_mic_granted else R.string.perm_mic_denied),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrTextPrimary
                        )
                    }
                    if (!micGranted) {
                        TextButton(onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                            Text(stringResource(R.string.btn_grant_permission), color = TrCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPreviewIncomingCall,
                        modifier = Modifier.weight(1f).testTag("settings_preview_incoming")
                    ) {
                        Icon(Icons.Rounded.RingVolume, contentDescription = null, tint = TrCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_preview_incoming_call), color = TrTextPrimary)
                    }
                    OutlinedButton(
                        onClick = onPreviewActiveCall,
                        modifier = Modifier.weight(1f).testTag("settings_preview_active")
                    ) {
                        Icon(Icons.Rounded.PhoneInTalk, contentDescription = null, tint = TrGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_preview_active_call), color = TrTextPrimary)
                    }
                }
            }

            // About Developer Link
            TelecomCard(
                onClick = onOpenAboutDeveloper,
                modifier = Modifier.widthIn(max = 580.dp).testTag("settings_about_developer")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = TrCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.settings_about_dev),
                                style = MaterialTheme.typography.titleMedium,
                                color = TrTextPrimary
                            )
                            Text(
                                text = stringResource(R.string.developer_credit),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TrCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TrTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TrBlue,
                uncheckedTrackColor = TrCard
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDeveloperScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val phases = listOf(
        "Phase 1: Project foundation, TR branding & 16-screen dark Compose UI",
        "Phase 2: Room Database, DataStore & salted PBKDF2/AES-GCM auth",
        "Phase 3: Contacts, Favorites & Call History management",
        "Phase 4: Modular SipEngine interface prepared for native PJSIP C/C++",
        "Phase 5: Modular AppToAppCallEngine interface prepared for WebRTC",
        "Phase 6: Incoming & Active Call UI + notification architecture",
        "Phase 7: Runtime permissions, validation & Robolectric verification",
        "Phase 8: APK export & native NDK integration checklist"
    )

    val remainingIntegrations = listOf(
        "1. Compile native PJSIP (pjsua2 C/C++ via Android NDK) and bind SipEngine implementation.",
        "2. Connect WebRTC PeerConnectionFactory + WebSocket/FCM signaling to AppToAppCallEngine.",
        "3. Wire Android Telecom ConnectionService & CallStyle notifications for background ring/lockscreen handling."
    )

    Scaffold(
        containerColor = TrBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title), color = TrTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("about_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = TrTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TelecomCard(modifier = Modifier.widthIn(max = 580.dp), containerColor = TrSurface) {
                TrDialLogoBadge(size = 58.dp, showWordmark = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.about_dev_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TrCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.about_dev_role),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TrTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = "v1.0 • Native Kotlin & Jetpack Compose", dotColor = TrGreen)
            }

            TelecomCard(modifier = Modifier.widthIn(max = 580.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Code, contentDescription = null, tint = TrCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.about_phases_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                phases.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrTextSecondary,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }

            TelecomCard(modifier = Modifier.widthIn(max = 580.dp)) {
                Text(
                    text = stringResource(R.string.about_remaining_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = TrTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                remainingIntegrations.forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = JetBrainsMonoFamily,
                        color = TrCyan,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
