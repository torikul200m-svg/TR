package com.example.trdial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trdial.calling.SipAccountCredentials
import com.example.trdial.calling.SipRegistrationState
import com.example.trdial.calling.SipTransport
import com.example.trdial.calling.sip.SipEngine
import com.example.trdial.data.local.SipAccountEntity
import com.example.trdial.data.local.UserSettings
import com.example.trdial.data.repository.SettingsRepository
import com.example.trdial.data.repository.SipAccountRepository
import com.example.trdial.security.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SipConfigStatus(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class SipAccountsSettingsViewModel(
    private val sipAccountRepository: SipAccountRepository,
    private val settingsRepository: SettingsRepository,
    private val sipEngine: SipEngine
) : ViewModel() {

    val sipAccounts: StateFlow<List<SipAccountEntity>> = sipAccountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val registrationState: StateFlow<SipRegistrationState> = sipEngine.registrationState

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _sipConfigStatus = MutableStateFlow(SipConfigStatus())
    val sipConfigStatus: StateFlow<SipConfigStatus> = _sipConfigStatus.asStateFlow()

    fun clearStatus() {
        _sipConfigStatus.value = SipConfigStatus()
    }

    fun saveAndTestSipAccount(
        editingId: Long = 0L,
        displayName: String,
        username: String,
        domain: String,
        portText: String,
        transport: String,
        outboundProxy: String,
        stunServer: String,
        password: String,
        makeDefault: Boolean,
        onSaved: () -> Unit
    ) {
        if (username.isBlank()) {
            _sipConfigStatus.update { it.copy(errorMessage = "SIP Username / Extension is required.") }
            return
        }
        if (!InputValidator.isValidSipHost(domain)) {
            _sipConfigStatus.update { it.copy(errorMessage = "Enter a valid SIP domain or registrar host.") }
            return
        }
        if (!InputValidator.isValidPort(portText)) {
            _sipConfigStatus.update { it.copy(errorMessage = "Port must be between 1 and 65535.") }
            return
        }
        if (editingId == 0L && password.isBlank()) {
            _sipConfigStatus.update { it.copy(errorMessage = "SIP Password is required (stored encrypted).") }
            return
        }

        viewModelScope.launch {
            _sipConfigStatus.update { it.copy(isSaving = true, errorMessage = null, infoMessage = null) }
            val result = sipAccountRepository.saveAccount(
                id = editingId,
                displayName = displayName,
                username = username,
                domain = domain,
                port = portText.toInt(),
                transport = transport,
                outboundProxy = outboundProxy,
                stunServer = stunServer,
                plainPassword = password,
                makeDefault = makeDefault
            )

            result.onSuccess { saved ->
                val decrypted = sipAccountRepository.decryptPassword(saved).getOrDefault("")
                val regResult = sipEngine.registerAccount(
                    SipAccountCredentials(
                        accountId = saved.id,
                        displayName = saved.displayName,
                        username = saved.username,
                        domain = saved.domain,
                        port = saved.port,
                        transport = runCatching { SipTransport.valueOf(saved.transport) }.getOrDefault(SipTransport.UDP),
                        outboundProxy = saved.outboundProxy.ifBlank { null },
                        decryptedPassword = decrypted
                    )
                )
                val info = when (val state = regResult.getOrNull()) {
                    is SipRegistrationState.Error -> state.reason
                    else -> "SIP account encrypted & saved."
                }
                _sipConfigStatus.update { it.copy(isSaving = false, infoMessage = info) }
                onSaved()
            }.onFailure { err ->
                _sipConfigStatus.update {
                    it.copy(isSaving = false, errorMessage = err.message ?: "Failed to save SIP account.")
                }
            }
        }
    }

    fun testRegistration(account: SipAccountEntity) {
        viewModelScope.launch {
            val decrypted = sipAccountRepository.decryptPassword(account).getOrDefault("")
            val regResult = sipEngine.registerAccount(
                SipAccountCredentials(
                    accountId = account.id,
                    displayName = account.displayName,
                    username = account.username,
                    domain = account.domain,
                    port = account.port,
                    transport = runCatching { SipTransport.valueOf(account.transport) }.getOrDefault(SipTransport.UDP),
                    outboundProxy = account.outboundProxy.ifBlank { null },
                    decryptedPassword = decrypted
                )
            )
            val info = (regResult.getOrNull() as? SipRegistrationState.Error)?.reason
                ?: "Checked SIP engine status."
            _sipConfigStatus.update { it.copy(infoMessage = info) }
        }
    }

    fun setDefaultAccount(accountId: Long) {
        viewModelScope.launch {
            sipAccountRepository.setDefaultAccount(accountId)
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            sipEngine.removeAccount(accountId)
            sipAccountRepository.deleteAccount(accountId)
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch { settingsRepository.setLanguageCode(code) }
    }

    fun setPreferredCallingMode(mode: String) {
        viewModelScope.launch { settingsRepository.setPreferredCallingMode(mode) }
    }

    fun setEchoCancellation(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setEchoCancellation(enabled) }
    }

    fun setNoiseSuppression(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNoiseSuppression(enabled) }
    }

    fun setAutoProximitySpeaker(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoProximitySpeaker(enabled) }
    }

    fun setWifiOnlyCalls(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setWifiOnlyCalls(enabled) }
    }

    class Factory(
        private val sipAccountRepository: SipAccountRepository,
        private val settingsRepository: SettingsRepository,
        private val sipEngine: SipEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SipAccountsSettingsViewModel(sipAccountRepository, settingsRepository, sipEngine) as T
    }
}
