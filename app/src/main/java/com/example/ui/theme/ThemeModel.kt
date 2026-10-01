package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 主题分类
 */
enum class ThemeCategory(val displayName: String) {
    ALL("全部"),
    LIGHT("明朗浅色"),
    DARK("深邃夜间")
}

/**
 * 结构化主题预设数据模型
 * 设计为完全开放可扩展结构，便于后续或第三方模块随时注册新主题。
 */
data class ThemePreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val isDark: Boolean,
    val accentColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val onBackgroundColor: Color,
    val onSurfaceColor: Color,
    val outlineColor: Color,
    val cardElevation: Dp = 1.dp,
    val tag: String = "官方推荐",
    val colorScheme: ColorScheme
)

/**
 * 全局主题注册中心 (ThemeRegistry)
 * 支持应用启动时静态注册、运行时动态注册以及后续版本无限扩充新主题。
 */
object ThemeRegistry {
    private val themeMap = LinkedHashMap<String, ThemePreset>()

    init {
        registerDefaultThemes()
    }

    private fun registerDefaultThemes() {
        // 1. iOS 经典明朗 (Cupertino Clean Light)
        val iosClassicLightScheme = lightColorScheme(
            primary = Color(0xFF007AFF), // iOS System Blue
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE5F1FF),
            onPrimaryContainer = Color(0xFF003875),
            secondary = Color(0xFF5856D6), // iOS System Purple
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFEFEFFC),
            onSecondaryContainer = Color(0xFF221F73),
            tertiary = Color(0xFFFF9500), // iOS System Orange
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFF2DF),
            onTertiaryContainer = Color(0xFF5C3200),
            background = Color(0xFFF2F2F7), // iOS System Grouped Background
            surface = Color(0xFFFFFFFF),    // iOS Secondary System Grouped Background (Cards)
            surfaceVariant = Color(0xFFE5E5EA),
            onBackground = Color(0xFF000000),
            onSurface = Color(0xFF000000),
            onSurfaceVariant = Color(0xFF6C6C70),
            outline = Color(0xFFD1D1D6)
        )
        register(
            ThemePreset(
                id = "ios_classic_light",
                name = "iOS 经典明朗",
                subtitle = "Cupertino 经典蓝白",
                description = "经典的 iOS 系统分组质感，标志性蔚蓝主色配纯白卡片，通透而明朗。",
                isDark = false,
                accentColor = Color(0xFF007AFF),
                secondaryColor = Color(0xFF5856D6),
                backgroundColor = Color(0xFFF2F2F7),
                surfaceColor = Color(0xFFFFFFFF),
                surfaceVariantColor = Color(0xFFE5E5EA),
                onBackgroundColor = Color(0xFF000000),
                onSurfaceColor = Color(0xFF1C1C1E),
                outlineColor = Color(0xFFD1D1D6),
                tag = "官方经典",
                colorScheme = iosClassicLightScheme
            )
        )

        // 2. iOS 深邃夜黑 (Cupertino OLED True Dark)
        val iosClassicDarkScheme = darkColorScheme(
            primary = Color(0xFF0A84FF), // iOS Dark System Blue
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF003875),
            onPrimaryContainer = Color(0xFFCCE4FF),
            secondary = Color(0xFF5E5CE6), // iOS Dark Purple
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF2E2B80),
            onSecondaryContainer = Color(0xFFDFDEFA),
            tertiary = Color(0xFFFF9F0A), // iOS Dark Orange
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFF5C3600),
            onTertiaryContainer = Color(0xFFFFE2B8),
            background = Color(0xFF000000), // OLED True Black
            surface = Color(0xFF1C1C1E),    // iOS Grouped Dark
            surfaceVariant = Color(0xFF2C2C2E),
            onBackground = Color(0xFFFFFFFF),
            onSurface = Color(0xFFF2F2F7),
            onSurfaceVariant = Color(0xFF98989D),
            outline = Color(0xFF38383A)
        )
        register(
            ThemePreset(
                id = "ios_classic_dark",
                name = "iOS 深邃夜黑",
                subtitle = "OLED 沉浸夜间模式",
                description = "专为夜间与 OLED 屏幕定制，极致纯黑底色与层次分明的灰阶卡片。",
                isDark = true,
                accentColor = Color(0xFF0A84FF),
                secondaryColor = Color(0xFF5E5CE6),
                backgroundColor = Color(0xFF000000),
                surfaceColor = Color(0xFF1C1C1E),
                surfaceVariantColor = Color(0xFF2C2C2E),
                onBackgroundColor = Color(0xFFFFFFFF),
                onSurfaceColor = Color(0xFFF2F2F7),
                outlineColor = Color(0xFF38383A),
                tag = "暗色护眼",
                colorScheme = iosClassicDarkScheme
            )
        )

        // 3. iOS 暖阳书笺 (Warm Paper Journal)
        val iosWarmAmberScheme = lightColorScheme(
            primary = Color(0xFFD97706), // Warm Amber
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFEF3C7),
            onPrimaryContainer = Color(0xFF78350F),
            secondary = Color(0xFF8D5B3A),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF3E7DC),
            onSecondaryContainer = Color(0xFF351F10),
            tertiary = Color(0xFFB45309),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFEDD5),
            onTertiaryContainer = Color(0xFF431407),
            background = Color(0xFFFAF6F0), // Warm Parchment
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF2ECE4),
            onBackground = Color(0xFF261D17),
            onSurface = Color(0xFF261D17),
            onSurfaceVariant = Color(0xFF786B61),
            outline = Color(0xFFE2D6C8)
        )
        register(
            ThemePreset(
                id = "ios_warm_paper",
                name = "iOS 暖阳书笺",
                subtitle = "温暖燕麦纸本色调",
                description = "如同在午后阳光下翻阅牛皮纸笔记本，散发着纸墨温润与松弛感。",
                isDark = false,
                accentColor = Color(0xFFD97706),
                secondaryColor = Color(0xFF8D5B3A),
                backgroundColor = Color(0xFFFAF6F0),
                surfaceColor = Color(0xFFFFFFFF),
                surfaceVariantColor = Color(0xFFF2ECE4),
                onBackgroundColor = Color(0xFF261D17),
                onSurfaceColor = Color(0xFF261D17),
                outlineColor = Color(0xFFE2D6C8),
                tag = "文学雅致",
                colorScheme = iosWarmAmberScheme
            )
        )

        // 4. iOS 鼠尾草绿 (Botanical Sage Green)
        val iosForestSageScheme = lightColorScheme(
            primary = Color(0xFF10B981), // Emerald Sage
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD1FAE5),
            onPrimaryContainer = Color(0xFF064E3B),
            secondary = Color(0xFF059669),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFA7F3D0),
            onSecondaryContainer = Color(0xFF064E3B),
            tertiary = Color(0xFF3B82F6),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFDBEAFE),
            onTertiaryContainer = Color(0xFF1E3A8A),
            background = Color(0xFFEFF5F1), // Sage Background
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE0EBE3),
            onBackground = Color(0xFF132A1C),
            onSurface = Color(0xFF132A1C),
            onSurfaceVariant = Color(0xFF556B5C),
            outline = Color(0xFFCBE0D1)
        )
        register(
            ThemePreset(
                id = "ios_forest_sage",
                name = "iOS 鼠尾草绿",
                subtitle = "清爽植物森林美学",
                description = "舒缓治愈的天然鼠尾草绿意，给日常书写注入一份宁静与自然生机。",
                isDark = false,
                accentColor = Color(0xFF10B981),
                secondaryColor = Color(0xFF059669),
                backgroundColor = Color(0xFFEFF5F1),
                surfaceColor = Color(0xFFFFFFFF),
                surfaceVariantColor = Color(0xFFE0EBE3),
                onBackgroundColor = Color(0xFF132A1C),
                onSurfaceColor = Color(0xFF132A1C),
                outlineColor = Color(0xFFCBE0D1),
                tag = "清新治愈",
                colorScheme = iosForestSageScheme
            )
        )

        // 5. iOS 晨露初樱 (Sakura Coral Blush)
        val iosSakuraBlushScheme = lightColorScheme(
            primary = Color(0xFFF43F5E), // Rose Pink
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFE4E6),
            onPrimaryContainer = Color(0xFF881337),
            secondary = Color(0xFFFB7185),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFECDD3),
            onSecondaryContainer = Color(0xFF881337),
            tertiary = Color(0xFFA855F7),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF3E8FF),
            onTertiaryContainer = Color(0xFF581C87),
            background = Color(0xFFFDF2F4), // Blush background
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFCE3E8),
            onBackground = Color(0xFF33141B),
            onSurface = Color(0xFF33141B),
            onSurfaceVariant = Color(0xFF7E545F),
            outline = Color(0xFFF3C7D0)
        )
        register(
            ThemePreset(
                id = "ios_sakura_blush",
                name = "iOS 晨露初樱",
                subtitle = "柔嫩樱花珊瑚粉",
                description = "温柔轻盈的早春初樱色泽，细腻唯美，记录心动与美好日常。",
                isDark = false,
                accentColor = Color(0xFFF43F5E),
                secondaryColor = Color(0xFFFB7185),
                backgroundColor = Color(0xFFFDF2F4),
                surfaceColor = Color(0xFFFFFFFF),
                surfaceVariantColor = Color(0xFFFCE3E8),
                onBackgroundColor = Color(0xFF33141B),
                onSurfaceColor = Color(0xFF33141B),
                outlineColor = Color(0xFFF3C7D0),
                tag = "浪漫温柔",
                colorScheme = iosSakuraBlushScheme
            )
        )

        // 6. iOS 幻梦薰衣草 (Lavender Lilac Mist)
        val iosLavenderScheme = lightColorScheme(
            primary = Color(0xFF8B5CF6), // Violet Iris
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEDE9FE),
            onPrimaryContainer = Color(0xFF4C1D95),
            secondary = Color(0xFF6366F1),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0E7FF),
            onSecondaryContainer = Color(0xFF312E81),
            tertiary = Color(0xFFEC4899),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFCE7F3),
            onTertiaryContainer = Color(0xFF831843),
            background = Color(0xFFF6F4FC), // Lavender Mist
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFECE7F8),
            onBackground = Color(0xFF221638),
            onSurface = Color(0xFF221638),
            onSurfaceVariant = Color(0xFF675682),
            outline = Color(0xFFDCD2F2)
        )
        register(
            ThemePreset(
                id = "ios_lavender_iris",
                name = "iOS 幻梦薰衣草",
                subtitle = "梦幻鸢尾紫雾",
                description = "充满灵感与冥想格调的薰衣草紫，营造专注而宁静的思绪空间。",
                isDark = false,
                accentColor = Color(0xFF8B5CF6),
                secondaryColor = Color(0xFF6366F1),
                backgroundColor = Color(0xFFF6F4FC),
                surfaceColor = Color(0xFFFFFFFF),
                surfaceVariantColor = Color(0xFFECE7F8),
                onBackgroundColor = Color(0xFF221638),
                onSurfaceColor = Color(0xFF221638),
                outlineColor = Color(0xFFDCD2F2),
                tag = "灵感幻境",
                colorScheme = iosLavenderScheme
            )
        )

        // 7. iOS 午夜星河 (Midnight Space Titanium)
        val iosMidnightDarkScheme = darkColorScheme(
            primary = Color(0xFF38BDF8), // Cyan star
            onPrimary = Color(0xFF002B40),
            primaryContainer = Color(0xFF075985),
            onPrimaryContainer = Color(0xFFE0F2FE),
            secondary = Color(0xFF818CF8),
            onSecondary = Color(0xFF1E1B4B),
            secondaryContainer = Color(0xFF3730A3),
            onSecondaryContainer = Color(0xFFE0E7FF),
            tertiary = Color(0xFF34D399),
            onTertiary = Color(0xFF022C22),
            tertiaryContainer = Color(0xFF065F46),
            onTertiaryContainer = Color(0xFFD1FAE5),
            background = Color(0xFF0B0F19), // Deep space
            surface = Color(0xFF131B2E),
            surfaceVariant = Color(0xFF1E293B),
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155)
        )
        register(
            ThemePreset(
                id = "ios_midnight_space",
                name = "iOS 午夜星河",
                subtitle = "太空深空钛金属夜色",
                description = "极客深空灰蓝搭配荧光青蓝星芒，科技感与高级质感兼备。",
                isDark = true,
                accentColor = Color(0xFF38BDF8),
                secondaryColor = Color(0xFF818CF8),
                backgroundColor = Color(0xFF0B0F19),
                surfaceColor = Color(0xFF131B2E),
                surfaceVariantColor = Color(0xFF1E293B),
                onBackgroundColor = Color(0xFFF1F5F9),
                onSurfaceColor = Color(0xFFF1F5F9),
                outlineColor = Color(0xFF334155),
                tag = "极客深空",
                colorScheme = iosMidnightDarkScheme
            )
        )
    }

    /**
     * 注册单个主题预设。后续版本或扩展模块直接调用即可扩充新主题！
     */
    fun register(theme: ThemePreset) {
        themeMap[theme.id] = theme
    }

    /**
     * 批量注册主题
     */
    fun registerAll(themes: List<ThemePreset>) {
        themes.forEach { register(it) }
    }

    /**
     * 获取所有可用主题列表
     */
    fun getAllThemes(): List<ThemePreset> = themeMap.values.toList()

    /**
     * 根据 ID 获取主题，若不存在则回退至经典明朗主题
     */
    fun getTheme(id: String): ThemePreset {
        return themeMap[id] ?: themeMap["ios_classic_light"] ?: getAllThemes().first()
    }

    /**
     * 根据用户所选主色与背景智能生成自定义 ThemePreset
     */
    fun buildCustomPreset(
        id: String,
        name: String,
        subtitle: String = "用户专属定制",
        accentColor: Color,
        backgroundColor: Color,
        isDark: Boolean,
        tag: String = "自定调色"
    ): ThemePreset {
        val scheme = if (isDark) {
            darkColorScheme(
                primary = accentColor,
                onPrimary = if (accentColor.luminance() > 0.5f) Color(0xFF000000) else Color(0xFFFFFFFF),
                primaryContainer = accentColor.copy(alpha = 0.25f),
                onPrimaryContainer = Color(0xFFE2E8F0),
                secondary = accentColor.copy(alpha = 0.85f),
                onSecondary = Color(0xFFFFFFFF),
                background = backgroundColor,
                surface = Color(0xFF1E2430),
                surfaceVariant = Color(0xFF283244),
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF3B4861)
            )
        } else {
            lightColorScheme(
                primary = accentColor,
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = accentColor.copy(alpha = 0.15f),
                onPrimaryContainer = accentColor,
                secondary = accentColor.copy(alpha = 0.8f),
                onSecondary = Color(0xFFFFFFFF),
                background = backgroundColor,
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFF1F5F9),
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A),
                onSurfaceVariant = Color(0xFF64748B),
                outline = Color(0xFFCBD5E1)
            )
        }

        return ThemePreset(
            id = id,
            name = name,
            subtitle = subtitle,
            description = "由用户自定调色板配置的专属风格色彩。",
            isDark = isDark,
            accentColor = accentColor,
            secondaryColor = accentColor.copy(alpha = 0.85f),
            backgroundColor = backgroundColor,
            surfaceColor = if (isDark) Color(0xFF1E2430) else Color(0xFFFFFFFF),
            surfaceVariantColor = if (isDark) Color(0xFF283244) else Color(0xFFF1F5F9),
            onBackgroundColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
            onSurfaceColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
            outlineColor = if (isDark) Color(0xFF3B4861) else Color(0xFFCBD5E1),
            tag = tag,
            colorScheme = scheme
        )
    }

    private fun Color.luminance(): Float {
        return (0.299f * red + 0.587f * green + 0.114f * blue)
    }
}

