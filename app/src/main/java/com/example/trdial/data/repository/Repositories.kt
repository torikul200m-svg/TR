package com.example.trdial.data.repository

import com.example.trdial.data.local.CallHistoryDao
import com.example.trdial.data.local.CallHistoryEntity
import com.example.trdial.data.local.ContactDao
import com.example.trdial.data.local.ContactEntity
import com.example.trdial.data.local.SettingsDataStore
import com.example.trdial.data.local.SipAccountDao
import com.example.trdial.data.local.SipAccountEntity
import com.example.trdial.data.local.UserProfileDao
import com.example.trdial.data.local.UserProfileEntity
import com.example.trdial.data.local.UserSettings
import com.example.trdial.security.SecurityHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

interface AuthRepository {
    fun observeUser(userId: Long): Flow<UserProfileEntity?>
    suspend fun register(fullName: String, email: String, phoneExtension: String, password: String): Result<UserProfileEntity>
    suspend fun login(email: String, password: String): Result<UserProfileEntity>
    suspend fun updateDisplayName(userId: Long, fullName: String, phoneExtension: String): Result<Unit>
}

interface ContactRepository {
    fun observeContacts(query: String = ""): Flow<List<ContactEntity>>
    fun observeFavorites(): Flow<List<ContactEntity>>
    suspend fun saveContact(contact: ContactEntity): Long
    suspend fun deleteContact(contact: ContactEntity)
    suspend fun setFavorite(contactId: Long, isFavorite: Boolean)
}

interface CallHistoryRepository {
    fun observeAll(): Flow<List<CallHistoryEntity>>
    fun observeRecent(limit: Int = 5): Flow<List<CallHistoryEntity>>
    suspend fun logCall(entry: CallHistoryEntity): Long
    suspend fun deleteEntry(id: Long)
    suspend fun clearAll()
}

interface SipAccountRepository {
    fun observeAccounts(): Flow<List<SipAccountEntity>>
    suspend fun saveAccount(
        id: Long = 0,
        displayName: String,
        username: String,
        domain: String,
        port: Int,
        transport: String,
        outboundProxy: String,
        stunServer: String,
        plainPassword: String,
        makeDefault: Boolean
    ): Result<SipAccountEntity>

    suspend fun setDefaultAccount(accountId: Long)
    suspend fun deleteAccount(accountId: Long)
    suspend fun decryptPassword(account: SipAccountEntity): Result<String>
}

