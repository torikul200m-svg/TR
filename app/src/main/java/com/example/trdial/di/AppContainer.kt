package com.example.trdial.di

import android.content.Context
import com.example.trdial.calling.CallCoordinator
import com.example.trdial.calling.NetworkMonitor
import com.example.trdial.calling.apptoapp.AppToAppCallEngine
import com.example.trdial.calling.apptoapp.UnconfiguredAppToAppEngine
import com.example.trdial.calling.sip.SipEngine
import com.example.trdial.calling.sip.UnconfiguredSipEngine
import com.example.trdial.data.local.SettingsDataStore
import com.example.trdial.data.local.TrDialDatabase
import com.example.trdial.data.repository.AuthRepository
import com.example.trdial.data.repository.CallHistoryRepository
import com.example.trdial.data.repository.ContactRepository
import com.example.trdial.data.repository.DataStoreSettingsRepository
import com.example.trdial.data.repository.RoomAuthRepository
import com.example.trdial.data.repository.RoomCallHistoryRepository
import com.example.trdial.data.repository.RoomContactRepository
import com.example.trdial.data.repository.RoomSipAccountRepository
import com.example.trdial.data.repository.SettingsRepository
import com.example.trdial.data.repository.SipAccountRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: TrDialDatabase by lazy {
        TrDialDatabase.getInstance(appContext)
    }

    private val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(appContext)
    }

    val authRepository: AuthRepository by lazy {
        RoomAuthRepository(database.userProfileDao())
    }

    val contactRepository: ContactRepository by lazy {
        RoomContactRepository(database.contactDao())
    }

    val callHistoryRepository: CallHistoryRepository by lazy {
        RoomCallHistoryRepository(database.callHistoryDao())
    }

    val sipAccountRepository: SipAccountRepository by lazy {
        RoomSipAccountRepository(database.sipAccountDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(settingsDataStore)
    }

    val sipEngine: SipEngine by lazy {
        UnconfiguredSipEngine()
    }

    val appToAppCallEngine: AppToAppCallEngine by lazy {
        UnconfiguredAppToAppEngine()
    }

    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(appContext)
    }

    val callCoordinator: CallCoordinator by lazy {
        CallCoordinator(
            sipEngine = sipEngine,
            appToAppEngine = appToAppCallEngine,
            callHistoryRepository = callHistoryRepository
        )
    }
}
