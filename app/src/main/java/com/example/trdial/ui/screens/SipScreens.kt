package com.example.trdial.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SettingsInputAntenna
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.trdial.calling.SipRegistrationState
import com.example.trdial.data.local.SipAccountEntity
import com.example.trdial.ui.components.StatusPill
import com.example.trdial.ui.components.TelecomCard
import com.example.trdial.viewmodel.SipConfigStatus
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
fun SipAccountManagerScreen(
    accounts: List<SipAccountEntity>,
    registrationState: SipRegistrationState,
    configStatus: SipConfigStatus,
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (SipAccountEntity) -> Unit,
    onSetDefault: (Long) -> Unit,
    onTestRegistration: (SipAccountEntity) -> Unit,
    onDeleteAccount: (Long) -> Unit,
    onClearNotice: () -> Unit
) {
    BackHandler { onBack() }

    Scaffold(
        containerColor = TrBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sip_manager_title), color = TrTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sip_manager_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = TrTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccount,
                containerColor = TrBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("sip_manager_add_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_sip_account))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            configStatus.infoMessage?.let { info ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = info,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearNotice) {
                            Text(stringResource(R.string.action_close), color = TrCyan)
                        }
                    }
                }
            }

            if (accounts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.SettingsInputAntenna,
                            contentDescription = null,
                            tint = TrCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.empty_sip_accounts),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TrTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(accounts, key = { it.id }) { account ->
                        val isErrorThis = (registrationState as? SipRegistrationState.Error)?.accountId == account.id
                        TelecomCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = account.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TrTextPrimary
                                        )
                                        if (account.isDefault) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            StatusPill(
                                                label = stringResource(R.string.badge_default),
                                                dotColor = TrGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${account.username}@${account.domain}:${account.port} (${account.transport})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = JetBrainsMonoFamily,
                                        color = TrCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = TrGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isErrorThis) "Engine Not Compiled (Standby)" else "AES-GCM Encrypted Secret",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isErrorThis) Color(0xFFF59E0B) else TrTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!account.isDefault) {
                                    OutlinedButton(
                                        onClick = { onSetDefault(account.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = TrGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.btn_set_default), color = TrTextPrimary)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { onTestRegistration(account) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.btn_register_test), color = TrCyan)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onEditAccount(account) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = TrCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.action_edit), color = TrCyan)
                                }
                                TextButton(onClick = { onDeleteAccount(account.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = TrRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.action_delete), color = TrRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SipConfigurationScreen(
    editingAccount: SipAccountEntity?,
    configStatus: SipConfigStatus,
    onBack: () -> Unit,
    onSaveAccount: (
        editingId: Long,
        displayName: String,
        username: String,
        domain: String,
        portText: String,
        transport: String,
        outboundProxy: String,
        stunServer: String,
        password: String,
        makeDefault: Boolean
    ) -> Unit
) {
    BackHandler { onBack() }

    var displayName by remember(editingAccount) { mutableStateOf(editingAccount?.displayName ?: "") }
    var username by remember(editingAccount) { mutableStateOf(editingAccount?.username ?: "") }
    var domain by remember(editingAccount) { mutableStateOf(editingAccount?.domain ?: "") }
    var portText by remember(editingAccount) { mutableStateOf((editingAccount?.port ?: 5060).toString()) }
    var transport by remember(editingAccount) { mutableStateOf(editingAccount?.transport ?: "UDP") }
    var outboundProxy by remember(editingAccount) { mutableStateOf(editingAccount?.outboundProxy ?: "") }
    var stunServer by remember(editingAccount) { mutableStateOf(editingAccount?.stunServer ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var makeDefault by remember(editingAccount) { mutableStateOf(editingAccount?.isDefault ?: true) }

    Scaffold(
        containerColor = TrBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sip_config_title), color = TrTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sip_config_back")) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TelecomCard(
                modifier = Modifier.widthIn(max = 560.dp),
                containerColor = TrSurface
            ) {
                configStatus.errorMessage?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TrRed.copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(error, color = TrRed, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                    }
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(stringResource(R.string.field_display_name)) },
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("sip_display_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.field_sip_username)) },
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("sip_username_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text(stringResource(R.string.field_sip_domain)) },
                        singleLine = true,
                        colors = trTextFieldColors(),
                        modifier = Modifier.weight(2f).testTag("sip_domain_input")
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it.filter { ch -> ch.isDigit() }.take(5) },
                        label = { Text(stringResource(R.string.field_sip_port)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = trTextFieldColors(),
                        modifier = Modifier.weight(1f).testTag("sip_port_input")
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.field_transport),
                    style = MaterialTheme.typography.labelLarge,
                    color = TrTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UDP", "TCP", "TLS").forEach { proto ->
                        val selected = transport == proto
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    transport = proto
                                    if (proto == "TLS" && portText == "5060") portText = "5061"
                                    else if (proto != "TLS" && portText == "5061") portText = "5060"
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) TrBlue.copy(alpha = 0.25f) else TrCard,
                            border = BorderStroke(1.dp, if (selected) TrCyan else TrBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = proto,
                                    color = if (selected) TrCyan else TrTextSecondary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            if (editingAccount != null) "${stringResource(R.string.field_sip_password)} (Leave blank to keep)"
                            else stringResource(R.string.field_sip_password)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Toggle password",
                                tint = TrTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("sip_password_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = outboundProxy,
                    onValueChange = { outboundProxy = it },
                    label = { Text(stringResource(R.string.field_outbound_proxy)) },
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("sip_proxy_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = stunServer,
                    onValueChange = { stunServer = it },
                    label = { Text(stringResource(R.string.field_stun_server)) },
                    singleLine = true,
                    colors = trTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("sip_stun_input")
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { makeDefault = !makeDefault }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = makeDefault,
                        onCheckedChange = { makeDefault = it },
                        colors = CheckboxDefaults.colors(checkedColor = TrCyan)
                    )
                    Text(stringResource(R.string.btn_set_default), color = TrTextPrimary)
                }

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        onSaveAccount(
                            editingAccount?.id ?: 0L,
                            displayName,
                            username,
                            domain,
                            portText,
                            transport,
                            outboundProxy,
                            stunServer,
                            password,
                            makeDefault
                        )
                    },
                    enabled = !configStatus.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("sip_save_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TrBlue)
                ) {
                    Text(stringResource(R.string.btn_save_register))
                }
            }
        }
    }
}
