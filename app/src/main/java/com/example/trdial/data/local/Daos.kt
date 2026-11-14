package com.example.trdial.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserProfileEntity): Long

    @Update
    suspend fun update(user: UserProfileEntity)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, name ASC")
    fun observeAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    fun observeFavorites(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR sipUri LIKE '%' || :query || '%' OR appPeerId LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("UPDATE contacts SET isFavorite = :isFavorite WHERE id = :contactId")
    suspend fun setFavorite(contactId: Long, isFavorite: Boolean)
}

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CallHistoryEntity>>

    @Query("SELECT * FROM call_history ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CallHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CallHistoryEntity): Long

    @Query("DELETE FROM call_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM call_history")
    suspend fun clearAll()
}

@Dao
interface SipAccountDao {
    @Query("SELECT * FROM sip_accounts ORDER BY isDefault DESC, createdAt DESC")
    fun observeAll(): Flow<List<SipAccountEntity>>

    @Query("SELECT * FROM sip_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SipAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: SipAccountEntity): Long

    @Update
    suspend fun update(account: SipAccountEntity)

    @Query("UPDATE sip_accounts SET isDefault = 0")
    suspend fun clearDefaults()

    @Query("UPDATE sip_accounts SET isDefault = 1 WHERE id = :accountId")
    suspend fun markDefault(accountId: Long)

    @Query("DELETE FROM sip_accounts WHERE id = :accountId")
    suspend fun deleteById(accountId: Long)
}
