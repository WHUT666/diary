package com.example.ui.poster

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class PosterThemeType(
    val title: String,
    val subtitle: String,
    val iconEmoji: String
) {
    PARCHMENT("极简信笺", "羊皮纸 · 素雅书卷", "📜"),
    MIDNIGHT("深邃午夜", "星空极光 · 沉浸暗夜", "🌌"),
    MATCHA("清风治愈", "森林抹茶 · 自然清爽", "🍵"),
    VINTAGE("复古胶片", "经典拍立得 · 温暖慢调", "📷")
}

data class DiaryPosterTheme(
    val type: PosterThemeType,
    val backgroundGradientColors: List<Color>,
    val cardBackground: Color,
    val cardBorderColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentColor: Color,
    val pillBackground: Color,
    val pillTextColor: Color,
    val sealBorderColor: Color,
    val sealTextColor: Color,
    val sealText: String,
    val defaultQuote: String,
    // Android Graphics Color Ints for Bitmap Canvas rendering
    val canvasBgGradientStart: Int,
    val canvasBgGradientEnd: Int,
    val canvasCardBg: Int,
    val canvasCardBorder: Int,
    val canvasTextPrimary: Int,
    val canvasTextSecondary: Int,
    val canvasAccent: Int,
    val canvasPillBg: Int,
    val canvasSealColor: Int
) {
    val backgroundBrush: Brush
        get() = Brush.verticalGradient(backgroundGradientColors)

    companion object {
        val Parchment = DiaryPosterTheme(
            type = PosterThemeType.PARCHMENT,
            backgroundGradientColors = listOf(Color(0xFFF9F6EE), Color(0xFFF2ECE1)),
            cardBackground = Color(0xFFFFFDF9),
            cardBorderColor = Color(0xFFE4DACE),
            textPrimary = Color(0xFF2E2721),
            textSecondary = Color(0xFF7A6F64),
            accentColor = Color(0xFFB84A39),
            pillBackground = Color(0xFFF2ECE1),
            pillTextColor = Color(0xFF594D42),
            sealBorderColor = Color(0xFFB84A39),
            sealTextColor = Color(0xFFB84A39),
            sealText = "日日是好日",
            defaultQuote = "心之所向，素履以往；生如逆旅，一苇以航。",
            canvasBgGradientStart = 0xFFF9F6EE.toInt(),
            canvasBgGradientEnd = 0xFFF2ECE1.toInt(),
            canvasCardBg = 0xFFFFFDF9.toInt(),
            canvasCardBorder = 0xFFE4DACE.toInt(),
            canvasTextPrimary = 0xFF2E2721.toInt(),
            canvasTextSecondary = 0xFF7A6F64.toInt(),
            canvasAccent = 0xFFB84A39.toInt(),
            canvasPillBg = 0xFFF2ECE1.toInt(),
            canvasSealColor = 0xFFB84A39.toInt()
        )

        val Midnight = DiaryPosterTheme(
            type = PosterThemeType.MIDNIGHT,
            backgroundGradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
            cardBackground = Color(0xFF141E33),
            cardBorderColor = Color(0xFF2E3D5C),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            accentColor = Color(0xFF38BDF8),
            pillBackground = Color(0xFF1E293B),
            pillTextColor = Color(0xFFE2E8F0),
            sealBorderColor = Color(0xFF38BDF8),
            sealTextColor = Color(0xFF38BDF8),
            sealText = "星河微光",
            defaultQuote = "星光不问赶路人，岁月不负有心人。",
            canvasBgGradientStart = 0xFF0F172A.toInt(),
            canvasBgGradientEnd = 0xFF1E293B.toInt(),
            canvasCardBg = 0xFF141E33.toInt(),
            canvasCardBorder = 0xFF2E3D5C.toInt(),
            canvasTextPrimary = 0xFFF8FAFC.toInt(),
            canvasTextSecondary = 0xFF94A3B8.toInt(),
            canvasAccent = 0xFF38BDF8.toInt(),
            canvasPillBg = 0xFF1E293B.toInt(),
            canvasSealColor = 0xFF38BDF8.toInt()
        )

        val Matcha = DiaryPosterTheme(
            type = PosterThemeType.MATCHA,
            backgroundGradientColors = listOf(Color(0xFFEFF5F0), Color(0xFFDFEDE1)),
            cardBackground = Color(0xFFF8FCF8),
            cardBorderColor = Color(0xFFCBE0D0),
            textPrimary = Color(0xFF1B3B2B),
            textSecondary = Color(0xFF52735E),
            accentColor = Color(0xFF2D6A4F),
            pillBackground = Color(0xFFE4EFE6),
            pillTextColor = Color(0xFF224E38),
            sealBorderColor = Color(0xFF2D6A4F),
            sealTextColor = Color(0xFF2D6A4F),
            sealText = "山川静谧",
            defaultQuote = "去生活，去经历，去把细碎的美好写进风里。",
            canvasBgGradientStart = 0xFFEFF5F0.toInt(),
            canvasBgGradientEnd = 0xFFDFEDE1.toInt(),
            canvasCardBg = 0xFFF8FCF8.toInt(),
            canvasCardBorder = 0xFFCBE0D0.toInt(),
            canvasTextPrimary = 0xFF1B3B2B.toInt(),
            canvasTextSecondary = 0xFF52735E.toInt(),
            canvasAccent = 0xFF2D6A4F.toInt(),
            canvasPillBg = 0xFFE4EFE6.toInt(),
            canvasSealColor = 0xFF2D6A4F.toInt()
        )

        val Vintage = DiaryPosterTheme(
            type = PosterThemeType.VINTAGE,
            backgroundGradientColors = listOf(Color(0xFFF6F0E6), Color(0xFFE8DCCB)),
            cardBackground = Color(0xFFFDFBF7),
            cardBorderColor = Color(0xFFDFCBB5),
            textPrimary = Color(0xFF3A2D23),
            textSecondary = Color(0xFF7E6E5F),
            accentColor = Color(0xFF8B4513),
            pillBackground = Color(0xFFEFE6D8),
            pillTextColor = Color(0xFF524032),
            sealBorderColor = Color(0xFF8B4513),
            sealTextColor = Color(0xFF8B4513),
            sealText = "时光留痕",
            defaultQuote = "把平凡的生活，过成浪漫的诗。",
            canvasBgGradientStart = 0xFFF6F0E6.toInt(),
            canvasBgGradientEnd = 0xFFE8DCCB.toInt(),
            canvasCardBg = 0xFFFDFBF7.toInt(),
            canvasCardBorder = 0xFFDFCBB5.toInt(),
            canvasTextPrimary = 0xFF3A2D23.toInt(),
            canvasTextSecondary = 0xFF7E6E5F.toInt(),
            canvasAccent = 0xFF8B4513.toInt(),
            canvasPillBg = 0xFFEFE6D8.toInt(),
            canvasSealColor = 0xFF8B4513.toInt()
        )

        val AllThemes = listOf(Parchment, Midnight, Matcha, Vintage)

        fun fromType(type: PosterThemeType): DiaryPosterTheme {
            return when (type) {
                PosterThemeType.PARCHMENT -> Parchment
                PosterThemeType.MIDNIGHT -> Midnight
                PosterThemeType.MATCHA -> Matcha
                PosterThemeType.VINTAGE -> Vintage
            }
        }
    }
}
