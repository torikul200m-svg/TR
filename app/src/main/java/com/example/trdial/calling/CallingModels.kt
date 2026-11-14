package com.example.trdial.calling

enum class CallingMode(val storageKey: String) {
    APP_TO_APP("APP_TO_APP"),
    SIP_TO_SIP("SIP_TO_SIP");

    companion object {
        fun fromKey(key: String): CallingMode = entries.firstOrNull { it.storageKey == key } ?: SIP_TO_SIP
    }
}

enum class SipTransport {
    UDP, TCP, TLS
}

sealed interface SipRegistrationState {
    data object NotConfigured : SipRegistrationState
    data object Unregistered : SipRegistrationState
    data class Registering(val accountId: Long, val sipUri: String) : SipRegistrationState
    data class Registered(val accountId: Long, val sipUri: String, val expiresInSeconds: Int) : SipRegistrationState
    data class Error(val accountId: Long, val reason: String) : SipRegistrationState
}

sealed interface SignalingConnectionState {
    data object NotConfigured : SignalingConnectionState
    data object Disconnected : SignalingConnectionState
    data class Connecting(val endpoint: String) : SignalingConnectionState
    data class Connected(val localPeerId: String) : SignalingConnectionState
    data class Error(val reason: String) : SignalingConnectionState
}

sealed interface CallDispatchResult {
    data class Dispatched(val callId: String) : CallDispatchResult
    data class NotConfigured(
        val mode: CallingMode,
        val title: String,
        val message: String
    ) : CallDispatchResult
    data class ValidationError(val message: String) : CallDispatchResult
}

data class SipEngineConfig(
    val userAgent: String = "TR-Dial-SIP/1.0",
    val enableEchoCancellation: Boolean = true,
    val enableNoiseSuppression: Boolean = true,
    val stunServer: String? = null
)

data class SipAccountCredentials(
    val accountId: Long,
    val displayName: String,
    val username: String,
    val domain: String,
    val port: Int,
    val transport: SipTransport,
    val outboundProxy: String?,
    val decryptedPassword: String
)

data class IncomingCallEvent(
    val callId: String,
    val callerName: String,
    val callerAddress: String,
    val mode: CallingMode,
    val timestamp: Long = System.currentTimeMillis()
)

data class ActiveCallUiSession(
    val callId: String,
    val remoteName: String,
    val remoteAddress: String,
    val callingMode: CallingMode,
    val isPreviewOnly: Boolean = true,
    val statusText: String = "UI Preview — Calling Engine Not Configured",
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isOnHold: Boolean = false,
    val lastDtmfDigit: Char? = null,
    val elapsedSeconds: Long = 0L
)
