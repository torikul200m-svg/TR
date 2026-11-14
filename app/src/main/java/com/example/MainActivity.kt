package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trdial.TrDialApplication
import com.example.trdial.navigation.TrDialApp
import com.example.trdial.viewmodel.AuthViewModel
import com.example.trdial.viewmodel.ContactsHistoryViewModel
import com.example.trdial.viewmodel.DialerViewModel
import com.example.trdial.viewmodel.SipAccountsSettingsViewModel
import com.example.ui.theme.TrDialSipTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as TrDialApplication).container

        setContent {
            TrDialSipTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(
                        authRepository = container.authRepository,
                        settingsRepository = container.settingsRepository
                    )
                )
                val dialerViewModel: DialerViewModel = viewModel(
                    factory = DialerViewModel.Factory(
                        callCoordinator = container.callCoordinator,
                        sipAccountRepository = container.sipAccountRepository,
                        settingsRepository = container.settingsRepository,
                        sipEngine = container.sipEngine,
                        appToAppEngine = container.appToAppCallEngine,
                        networkMonitor = container.networkMonitor
                    )
                )
                val contactsHistoryViewModel: ContactsHistoryViewModel = viewModel(
                    factory = ContactsHistoryViewModel.Factory(
                        contactRepository = container.contactRepository,
                        callHistoryRepository = container.callHistoryRepository
                    )
                )
                val sipSettingsViewModel: SipAccountsSettingsViewModel = viewModel(
                    factory = SipAccountsSettingsViewModel.Factory(
                        sipAccountRepository = container.sipAccountRepository,
                        settingsRepository = container.settingsRepository,
                        sipEngine = container.sipEngine
                    )
                )

                TrDialApp(
                    authViewModel = authViewModel,
                    dialerViewModel = dialerViewModel,
                    contactsHistoryViewModel = contactsHistoryViewModel,
                    sipSettingsViewModel = sipSettingsViewModel
                )
            }
        }
    }
}
