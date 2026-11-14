package com.example.trdial.calling

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.trdial.calling.apptoapp.AppToAppCallEngine
import com.example.trdial.calling.sip.SipEngine
import com.example.trdial.data.local.CallHistoryEntity
import com.example.trdial.data.repository.CallHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(checkCurrentlyOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = checkCurrentlyOnline()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            _isOnline.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    init {
        runCatching {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, callback)
        }
    }

    private fun checkCurrentlyOnline(): Boolean {
        val cm = connectivityManager ?: return false
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class CallCoordinator(
    private val sipEngine: SipEngine,
    private val appToAppEngine: AppToAppCallEngine,
    private val callHistoryRepository: CallHistoryRepository
) {
    private val _previewIncomingCall = MutableStateFlow<IncomingCallEvent?>(null)
    val previewIncomingCall: StateFlow<IncomingCallEvent?> = _previewIncomingCall.asStateFlow()

    private val _activeCallSession = MutableStateFlow<ActiveCallUiSession?>(null)
    val activeCallSession: StateFlow<ActiveCallUiSession?> = _activeCallSession.asStateFlow()

    suspend fun placeCall(
        targetAddress: String,
        contactName: String?,
        mode: CallingMode,
        sipAccountId: Long?
    ): CallDispatchResult {
        val trimmed = targetAddress.trim()
        if (trimmed.isEmpty()) {
            return CallDispatchResult.ValidationError("Please enter a destination number, SIP URI, or Peer ID.")
        }

        val result = when (mode) {
            CallingMode.SIP_TO_SIP -> {
                if (sipAccountId == null) {
                    CallDispatchResult.NotConfigured(
                        mode = CallingMode.SIP_TO_SIP,
                        title = "Calling Service Not Configured",
                        message = "No SIP account is selected and the native PJSIP engine is not yet compiled. Add a SIP account and integrate PJSIP to place live SIP calls."
                    )
                } else {
                    sipEngine.makeCall(trimmed, sipAccountId)
                }
            }
            CallingMode.APP_TO_APP -> appToAppEngine.startCall(trimmed)
        }

        val status = when (result) {
            is CallDispatchResult.Dispatched -> "INITIATED"
            is CallDispatchResult.NotConfigured -> "UNCONFIGURED"
            is CallDispatchResult.ValidationError -> return result
        }

        callHistoryRepository.logCall(
            CallHistoryEntity(
                contactName = contactName?.takeIf { it.isNotBlank() } ?: trimmed,
                targetAddress = trimmed,
                callingMode = mode.storageKey,
                direction = "OUTGOING",
                status = status,
                durationSeconds = 0
            )
        )

        return result
    }

    fun openIncomingCallPreview(
        callerName: String = "Tariqul Telecom Lab",
        callerAddress: String = "sip:1001@voip.trdial.local",
        mode: CallingMode = CallingMode.SIP_TO_SIP
    ) {
        _previewIncomingCall.value = IncomingCallEvent(
            callId = "preview-${UUID.randomUUID().toString().take(8)}",
            callerName = callerName,
            callerAddress = callerAddress,
            mode = mode
        )
    }

    fun openActiveCallPreview(
        remoteName: String = "Tariqul Telecom Lab",
        remoteAddress: String = "sip:1001@voip.trdial.local",
        mode: CallingMode = CallingMode.SIP_TO_SIP
    ) {
        _activeCallSession.value = ActiveCallUiSession(
            callId = "preview-${UUID.randomUUID().toString().take(8)}",
            remoteName = remoteName,
            remoteAddress = remoteAddress,
            callingMode = mode,
            isPreviewOnly = true
        )
    }

    suspend fun answerPreviewIncomingCall() {
        val incoming = _previewIncomingCall.value ?: return
        when (incoming.mode) {
            CallingMode.SIP_TO_SIP -> sipEngine.answerCall(incoming.callId)
            CallingMode.APP_TO_APP -> appToAppEngine.acceptCall(incoming.callId)
        }
        _previewIncomingCall.value = null
        _activeCallSession.value = ActiveCallUiSession(
            callId = incoming.callId,
            remoteName = incoming.callerName,
            remoteAddress = incoming.callerAddress,
            callingMode = incoming.mode,
            isPreviewOnly = true
        )
    }

    suspend fun rejectPreviewIncomingCall() {
        val incoming = _previewIncomingCall.value
        if (incoming != null) {
            when (incoming.mode) {
                CallingMode.SIP_TO_SIP -> sipEngine.rejectCall(incoming.callId)
                CallingMode.APP_TO_APP -> appToAppEngine.declineCall(incoming.callId)
            }
        }
        _previewIncomingCall.value = null
    }

    suspend fun toggleMute() {
        val session = _activeCallSession.value ?: return
        val next = !session.isMuted
        when (session.callingMode) {
            CallingMode.SIP_TO_SIP -> sipEngine.setMuted(session.callId, next)
            CallingMode.APP_TO_APP -> appToAppEngine.setMuted(session.callId, next)
        }
        _activeCallSession.update { it?.copy(isMuted = next) }
    }

    suspend fun toggleSpeaker() {
        val session = _activeCallSession.value ?: return
        val next = !session.isSpeakerOn
        when (session.callingMode) {
            CallingMode.SIP_TO_SIP -> sipEngine.setSpeakerEnabled(session.callId, next)
            CallingMode.APP_TO_APP -> appToAppEngine.setSpeakerEnabled(session.callId, next)
        }
        _activeCallSession.update { it?.copy(isSpeakerOn = next) }
    }

    suspend fun toggleHold() {
        val session = _activeCallSession.value ?: return
        val next = !session.isOnHold
        when (session.callingMode) {
            CallingMode.SIP_TO_SIP -> sipEngine.setHold(session.callId, next)
            CallingMode.APP_TO_APP -> appToAppEngine.setHold(session.callId, next)
        }
        _activeCallSession.update { it?.copy(isOnHold = next) }
    }

    suspend fun sendDtmf(digit: Char) {
        val session = _activeCallSession.value ?: return
        when (session.callingMode) {
            CallingMode.SIP_TO_SIP -> sipEngine.sendDtmf(session.callId, digit)
            CallingMode.APP_TO_APP -> appToAppEngine.sendDtmf(session.callId, digit)
        }
        _activeCallSession.update { it?.copy(lastDtmfDigit = digit) }
    }

    suspend fun endActiveCall() {
        val session = _activeCallSession.value
        if (session != null) {
            when (session.callingMode) {
                CallingMode.SIP_TO_SIP -> sipEngine.endCall(session.callId)
                CallingMode.APP_TO_APP -> appToAppEngine.endCall(session.callId)
            }
        }
        _activeCallSession.value = null
    }
}
