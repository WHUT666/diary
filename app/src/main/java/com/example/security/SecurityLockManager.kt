package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import java.security.MessageDigest

/**
 * 本地隐私安全锁管理器
 * 负责应用启动时、后台切回时的隐私保护认证，支持PIN密码锁、纯文本密码及指纹/面部生物识别。
 */
class SecurityLockManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "diary_security_prefs"
        private const val KEY_IS_LOCK_ENABLED = "key_is_lock_enabled"
        private const val KEY_PASSWORD_HASH = "key_password_hash"
        private const val KEY_PASSWORD_SALT = "key_password_salt"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_LOCK_TIMEOUT_SECONDS = "key_lock_timeout_seconds"
        private const val KEY_SECURITY_QUESTION = "key_security_question"
        private const val KEY_SECURITY_ANSWER_HASH = "key_security_answer_hash"

        // 默认超时锁定时间（秒）: 0 表示离开应用即刻加锁，60 表示后台 1 分钟后加锁
        const val DEFAULT_TIMEOUT_SECONDS = 0

        fun hashWithSalt(password: String, salt: String): String {
            val combined = "$salt:$password"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * 是否开启了隐私应用锁
     */
    val isLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCK_ENABLED, false) && prefs.getString(KEY_PASSWORD_HASH, null) != null

    /**
     * 是否开启了生物识别辅助解锁
     */
    val isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)

    /**
     * 后台超时自动加锁时间阈值（秒）
     */
    val lockTimeoutSeconds: Int
        get() = prefs.getInt(KEY_LOCK_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS)

    /**
     * 密保问题（用于忘记密码时重置）
     */
    val securityQuestion: String?
        get() = prefs.getString(KEY_SECURITY_QUESTION, null)

    /**
     * 检查设备硬件与系统是否支持生物识别
     */
    fun canAuthenticateWithBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * 设置/更新隐私密码
     */
    fun setPassword(password: String, question: String? = null, answer: String? = null): Boolean {
        if (password.isBlank()) return false
        val salt = System.currentTimeMillis().toString() + "_" + (1000..9999).random()
        val hash = hashWithSalt(password.trim(), salt)

        val editor = prefs.edit()
            .putBoolean(KEY_IS_LOCK_ENABLED, true)
            .putString(KEY_PASSWORD_SALT, salt)
            .putString(KEY_PASSWORD_HASH, hash)

        if (!question.isNullOrBlank() && !answer.isNullOrBlank()) {
            val answerHash = hashWithSalt(answer.trim().lowercase(), salt)
            editor.putString(KEY_SECURITY_QUESTION, question.trim())
            editor.putString(KEY_SECURITY_ANSWER_HASH, answerHash)
        }

        editor.apply()
        return true
    }

    /**
     * 校验密码是否正确
     */
    fun verifyPassword(inputPassword: String): Boolean {
        val savedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        val salt = prefs.getString(KEY_PASSWORD_SALT, "") ?: ""
        val inputHash = hashWithSalt(inputPassword.trim(), salt)
        return savedHash == inputHash
    }

    /**
     * 校验密保答案
     */
    fun verifySecurityAnswer(answer: String): Boolean {
        val savedAnswerHash = prefs.getString(KEY_SECURITY_ANSWER_HASH, null) ?: return false
        val salt = prefs.getString(KEY_PASSWORD_SALT, "") ?: ""
        val inputHash = hashWithSalt(answer.trim().lowercase(), salt)
        return savedAnswerHash == inputHash
    }

    /**
     * 开启/关闭生物识别快捷解锁
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * 设置锁定时长阈值
     */
    fun setLockTimeoutSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_LOCK_TIMEOUT_SECONDS, seconds).apply()
    }

    /**
     * 关闭/解除隐私锁
     */
    fun disableLock() {
        prefs.edit()
            .putBoolean(KEY_IS_LOCK_ENABLED, false)
            .remove(KEY_PASSWORD_HASH)
            .remove(KEY_PASSWORD_SALT)
            .remove(KEY_SECURITY_QUESTION)
            .remove(KEY_SECURITY_ANSWER_HASH)
            .apply()
    }
}