interface SettingsRepository {
    val settingsFlow: Flow<UserSettings>
    suspend fun setLanguageCode(code: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setLoggedInUserId(userId: Long?)
    suspend fun setPreferredCallingMode(mode: String)
    suspend fun setEchoCancellation(enabled: Boolean)
    suspend fun setNoiseSuppression(enabled: Boolean)
    suspend fun setAutoProximitySpeaker(enabled: Boolean)
    suspend fun setWifiOnlyCalls(enabled: Boolean)
}

class RoomAuthRepository(
    private val userProfileDao: UserProfileDao
) : AuthRepository {

    override fun observeUser(userId: Long): Flow<UserProfileEntity?> = userProfileDao.observeById(userId)

    override suspend fun register(
        fullName: String,
        email: String,
        phoneExtension: String,
        password: String
    ): Result<UserProfileEntity> = runCatching {
        val normalizedEmail = email.trim().lowercase()
        val existing = userProfileDao.getByEmail(normalizedEmail)
        if (existing != null) {
            throw IllegalStateException("EMAIL_EXISTS")
        }
        val hashed = SecurityHelper.hashPassword(password)
        val peerSuffix = UUID.randomUUID().toString().take(6).uppercase()
        val entity = UserProfileEntity(
            fullName = fullName.trim(),
            email = normalizedEmail,
            phoneExtension = phoneExtension.trim(),
            appPeerId = "TR-$peerSuffix",
            passwordHash = hashed.hashBase64,
            passwordSalt = hashed.saltBase64
        )
        val id = userProfileDao.insert(entity)
        entity.copy(id = id)
    }

    override suspend fun login(email: String, password: String): Result<UserProfileEntity> = runCatching {
        val normalizedEmail = email.trim().lowercase()
        val user = userProfileDao.getByEmail(normalizedEmail)
            ?: throw IllegalArgumentException("INVALID_CREDENTIALS")
        val verified = SecurityHelper.verifyPassword(password, user.passwordHash, user.passwordSalt)
        if (!verified) {
            throw IllegalArgumentException("INVALID_CREDENTIALS")
        }
        user
    }

    override suspend fun updateDisplayName(userId: Long, fullName: String, phoneExtension: String): Result<Unit> = runCatching {
        val existing = userProfileDao.getById(userId) ?: error("User not found")
        userProfileDao.update(
            existing.copy(
                fullName = fullName.trim(),
                phoneExtension = phoneExtension.trim()
            )
        )
    }
}

class RoomContactRepository(
    private val contactDao: ContactDao
) : ContactRepository {
    override fun observeContacts(query: String): Flow<List<ContactEntity>> =
        if (query.isBlank()) contactDao.observeAll() else contactDao.search(query.trim())

    override fun observeFavorites(): Flow<List<ContactEntity>> = contactDao.observeFavorites()

    override suspend fun saveContact(contact: ContactEntity): Long =
        if (contact.id == 0L) contactDao.insert(contact) else {
            contactDao.update(contact)
            contact.id
        }

    override suspend fun deleteContact(contact: ContactEntity) = contactDao.delete(contact)

    override suspend fun setFavorite(contactId: Long, isFavorite: Boolean) =
        contactDao.setFavorite(contactId, isFavorite)
}

class RoomCallHistoryRepository(
    private val callHistoryDao: CallHistoryDao
) : CallHistoryRepository {
    override fun observeAll(): Flow<List<CallHistoryEntity>> = callHistoryDao.observeAll()

    override fun observeRecent(limit: Int): Flow<List<CallHistoryEntity>> = callHistoryDao.observeRecent(limit)

    override suspend fun logCall(entry: CallHistoryEntity): Long = callHistoryDao.insert(entry)

    override suspend fun deleteEntry(id: Long) = callHistoryDao.deleteById(id)

    override suspend fun clearAll() = callHistoryDao.clearAll()
}

class RoomSipAccountRepository(
    private val sipAccountDao: SipAccountDao
) : SipAccountRepository {
    override fun observeAccounts(): Flow<List<SipAccountEntity>> = sipAccountDao.observeAll()

    override suspend fun saveAccount(
        id: Long,
        displayName: String,
        username: String,
        domain: String,
        port: Int,
        transport: String,
        outboundProxy: String,
        stunServer: String,
        plainPassword: String,
        makeDefault: Boolean
    ): Result<SipAccountEntity> = runCatching {
        val existing = if (id > 0L) sipAccountDao.getById(id) else null
        val encrypted = if (plainPassword.isNotBlank()) {
            SecurityHelper.encryptSecret(plainPassword)
        } else if (existing != null) {
            null
        } else {
            throw IllegalArgumentException("SIP password is required")
        }

        val currentList = sipAccountDao.observeAll().first()
        val shouldBeDefault = makeDefault || currentList.isEmpty()
        if (shouldBeDefault) {
            sipAccountDao.clearDefaults()
        }

        val entity = SipAccountEntity(
            id = id,
            displayName = displayName.trim().ifBlank { username.trim() },
            username = username.trim(),
            domain = domain.trim(),
            port = port,
            transport = transport,
            outboundProxy = outboundProxy.trim(),
            stunServer = stunServer.trim(),
            encryptedPassword = encrypted?.ciphertextBase64 ?: existing!!.encryptedPassword,
            passwordIv = encrypted?.ivBase64 ?: existing!!.passwordIv,
            isDefault = shouldBeDefault
        )
        val savedId = sipAccountDao.insert(entity)
        entity.copy(id = if (id > 0L) id else savedId)
    }

    override suspend fun setDefaultAccount(accountId: Long) {
        sipAccountDao.clearDefaults()
        sipAccountDao.markDefault(accountId)
    }

    override suspend fun deleteAccount(accountId: Long) {
        sipAccountDao.deleteById(accountId)
    }

    override suspend fun decryptPassword(account: SipAccountEntity): Result<String> =
        SecurityHelper.decryptSecret(account.encryptedPassword, account.passwordIv)
}

class DataStoreSettingsRepository(
    private val dataStore: SettingsDataStore
) : SettingsRepository {
    override val settingsFlow: Flow<UserSettings> = dataStore.settingsFlow
    override suspend fun setLanguageCode(code: String) = dataStore.setLanguageCode(code)
    override suspend fun setOnboardingCompleted(completed: Boolean) = dataStore.setOnboardingCompleted(completed)
    override suspend fun setLoggedInUserId(userId: Long?) = dataStore.setLoggedInUserId(userId)
    override suspend fun setPreferredCallingMode(mode: String) = dataStore.setPreferredCallingMode(mode)
    override suspend fun setEchoCancellation(enabled: Boolean) = dataStore.setEchoCancellation(enabled)
    override suspend fun setNoiseSuppression(enabled: Boolean) = dataStore.setNoiseSuppression(enabled)
    override suspend fun setAutoProximitySpeaker(enabled: Boolean) = dataStore.setAutoProximitySpeaker(enabled)
    override suspend fun setWifiOnlyCalls(enabled: Boolean) = dataStore.setWifiOnlyCalls(enabled)
}
