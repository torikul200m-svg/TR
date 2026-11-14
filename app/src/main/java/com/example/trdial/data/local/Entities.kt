package com.example.trdial.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phoneExtension: String,
    val appPeerId: String,
    val passwordHash: String,
    val passwordSalt: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val sipUri: String,
    val appPeerId: String,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_history")
data class CallHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String,
    val targetAddress: String,
    val callingMode: String, // "SIP_TO_SIP" or "APP_TO_APP"
    val direction: String,   // "OUTGOING", "INCOMING", "MISSED"
    val status: String,      // "UNCONFIGURED", "COMPLETED", "MISSED", "REJECTED"
    val durationSeconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sip_accounts")
data class SipAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val username: String,
    val domain: String,
    val port: Int = 5060,
    val transport: String = "UDP", // UDP, TCP, TLS
    val outboundProxy: String = "",
    val stunServer: String = "",
    val encryptedPassword: String,
    val passwordIv: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
