package com.example

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ThemePreset
import com.example.ui.theme.ThemeRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeRegistryTest {

    @Test
    fun testDefaultThemesRegistered() {
        val themes = ThemeRegistry.getAllThemes()
        assertTrue("At least 7 default themes should be registered", themes.size >= 7)

        val classicLight = ThemeRegistry.getTheme("ios_classic_light")
        assertEquals("ios_classic_light", classicLight.id)
        assertEquals("iOS 经典明朗", classicLight.name)
        assertEquals(false, classicLight.isDark)

        val classicDark = ThemeRegistry.getTheme("ios_classic_dark")
        assertEquals("ios_classic_dark", classicDark.id)
        assertEquals("iOS 深邃夜黑", classicDark.name)
        assertEquals(true, classicDark.isDark)
    }

    @Test
    fun testFallbackToDefaultWhenThemeNotFound() {
        val unknownTheme = ThemeRegistry.getTheme("non_existent_id")
        assertNotNull(unknownTheme)
        assertEquals("ios_classic_light", unknownTheme.id)
    }

    @Test
    fun testDynamicThemeRegistrationForFutureUpdates() {
        val newCustomTheme = ThemePreset(
            id = "custom_cyberpunk_neon",
            name = "赛博霓虹",
            subtitle = "未来荧光粉紫",
            description = "赛博朋克极夜霓虹风格",
            isDark = true,
            accentColor = Color(0xFFFF007F),
            secondaryColor = Color(0xFF00F0FF),
            backgroundColor = Color(0xFF05050A),
            surfaceColor = Color(0xFF10101A),
            surfaceVariantColor = Color(0xFF1C1C2E),
            onBackgroundColor = Color(0xFFFFFFFF),
            onSurfaceColor = Color(0xFFFFFFFF),
            outlineColor = Color(0xFF2A2A44),
            cardElevation = 2.dp,
            tag = "社区定制",
            colorScheme = lightColorScheme(
                primary = Color(0xFFFF007F),
                background = Color(0xFF05050A),
                surface = Color(0xFF10101A)
            )
        )

        // 注册新主题
        ThemeRegistry.register(newCustomTheme)

        val retrieved = ThemeRegistry.getTheme("custom_cyberpunk_neon")
        assertEquals("custom_cyberpunk_neon", retrieved.id)
        assertEquals("赛博霓虹", retrieved.name)
        assertEquals("未来荧光粉紫", retrieved.subtitle)
        assertEquals(true, retrieved.isDark)
    }
}
