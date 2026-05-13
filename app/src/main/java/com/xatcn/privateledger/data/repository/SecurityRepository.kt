package com.xatcn.privateledger.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class SecurityRepository(private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "private_ledger_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // 数据库密码
    fun getDatabasePassphrase(): String {
        var passphrase = encryptedPrefs.getString("db_passphrase", null)
        if (passphrase == null) {
            passphrase = generateSecurePassphrase()
            encryptedPrefs.edit().putString("db_passphrase", passphrase).apply()
        }
        return passphrase
    }
    
    // 用户密码哈希
    fun savePasswordHash(password: String) {
        val hash = hashPassword(password)
        encryptedPrefs.edit().putString("password_hash", hash).apply()
    }
    
    fun getPasswordHash(): String? {
        return encryptedPrefs.getString("password_hash", null)
    }
    
    fun verifyPassword(password: String): Boolean {
        val storedHash = getPasswordHash() ?: return false
        return hashPassword(password) == storedHash
    }
    
    fun hasPassword(): Boolean {
        return encryptedPrefs.contains("password_hash")
    }
    
    // AI 模型路径
    fun saveModelPath(path: String) {
        encryptedPrefs.edit().putString("model_path", path).apply()
    }
    
    fun getModelPath(): String? {
        return encryptedPrefs.getString("model_path", null)
    }
    
    // 用户名
    fun saveUsername(username: String) {
        encryptedPrefs.edit().putString("username", username).apply()
    }
    
    fun getUsername(): String? {
        return encryptedPrefs.getString("username", null)
    }
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    private fun generateSecurePassphrase(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..32).map { chars.random() }.joinToString("")
    }
}
