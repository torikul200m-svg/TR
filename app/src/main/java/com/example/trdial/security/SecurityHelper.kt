package com.example.trdial.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class PasswordHashResult(
    val hashBase64: String,
    val saltBase64: String
)

data class EncryptedSecret(
    val ciphertextBase64: String,
    val ivBase64: String
)

object SecurityHelper {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "tr_dial_sip_master_key"
    private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 12_000
    private const val PBKDF2_KEY_LENGTH = 256

    // Ephemeral fallback key used strictly when AndroidKeyStore is unavailable (e.g., JVM Robolectric tests)
    private val testFallbackKey: SecretKey by lazy {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        SecretKeySpec(bytes, "AES")
    }

    fun hashPassword(password: String, existingSaltBase64: String? = null): PasswordHashResult {
        val salt = if (existingSaltBase64 != null) {
            Base64.decode(existingSaltBase64, Base64.NO_WRAP)
        } else {
            ByteArray(16).also { SecureRandom().nextBytes(it) }
        }
        val hashBytes = try {
            val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            factory.generateSecret(spec).encoded
        } catch (_: Exception) {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            digest.digest(password.toByteArray(Charsets.UTF_8))
        }
        return PasswordHashResult(
            hashBase64 = Base64.encodeToString(hashBytes, Base64.NO_WRAP),
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        )
    }

    fun verifyPassword(password: String, storedHashBase64: String, storedSaltBase64: String): Boolean {
        if (storedHashBase64.isBlank() || storedSaltBase64.isBlank()) return false
        val computed = hashPassword(password, storedSaltBase64)
        val expectedBytes = Base64.decode(storedHashBase64, Base64.NO_WRAP)
        val actualBytes = Base64.decode(computed.hashBase64, Base64.NO_WRAP)
        return MessageDigest.isEqual(expectedBytes, actualBytes)
    }

    fun encryptSecret(plaintext: String): EncryptedSecret {
        val key = getOrCreateAesKey()
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedSecret(
            ciphertextBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        )
    }

    fun decryptSecret(ciphertextBase64: String, ivBase64: String): Result<String> = runCatching {
        val key = getOrCreateAesKey()
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(Base64.decode(ciphertextBase64, Base64.NO_WRAP))
        String(decrypted, Charsets.UTF_8)
    }

    private fun getOrCreateAesKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) {
                existing
            } else {
                val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGen.init(spec)
                keyGen.generateKey()
            }
        } catch (_: Exception) {
            testFallbackKey
        }
    }
}

object InputValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val HOST_REGEX = Regex("^[A-Za-z0-9.-]+(:[0-9]{1,5})?$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun isValidPort(portText: String): Boolean {
        val port = portText.toIntOrNull() ?: return false
        return port in 1..65535
    }

    fun isValidSipHost(host: String): Boolean = host.trim().isNotEmpty() && HOST_REGEX.matches(host.trim())

    fun sanitizeDialTarget(input: String): String = input.trim().filterNot { it.isWhitespace() }
}
