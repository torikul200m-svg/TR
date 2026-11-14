package com.example.trdial.calling.sip

import com.example.trdial.calling.CallDispatchResult
import com.example.trdial.calling.CallingMode
import com.example.trdial.calling.IncomingCallEvent
import com.example.trdial.calling.SipAccountCredentials
import com.example.trdial.calling.SipEngineConfig
import com.example.trdial.calling.SipRegistrationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Modular SIP engine contract designed for future native PJSIP (pjsua2 C/C++ NDK) integration.
 * Implementations MUST NOT fake successful live SIP registrations or audio calls.
 */
interface SipEngine {
    val registrationState: StateFlow<SipRegistrationState>
    val isEngineCompiled: Boolean

    suspend fun initialize(config: SipEngineConfig): Result<Unit>
    suspend fun registerAccount(credentials: SipAccountCredentials): Result<SipRegistrationState>
    suspend fun unregisterAccount(accountId: Long): Result<Unit>
    suspend fun removeAccount(accountId: Long): Result<Unit>
    suspend fun makeCall(targetUri: String, accountId: Long): CallDispatchResult
    fun observeIncomingCalls(): Flow<IncomingCallEvent>
    suspend fun answerCall(callId: String): Result<Unit>
    suspend fun rejectCall(callId: String): Result<Unit>
    suspend fun endCall(callId: String): Result<Unit>
    suspend fun setMuted(callId: String, muted: Boolean): Result<Unit>
    suspend fun setSpeakerEnabled(callId: String, enabled: Boolean): Result<Unit>
    suspend fun setHold(callId: String, onHold: Boolean): Result<Unit>
    suspend fun sendDtmf(callId: String, digit: Char): Result<Unit>
}

class UnconfiguredSipEngine : SipEngine {
    private val _registrationState = MutableStateFlow<SipRegistrationState>(SipRegistrationState.NotConfigured)
    override val registrationState: StateFlow<SipRegistrationState> = _registrationState.asStateFlow()

    override val isEngineCompiled: Boolean = false

    private val incomingCallsFlow = MutableSharedFlow<IncomingCallEvent>(extraBufferCapacity = 4)

    override suspend fun initialize(config: SipEngineConfig): Result<Unit> {
        _registrationState.value = SipRegistrationState.NotConfigured
        return Result.success(Unit)
    }

    override suspend fun registerAccount(credentials: SipAccountCredentials): Result<SipRegistrationState> {
        val message = "Native PJSIP library is not integrated in this build. " +
            "Credentials for ${credentials.username}@${credentials.domain} are saved securely, " +
            "but live SIP REGISTER packets cannot be sent yet."
        val state = SipRegistrationState.Error(accountId = credentials.accountId, reason = message)
        _registrationState.value = state
        return Result.success(state)
    }

    override suspend fun unregisterAccount(accountId: Long): Result<Unit> {
        _registrationState.value = SipRegistrationState.NotConfigured
        return Result.success(Unit)
    }

    override suspend fun removeAccount(accountId: Long): Result<Unit> {
        _registrationState.value = SipRegistrationState.NotConfigured
        return Result.success(Unit)
    }

    override suspend fun makeCall(targetUri: String, accountId: Long): CallDispatchResult {
        if (targetUri.isBlank()) {
            return CallDispatchResult.ValidationError("Enter a phone number or SIP URI.")
        }
        return CallDispatchResult.NotConfigured(
            mode = CallingMode.SIP_TO_SIP,
            title = "Calling Service Not Configured",
            message = "SIP-to-SIP calling engine is not configured. Native PJSIP C/C++ libraries " +
                "are not compiled in this environment, so no call to \"$targetUri\" was placed."
        )
    }

    override fun observeIncomingCalls(): Flow<IncomingCallEvent> = incomingCallsFlow.asSharedFlow()

    override suspend fun answerCall(callId: String): Result<Unit> =
        Result.failure(IllegalStateException("SIP engine not configured (no native PJSIP media session)."))

    override suspend fun rejectCall(callId: String): Result<Unit> =
        Result.failure(IllegalStateException("SIP engine not configured."))

    override suspend fun endCall(callId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun setMuted(callId: String, muted: Boolean): Result<Unit> =
        Result.success(Unit)

    override suspend fun setSpeakerEnabled(callId: String, enabled: Boolean): Result<Unit> =
        Result.success(Unit)

    override suspend fun setHold(callId: String, onHold: Boolean): Result<Unit> =
        Result.success(Unit)

    override suspend fun sendDtmf(callId: String, digit: Char): Result<Unit> =
        Result.success(Unit)
}