/**
 * 本地主题持久化管理器
 */
class AppThemeManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadInitialTheme())
    val currentTheme: StateFlow<ThemePreset> = _currentTheme.asStateFlow()

    init {
        loadCustomSavedThemes()
    }

    companion object {
        private const val PREFS_NAME = "diary_app_theme_prefs"
        private const val KEY_THEME_ID = "key_current_theme_id"
        private const val KEY_CUSTOM_THEMES_STORE = "key_custom_themes_store"
        const val DEFAULT_THEME_ID = "ios_classic_light"
    }

    private fun loadInitialTheme(): ThemePreset {
        loadCustomSavedThemes()
        val savedId = prefs.getString(KEY_THEME_ID, DEFAULT_THEME_ID) ?: DEFAULT_THEME_ID
        return ThemeRegistry.getTheme(savedId)
    }

    private fun loadCustomSavedThemes() {
        val raw = prefs.getString(KEY_CUSTOM_THEMES_STORE, null) ?: return
        // 格式: id|name|accentArgb|bgArgb|isDark;;
        val items = raw.split(";;").filter { it.isNotBlank() }
        for (item in items) {
            val parts = item.split("|")
            if (parts.size >= 5) {
                try {
                    val id = parts[0]
                    val name = parts[1]
                    val accent = Color(parts[2].toLong())
                    val bg = Color(parts[3].toLong())
                    val isDark = parts[4].toBoolean()

                    val preset = ThemeRegistry.buildCustomPreset(
                        id = id,
                        name = name,
                        accentColor = accent,
                        backgroundColor = bg,
                        isDark = isDark,
                        tag = "我的定制"
                    )
                    ThemeRegistry.register(preset)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * 保存并应用新的自创主题
     */
    fun createAndApplyCustomTheme(
        name: String,
        accentColor: Color,
        backgroundColor: Color,
        isDark: Boolean
    ) {
        val customId = "custom_theme_${System.currentTimeMillis()}"
        val preset = ThemeRegistry.buildCustomPreset(
            id = customId,
            name = name.ifBlank { "我的专属主题" },
            accentColor = accentColor,
            backgroundColor = backgroundColor,
            isDark = isDark,
            tag = "我的定制"
        )
        ThemeRegistry.register(preset)

        // 追加持久化
        val currentStore = prefs.getString(KEY_CUSTOM_THEMES_STORE, "") ?: ""
        val entry = "$customId|${preset.name}|${accentColor.value.toLong()}|${backgroundColor.value.toLong()}|$isDark;;"
        prefs.edit().putString(KEY_CUSTOM_THEMES_STORE, currentStore + entry).apply()

        selectTheme(customId)
    }

    /**
     * 切换并保存主题
     */
    fun selectTheme(themeId: String) {
        val theme = ThemeRegistry.getTheme(themeId)
        prefs.edit().putString(KEY_THEME_ID, theme.id).apply()
        _currentTheme.value = theme
    }

    /**
     * 快速在浅色与深色默认主题间切换
     */
    fun toggleDarkMode() {
        val nextId = if (_currentTheme.value.isDark) "ios_classic_light" else "ios_classic_dark"
        selectTheme(nextId)
    }
}

/**
 * CompositionLocal 用于向下透传当前的主题预设
 */
val LocalThemePreset = compositionLocalOf {
    ThemeRegistry.getTheme(AppThemeManager.DEFAULT_THEME_ID)
}
