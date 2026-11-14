package com.example.trdial.calling.apptoapp

import com.example.trdial.calling.CallDispatchResult
import com.example.trdial.calling.CallingMode
import com.example.trdial.calling.IncomingCallEvent
import com.example.trdial.calling.SignalingConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Modular App-to-App Internet Calling engine contract designed for future WebRTC + WebSocket/FCM signaling.
 */
interface AppToAppCallEngine {
    val connectionState: StateFlow<SignalingConnectionState>
    val isConfigured: Boolean

    suspend fun initialize(localPeerId: String): Result<Unit>
    suspend fun connectSignaling(endpointUrl: String): Result<SignalingConnectionState>
    suspend fun startCall(remotePeerId: String): CallDispatchResult
    fun observeIncomingCalls(): Flow<IncomingCallEvent>
    suspend fun acceptCall(callId: String): Result<Unit>
    suspend fun declineCall(callId: String): Result<Unit>
    suspend fun endCall(callId: String): Result<Unit>
    suspend fun setMuted(callId: String, muted: Boolean): Result<Unit>
    suspend fun setSpeakerEnabled(callId: String, enabled: Boolean): Result<Unit>
    suspend fun setHold(callId: String, onHold: Boolean): Result<Unit>
    suspend fun sendDtmf(callId: String, digit: Char): Result<Unit>
}

class UnconfiguredAppToAppEngine : AppToAppCallEngine {
    private val _connectionState = MutableStateFlow<SignalingConnectionState>(SignalingConnectionState.NotConfigured)
    override val connectionState: StateFlow<SignalingConnectionState> = _connectionState.asStateFlow()

    override val isConfigured: Boolean = false

    private val incomingCallsFlow = MutableSharedFlow<IncomingCallEvent>(extraBufferCapacity = 4)

    override suspend fun initialize(localPeerId: String): Result<Unit> {
        _connectionState.value = SignalingConnectionState.NotConfigured
        return Result.success(Unit)
    }

    override suspend fun connectSignaling(endpointUrl: String): Result<SignalingConnectionState> {
        val state = SignalingConnectionState.Error(
            "App-to-App WebRTC signaling server is not configured."
        )
        _connectionState.value = state
        return Result.success(state)
    }

    override suspend fun startCall(remotePeerId: String): CallDispatchResult {
        if (remotePeerId.isBlank()) {
            return CallDispatchResult.ValidationError("Enter a remote Peer ID or handle.")
        }
        return CallDispatchResult.NotConfigured(
            mode = CallingMode.APP_TO_APP,
            title = "Calling Service Not Configured",
            message = "App-to-App internet calling is not configured yet. Connect a WebRTC " +
                "signaling server and ICE/STUN/TURN transport before calling \"$remotePeerId\"."
        )
    }

    override fun observeIncomingCalls(): Flow<IncomingCallEvent> = incomingCallsFlow.asSharedFlow()

    override suspend fun acceptCall(callId: String): Result<Unit> =
        Result.failure(IllegalStateException("App-to-App signaling transport not configured."))

    override suspend fun declineCall(callId: String): Result<Unit> =
        Result.failure(IllegalStateException("App-to-App signaling transport not configured."))

    override suspend fun endCall(callId: String): Result<Unit> = Result.success(Unit)

    override suspend fun setMuted(callId: String, muted: Boolean): Result<Unit> = Result.success(Unit)

    override suspend fun setSpeakerEnabled(callId: String, enabled: Boolean): Result<Unit> = Result.success(Unit)

    override suspend fun setHold(callId: String, onHold: Boolean): Result<Unit> = Result.success(Unit)

    override suspend fun sendDtmf(callId: String, digit: Char): Result<Unit> = Result.success(Unit)
}
