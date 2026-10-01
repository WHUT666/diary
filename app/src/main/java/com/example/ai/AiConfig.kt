package com.example.ai

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

enum class AiProvider(val displayName: String) {
    GEMINI("Google Gemini"),
    CUSTOM_OPENAI("自定义 / OpenAI兼容接口")
}

data class AiConfig(
    val provider: AiProvider = AiProvider.GEMINI,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.5-flash",
    val customBaseUrl: String = "https://api.deepseek.com/v1",
    val customApiKey: String = "",
    val customModel: String = "deepseek-chat",
    val temperature: Float = 0.7f
) {
    val effectiveGeminiKey: String
        get() = geminiApiKey.ifBlank {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }
        }
}

class AiConfigManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_settings_prefs", Context.MODE_PRIVATE)

    fun loadConfig(): AiConfig {
        val providerStr = prefs.getString("provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        val provider = try {
            AiProvider.valueOf(providerStr)
        } catch (e: Exception) {
            AiProvider.GEMINI
        }
        return AiConfig(
            provider = provider,
            geminiApiKey = prefs.getString("gemini_api_key", "") ?: "",
            geminiModel = prefs.getString("gemini_model", "gemini-3.5-flash") ?: "gemini-3.5-flash",
            customBaseUrl = prefs.getString("custom_base_url", "https://api.deepseek.com/v1") ?: "https://api.deepseek.com/v1",
            customApiKey = prefs.getString("custom_api_key", "") ?: "",
            customModel = prefs.getString("custom_model", "deepseek-chat") ?: "deepseek-chat",
            temperature = prefs.getFloat("temperature", 0.7f)
        )
    }

    fun saveConfig(config: AiConfig) {
        prefs.edit()
            .putString("provider", config.provider.name)
            .putString("gemini_api_key", config.geminiApiKey)
            .putString("gemini_model", config.geminiModel)
            .putString("custom_base_url", config.customBaseUrl)
            .putString("custom_api_key", config.customApiKey)
            .putString("custom_model", config.customModel)
            .putFloat("temperature", config.temperature)
            .apply()
    }
}
