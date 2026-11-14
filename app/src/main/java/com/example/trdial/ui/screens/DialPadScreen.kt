package com.example.trdial.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.SettingsInputAntenna
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.trdial.calling.CallingMode
import com.example.trdial.data.local.SipAccountEntity
import com.example.trdial.viewmodel.DialerNotice
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TrBackground
import com.example.ui.theme.TrBlue
import com.example.ui.theme.TrBorder
import com.example.ui.theme.TrCard
import com.example.ui.theme.TrCyan
import com.example.ui.theme.TrGreen
import com.example.ui.theme.TrSurface
import com.example.ui.theme.TrTextPrimary
import com.example.ui.theme.TrTextSecondary

private data class DialKey(val digit: String, val letters: String)

private val DIAL_KEYS = listOf(
    DialKey("1", ""),
    DialKey("2", "ABC"),
    DialKey("3", "DEF"),
    DialKey("4", "GHI"),
    DialKey("5", "JKL"),
    DialKey("6", "MNO"),
    DialKey("7", "PQRS"),
    DialKey("8", "TUV"),
    DialKey("9", "WXYZ"),
    DialKey("*", ""),
    DialKey("0", "+"),
    DialKey("#", "")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialPadScreen(
    dialInput: String,
    callingMode: CallingMode,
    sipAccounts: List<SipAccountEntity>,
    selectedSipAccountId: Long?,
    notice: DialerNotice?,
    onInputChange: (String) -> Unit,
    onAppendDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearInput: () -> Unit,
    onSelectMode: (CallingMode) -> Unit,
    onSelectSipAccount: (Long) -> Unit,
    onCallClick: () -> Unit,
    onDismissNotice: () -> Unit,
    onOpenSipConfig: () -> Unit,
    onPreviewActiveCall: () -> Unit,
    onPreviewIncomingCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAccountMenu by remember { mutableStateOf(false) }
    var showMicPermissionBanner by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        showMicPermissionBanner = !granted
        if (granted) {
            onCallClick()
        }
    }

    val selectedAccount = sipAccounts.firstOrNull { it.id == selectedSipAccountId }
        ?: sipAccounts.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Calling Mode Selector (App-to-App vs SIP-to-SIP)
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = TrSurface,
            border = BorderStroke(1.dp, TrBorder)
        ) {
            Row(
                modifier = Modifier.padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModeToggleChip(
                    label = stringResource(R.string.mode_app_to_app),
                    icon = Icons.Rounded.Hub,
                    selected = callingMode == CallingMode.APP_TO_APP,
                    accent = TrGreen,
                    onClick = { onSelectMode(CallingMode.APP_TO_APP) },
                    modifier = Modifier.weight(1f).testTag("mode_app_to_app")
                )
                ModeToggleChip(
                    label = stringResource(R.string.mode_sip_to_sip),
                    icon = Icons.Rounded.SettingsInputAntenna,
                    selected = callingMode == CallingMode.SIP_TO_SIP,
                    accent = TrCyan,
                    onClick = { onSelectMode(CallingMode.SIP_TO_SIP) },
                    modifier = Modifier.weight(1f).testTag("mode_sip_to_sip")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SIP Account Selector Row
        if (callingMode == CallingMode.SIP_TO_SIP) {
            Box(modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TrCard,
                    border = BorderStroke(1.dp, TrBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (sipAccounts.isEmpty()) onOpenSipConfig()
                            else showAccountMenu = true
                        }
                        .testTag("dialpad_sip_account_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.select_sip_account),
                                style = MaterialTheme.typography.labelMedium,
                                color = TrTextSecondary
                            )
                            Text(
                                text = selectedAccount?.let { "${it.displayName} (${it.username}@${it.domain})" }
                                    ?: stringResource(R.string.no_sip_account),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedAccount != null) TrTextPrimary else TrCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Choose SIP account",
                            tint = TrCyan
                        )
                    }
                }

                DropdownMenu(
                    expanded = showAccountMenu,
                    onDismissRequest = { showAccountMenu = false }
                ) {
                    sipAccounts.forEach { account ->
                        DropdownMenuItem(
                            text = {
                                Text("${account.displayName} • ${account.username}@${account.domain}")
                            },
                            onClick = {
                                onSelectSipAccount(account.id)
                                showAccountMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ ${stringResource(R.string.configure_sip_now)}", color = TrCyan) },
                        onClick = {
                            showAccountMenu = false
                            onOpenSipConfig()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Mic Permission Notice (if missing)
        if (showMicPermissionBanner) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = TrBlue.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, TrBlue.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = null, tint = TrCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.mic_permission_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    ) {
                        Text(stringResource(R.string.btn_grant_permission), color = TrCyan)
                    }
                }
            }
        }

        // Number Input Field
        OutlinedTextField(
            value = dialInput,
            onValueChange = onInputChange,
            placeholder = {
                Text(
                    text = stringResource(
                        if (callingMode == CallingMode.SIP_TO_SIP) R.string.dial_hint_sip else R.string.dial_hint_app
                    ),
                    color = TrTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrTextPrimary,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            trailingIcon = {
                if (dialInput.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = onBackspace,
                                onLongClick = onClearInput
                            )
                            .testTag("dialpad_backspace"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                            contentDescription = "Backspace",
                            tint = TrTextSecondary
                        )
                    }
                }
            },
            colors = trTextFieldColors(),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .testTag("dialpad_number_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 0-9 + * + # Numeric Keypad
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DIAL_KEYS.chunked(3).forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
                ) {
                    rowKeys.forEach { key ->
                        DialKeyButton(
                            digit = key.digit,
                            letters = key.letters,
                            onClick = { onAppendDigit(key.digit) },
                            onLongClick = if (key.digit == "0") {
                                { onAppendDigit("+") }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Call Action Button
        Button(
            onClick = {
                val hasMic = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasMic) {
                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    onCallClick()
                }
            },
            modifier = Modifier
                .widthIn(max = 260.dp)
                .fillMaxWidth()
                .height(58.dp)
                .testTag("dialpad_call_button"),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (callingMode == CallingMode.APP_TO_APP) TrGreen else TrBlue
            )
        ) {
            Icon(Icons.Rounded.Call, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.action_call),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }

    if (notice != null) {
        AlertDialog(
            onDismissRequest = onDismissNotice,
            containerColor = TrSurface,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = notice.title,
                    color = TrTextPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = notice.message,
                        color = TrTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (notice.mode == CallingMode.SIP_TO_SIP) {
                        OutlinedButton(
                            onClick = {
                                onDismissNotice()
                                onOpenSipConfig()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.configure_sip_now), color = TrCyan)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismissNotice()
                                onPreviewIncomingCall()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Incoming UI", color = TrTextPrimary, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                onDismissNotice()
                                onPreviewActiveCall()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Active UI", color = TrTextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissNotice) {
                    Text(stringResource(R.string.action_close), color = TrCyan)
                }
            }
        )
    }
}

@Composable
private fun ModeToggleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.2f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, accent.copy(alpha = 0.55f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accent else TrTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) TrTextPrimary else TrTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialKeyButton(
    digit: String,
    letters: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("dial_key_$digit"),
        shape = RoundedCornerShape(18.dp),
        color = TrCard,
        border = BorderStroke(1.dp, TrBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TrTextPrimary
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall,
                    color = TrCyan,
                    fontSize = 10.sp
                )
            }
        }
    }
}
