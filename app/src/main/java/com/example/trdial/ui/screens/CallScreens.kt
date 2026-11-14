package com.example.trdial.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.trdial.calling.ActiveCallUiSession
import com.example.trdial.calling.IncomingCallEvent
import com.example.trdial.ui.components.ContactAvatar
import com.example.trdial.ui.components.EngineBadgeChip
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TrBackground
import com.example.ui.theme.TrBorder
import com.example.ui.theme.TrCard
import com.example.ui.theme.TrCyan
import com.example.ui.theme.TrGreen
import com.example.ui.theme.TrRed
import com.example.ui.theme.TrTextPrimary
import com.example.ui.theme.TrTextSecondary

@Composable
fun IncomingCallScreen(
    incomingCall: IncomingCallEvent?,
    onAnswer: () -> Unit,
    onReject: () -> Unit
) {
    BackHandler { onReject() }

    val callerName = incomingCall?.callerName ?: "Tariqul Telecom Lab"
    val callerAddress = incomingCall?.callerAddress ?: "sip:1001@voip.trdial.local"
    val modeKey = incomingCall?.mode?.storageKey ?: "SIP_TO_SIP"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F203D), TrBackground)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CallPreviewNoticeBanner()
            Spacer(modifier = Modifier.height(18.dp))
            EngineBadgeChip(mode = modeKey)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.incoming_call_label),
                style = MaterialTheme.typography.titleMedium,
                color = TrCyan
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ContactAvatar(name = callerName, size = 104.dp, accentColor = TrCyan)
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineLarge,
                color = TrTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = callerAddress,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = JetBrainsMonoFamily,
                color = TrTextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = TrRed,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onReject)
                        .testTag("incoming_reject_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CallEnd,
                            contentDescription = stringResource(R.string.btn_reject),
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.btn_reject), color = TrTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = TrGreen,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAnswer)
                        .testTag("incoming_answer_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = stringResource(R.string.btn_answer),
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.btn_answer), color = TrTextPrimary)
            }
        }
    }
}

@Composable
fun ActiveCallScreen(
    session: ActiveCallUiSession?,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onSendDtmf: (Char) -> Unit,
    onEndCall: () -> Unit
) {
    BackHandler { onEndCall() }

    var showDtmfPad by remember { mutableStateOf(false) }

    val remoteName = session?.remoteName ?: "Tariqul Telecom Lab"
    val remoteAddress = session?.remoteAddress ?: "sip:1001@voip.trdial.local"
    val modeKey = session?.callingMode?.storageKey ?: "SIP_TO_SIP"
    val isMuted = session?.isMuted ?: false
    val isSpeaker = session?.isSpeakerOn ?: false
    val isOnHold = session?.isOnHold ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E1B36), TrBackground)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CallPreviewNoticeBanner()
            Spacer(modifier = Modifier.height(14.dp))
            EngineBadgeChip(mode = modeKey)
            Spacer(modifier = Modifier.height(12.dp))
            ContactAvatar(name = remoteName, size = 84.dp, accentColor = TrCyan)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = remoteName,
                style = MaterialTheme.typography.headlineMedium,
                color = TrTextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = remoteAddress,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = JetBrainsMonoFamily,
                color = TrTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (isOnHold) R.string.call_state_on_hold else R.string.call_state_standby
                ),
                style = MaterialTheme.typography.labelLarge,
                color = if (isOnHold) Color(0xFFF59E0B) else TrCyan
            )
            session?.lastDtmfDigit?.let { digit ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.dtmf_last_tone, digit.toString()),
                    style = MaterialTheme.typography.labelSmall,
                    color = TrGreen
                )
            }
        }

        AnimatedVisibility(visible = showDtmfPad) {
            val dtmfRows = listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9'),
                listOf('*', '0', '#')
            )
            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dtmfRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { ch ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSendDtmf(ch) }
                                    .testTag("dtmf_key_$ch"),
                                shape = RoundedCornerShape(12.dp),
                                color = TrCard,
                                border = BorderStroke(1.dp, TrBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = ch.toString(),
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TrTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallControlToggle(
                    icon = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                    label = stringResource(R.string.btn_mute),
                    active = isMuted,
                    onClick = onToggleMute,
                    modifier = Modifier.testTag("active_call_mute")
                )
                CallControlToggle(
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    label = stringResource(R.string.btn_speaker),
                    active = isSpeaker,
                    onClick = onToggleSpeaker,
                    modifier = Modifier.testTag("active_call_speaker")
                )
                CallControlToggle(
                    icon = if (isOnHold) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    label = stringResource(R.string.btn_hold),
                    active = isOnHold,
                    onClick = onToggleHold,
                    modifier = Modifier.testTag("active_call_hold")
                )
                CallControlToggle(
                    icon = Icons.Rounded.Dialpad,
                    label = stringResource(R.string.btn_keypad),
                    active = showDtmfPad,
                    onClick = { showDtmfPad = !showDtmfPad },
                    modifier = Modifier.testTag("active_call_keypad")
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Surface(
                shape = CircleShape,
                color = TrRed,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEndCall)
                    .testTag("active_call_end_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CallEnd,
                        contentDescription = stringResource(R.string.btn_end_call),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.btn_end_call), color = TrTextSecondary)
        }
    }
}

@Composable
private fun CallPreviewNoticeBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF59E0B).copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.call_preview_banner),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFDE68A)
            )
        }
    }
}

@Composable
private fun CallControlToggle(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = if (active) TrCyan.copy(alpha = 0.25f) else TrCard,
            border = BorderStroke(1.dp, if (active) TrCyan else TrBorder),
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active) TrCyan else TrTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) TrCyan else TrTextSecondary
        )
    }
}
