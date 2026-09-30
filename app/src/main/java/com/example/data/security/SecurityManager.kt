package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences = createEncryptedSharedPreferences(context)

    // App is locked on startup if a PIN is configured
    private val _isLocked = MutableStateFlow(isPinEnabled())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isPinConfigured = MutableStateFlow(isPinEnabled())
    val isPinConfigured: StateFlow<Boolean> = _isPinConfigured.asStateFlow()

    private var lastActivityTimestamp: Long = System.currentTimeMillis()

    init {
        // Pre-configure initial PIN on first startup so app locks securely on startup
        if (!isPinEnabled()) {
            setPin("1234", "Default PIN: 1234")
            _isLocked.value = true
        }
    }

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w("SecurityManager", "Failed to initialize EncryptedSharedPreferences, falling back to standard prefs", e)
            context.getSharedPreferences("safeledger_security_fallback", Context.MODE_PRIVATE)
        }
    }

    fun isPinEnabled(): Boolean {
        return prefs.contains(KEY_PIN_HASH) && !prefs.getString(KEY_PIN_HASH, null).isNullOrEmpty()
    }

    fun getSecurityHint(): String {
        return prefs.getString(KEY_SECURITY_HINT, "") ?: ""
    }

    /**
     * Hashes the PIN using PBKDF2WithHmacSHA256 and securely stores it in EncryptedSharedPreferences.
     */
    fun setPin(pin: String, hint: String = ""): Boolean {
        if (pin.length < 4) return false
        val salt = generateSalt()
        val hash = hashPinWithSalt(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_SECURITY_HINT, hint)
            .apply()

        _isPinConfigured.value = true
        _isLocked.value = false
        lastActivityTimestamp = System.currentTimeMillis()
        return true
    }

    fun removePin(): Boolean {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .remove(KEY_SECURITY_HINT)
            .apply()

        _isPinConfigured.value = false
        _isLocked.value = false
        return true
    }

    /**
     * Verifies the entered PIN against the hashed PIN stored in EncryptedSharedPreferences.
     */
    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val saltString = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val salt = Base64.decode(saltString, Base64.NO_WRAP)
        val computedHash = hashPinWithSalt(pin, salt)
        val valid = storedHash == computedHash

        if (valid) {
            _isLocked.value = false
            lastActivityTimestamp = System.currentTimeMillis()
        }
        return valid
    }

    fun unlockDirectly() {
        _isLocked.value = false
        lastActivityTimestamp = System.currentTimeMillis()
    }

    fun lockNow() {
        if (isPinEnabled()) {
            _isLocked.value = true
        }
    }

    fun recordActivity() {
        lastActivityTimestamp = System.currentTimeMillis()
    }

    fun checkAndApplyAutoLock(timeoutMinutes: Int) {
        if (!isPinEnabled()) return
        if (timeoutMinutes == 0) {
            _isLocked.value = true
            return
        }
        val timeoutMillis = timeoutMinutes * 60 * 1000L
        if (System.currentTimeMillis() - lastActivityTimestamp > timeoutMillis) {
            _isLocked.value = true
        }
    }

    // --- AES-256-GCM Encryption / Decryption with PBKDF2 ---
    fun encryptData(plainText: String, secretPassphrase: String = getEncryptionSeed()): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val iv = ByteArray(12) // 12 bytes standard for GCM
        random.nextBytes(iv)

        val key = deriveKey(secretPassphrase, salt)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Format: [Salt (16)] + [IV (12)] + [Ciphertext + AuthTag]
        val combined = ByteArray(salt.size + iv.size + cipherBytes.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(cipherBytes, 0, combined, salt.size + iv.size, cipherBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptData(base64Payload: String, secretPassphrase: String = getEncryptionSeed()): String {
        val combined = Base64.decode(base64Payload, Base64.NO_WRAP)
        require(combined.size > 28) { "Invalid encrypted payload size" }

        val salt = ByteArray(16)
        val iv = ByteArray(12)
        val cipherBytes = ByteArray(combined.size - 28)

        System.arraycopy(combined, 0, salt, 0, 16)
        System.arraycopy(combined, 16, iv, 0, 12)
        System.arraycopy(combined, 28, cipherBytes, 0, cipherBytes.size)

        val key = deriveKey(secretPassphrase, salt)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val decryptedBytes = cipher.doFinal(cipherBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun getEncryptionSeed(): String {
        val storedHash = prefs.getString(KEY_PIN_HASH, null)
        if (!storedHash.isNullOrEmpty()) {
            return storedHash
        }
        var persistentSeed = prefs.getString(KEY_DEFAULT_ENCRYPTION_SEED, null)
        if (persistentSeed == null) {
            val random = ByteArray(32)
            SecureRandom().nextBytes(random)
            persistentSeed = Base64.encodeToString(random, Base64.NO_WRAP)
            prefs.edit().putString(KEY_DEFAULT_ENCRYPTION_SEED, persistentSeed).apply()
        }
        return persistentSeed
    }

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, 10000, 256)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPinWithSalt(pin: String, salt: ByteArray): String {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pin.toCharArray(), salt, 12000, 256)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    companion object {
        private const val ENCRYPTED_PREFS_FILE = "safeledger_secure_prefs"
        private const val KEY_PIN_HASH = "encrypted_pin_hash"
        private const val KEY_PIN_SALT = "encrypted_pin_salt"
        private const val KEY_SECURITY_HINT = "encrypted_security_hint"
        private const val KEY_DEFAULT_ENCRYPTION_SEED = "encrypted_default_seed"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
