package com.example.trdial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trdial.calling.ActiveCallUiSession
import com.example.trdial.calling.CallCoordinator
import com.example.trdial.calling.CallDispatchResult
import com.example.trdial.calling.CallingMode
import com.example.trdial.calling.IncomingCallEvent
import com.example.trdial.calling.NetworkMonitor
import com.example.trdial.calling.SipRegistrationState
import com.example.trdial.calling.apptoapp.AppToAppCallEngine
import com.example.trdial.calling.sip.SipEngine
import com.example.trdial.data.local.SipAccountEntity
import com.example.trdial.data.repository.SettingsRepository
import com.example.trdial.data.repository.SipAccountRepository
import com.example.trdial.security.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DialerNotice(
    val title: String,
    val message: String,
    val mode: CallingMode? = null
)

class DialerViewModel(
    private val callCoordinator: CallCoordinator,
    private val sipAccountRepository: SipAccountRepository,
    private val settingsRepository: SettingsRepository,
    private val sipEngine: SipEngine,
    private val appToAppEngine: AppToAppCallEngine,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
    val sipRegistrationState: StateFlow<SipRegistrationState> = sipEngine.registrationState
    val sipAccounts: StateFlow<List<SipAccountEntity>> = sipAccountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val previewIncomingCall: StateFlow<IncomingCallEvent?> = callCoordinator.previewIncomingCall
    val activeCallSession: StateFlow<ActiveCallUiSession?> = callCoordinator.activeCallSession

    private val _dialInput = MutableStateFlow("")
    val dialInput: StateFlow<String> = _dialInput.asStateFlow()

    private val _callingMode = MutableStateFlow(CallingMode.SIP_TO_SIP)
    val callingMode: StateFlow<CallingMode> = _callingMode.asStateFlow()

    private val _selectedSipAccountId = MutableStateFlow<Long?>(null)
    val selectedSipAccountId: StateFlow<Long?> = _selectedSipAccountId.asStateFlow()

    private val _notice = MutableStateFlow<DialerNotice?>(null)
    val notice: StateFlow<DialerNotice?> = _notice.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = settingsRepository.settingsFlow.first()
            _callingMode.value = CallingMode.fromKey(prefs.preferredCallingMode)
        }
        viewModelScope.launch {
            sipAccounts.collect { accounts ->
                if (_selectedSipAccountId.value == null || accounts.none { it.id == _selectedSipAccountId.value }) {
                    _selectedSipAccountId.value = accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id
                }
            }
        }
    }

    fun appendDigit(digit: String) {
        _dialInput.update { current ->
            if (current.length < 48) current + digit else current
        }
    }

    fun setDialInput(value: String) {
        _dialInput.value = value.take(64)
    }

    fun backspace() {
        _dialInput.update { if (it.isNotEmpty()) it.dropLast(1) else it }
    }

    fun clearDialInput() {
        _dialInput.value = ""
    }

    fun setCallingMode(mode: CallingMode) {
        _callingMode.value = mode
    }

    fun selectSipAccount(accountId: Long) {
        _selectedSipAccountId.value = accountId
    }

    fun dismissNotice() {
        _notice.value = null
    }

    fun initiateCall(
        targetOverride: String? = null,
        contactName: String? = null,
        modeOverride: CallingMode? = null
    ) {
        val rawTarget = targetOverride ?: _dialInput.value
        val target = InputValidator.sanitizeDialTarget(rawTarget)
        val activeMode = modeOverride ?: _callingMode.value
        val accountId = _selectedSipAccountId.value

        viewModelScope.launch {
            when (val result = callCoordinator.placeCall(target, contactName, activeMode, accountId)) {
                is CallDispatchResult.NotConfigured -> {
                    _notice.value = DialerNotice(
                        title = result.title,
                        message = result.message,
                        mode = result.mode
                    )
                }
                is CallDispatchResult.ValidationError -> {
                    _notice.value = DialerNotice(
                        title = "Invalid Destination",
                        message = result.message
                    )
                }
                is CallDispatchResult.Dispatched -> Unit
            }
        }
    }

    fun openIncomingCallPreview(
        name: String = "Tariqul Telecom Lab",
        address: String? = null,
        mode: CallingMode = _callingMode.value
    ) {
        val target = address ?: _dialInput.value.ifBlank {
            if (mode == CallingMode.SIP_TO_SIP) "sip:1001@voip.trdial.local" else "TR-884210"
        }
        callCoordinator.openIncomingCallPreview(name, target, mode)
    }

    fun openActiveCallPreview(
        name: String = "Tariqul Telecom Lab",
        address: String? = null,
        mode: CallingMode = _callingMode.value
    ) {
        val target = address ?: _dialInput.value.ifBlank {
            if (mode == CallingMode.SIP_TO_SIP) "sip:1001@voip.trdial.local" else "TR-884210"
        }
        callCoordinator.openActiveCallPreview(name, target, mode)
    }

    fun answerPreviewIncomingCall() {
        viewModelScope.launch { callCoordinator.answerPreviewIncomingCall() }
    }

    fun rejectPreviewIncomingCall() {
        viewModelScope.launch { callCoordinator.rejectPreviewIncomingCall() }
    }

    fun toggleMute() {
        viewModelScope.launch { callCoordinator.toggleMute() }
    }

    fun toggleSpeaker() {
        viewModelScope.launch { callCoordinator.toggleSpeaker() }
    }

    fun toggleHold() {
        viewModelScope.launch { callCoordinator.toggleHold() }
    }

    fun sendDtmf(digit: Char) {
        viewModelScope.launch { callCoordinator.sendDtmf(digit) }
    }

    fun endActiveCall() {
        viewModelScope.launch { callCoordinator.endActiveCall() }
    }

    class Factory(
        private val callCoordinator: CallCoordinator,
        private val sipAccountRepository: SipAccountRepository,
        private val settingsRepository: SettingsRepository,
        private val sipEngine: SipEngine,
        private val appToAppEngine: AppToAppCallEngine,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DialerViewModel(
            callCoordinator,
            sipAccountRepository,
            settingsRepository,
            sipEngine,
            appToAppEngine,
            networkMonitor
        ) as T
    }
}
