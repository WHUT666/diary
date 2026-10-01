package com.example.ui.poster

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.DiaryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object DiaryPosterExporter {

    private const val POSTER_WIDTH = 1080
    private const val CARD_MARGIN = 64f
    private const val CARD_PADDING = 54f
    private const val CARD_RADIUS = 36f

    /**
     * Renders a high-resolution (1080px wide) artistic poster bitmap from a diary entry.
     */
    suspend fun generatePosterBitmap(
        context: Context,
        entry: DiaryEntry,
        theme: DiaryPosterTheme
    ): Bitmap = withContext(Dispatchers.IO) {
        val cardWidth = POSTER_WIDTH - (CARD_MARGIN * 2)
        val contentWidth = cardWidth - (CARD_PADDING * 2)

        // 1. Prepare Paints
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasTextPrimary
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasTextPrimary
            textSize = 52f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val dateNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasAccent
            textSize = 102f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val dateSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasTextSecondary
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasTextSecondary
            textSize = 32f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }

        // 2. Pre-calculate text layout heights
        val titleLayout = if (entry.title.isNotBlank()) {
            buildStaticLayout(entry.title, titlePaint, contentWidth.toInt())
        } else null

        val bodyLayout = buildStaticLayout(
            entry.cleanPosterContent.ifBlank { "记录美好生活……" },
            textPaint,
            contentWidth.toInt()
        )

        val quoteLayout = buildStaticLayout(
            "“${theme.defaultQuote}”",
            quotePaint,
            contentWidth.toInt()
        )

        // 3. Photos loading (supports allImages from attachments and markdown)
        val loadedPhotos = mutableListOf<Bitmap>()
        val photoUris = entry.allImages
        if (photoUris.isNotEmpty()) {
            val maxPhotos = min(photoUris.size, 2)
            val imageLoader = Coil.imageLoader(context)
            for (i in 0 until maxPhotos) {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(photoUris[i])
                        .allowHardware(false) // Required for software canvas rendering!
                        .build()
                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val bmp = drawableToBitmap(result.drawable)
                        loadedPhotos.add(bmp)
                    }
                } catch (_: Throwable) {
                    // Fallback to ContentResolver if Coil fails
                    try {
                        val uri = Uri.parse(photoUris[i])
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val original = BitmapFactory.decodeStream(stream)
                            if (original != null) {
                                loadedPhotos.add(original)
                            }
                        }
                    } catch (_: Throwable) {}
                }
            }
        }

        val photoSectionHeight = if (loadedPhotos.isNotEmpty()) 460f else 0f

        // 4. Calculate total dynamic height
        var dynamicHeight = CARD_MARGIN * 2 + CARD_PADDING * 2
        dynamicHeight += 160f // Header (Day number + date info)
        dynamicHeight += 40f  // Space
        dynamicHeight += 70f  // Badges row
        dynamicHeight += 40f  // Space

        if (titleLayout != null) {
            dynamicHeight += titleLayout.height + 30f
        }

        dynamicHeight += bodyLayout.height + 40f

        if (photoSectionHeight > 0) {
            dynamicHeight += photoSectionHeight + 40f
        }

        dynamicHeight += 60f // Divider
        dynamicHeight += quoteLayout.height + 40f
        dynamicHeight += 140f // Seal stamp and footer watermark

        // Minimum height ensuring beautiful poster proportions (at least 1350px)
        val finalHeight = max(1350, dynamicHeight.toInt())
        val bitmap = Bitmap.createBitmap(POSTER_WIDTH, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 5. Draw Outer Background Gradient
        val bgShader = LinearGradient(
            0f, 0f, 0f, finalHeight.toFloat(),
            theme.canvasBgGradientStart,
            theme.canvasBgGradientEnd,
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply { shader = bgShader }
        canvas.drawRect(0f, 0f, POSTER_WIDTH.toFloat(), finalHeight.toFloat(), bgPaint)

        // 6. Draw Main Card Box with rounded corners and subtle border
        val cardRect = RectF(
            CARD_MARGIN,
            CARD_MARGIN,
            POSTER_WIDTH - CARD_MARGIN,
            finalHeight - CARD_MARGIN
        )
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasCardBg
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, CARD_RADIUS, CARD_RADIUS, cardPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasCardBorder
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(cardRect, CARD_RADIUS, CARD_RADIUS, borderPaint)

        // 7. Paint Card Contents
        var currY = CARD_MARGIN + CARD_PADDING
        val leftX = CARD_MARGIN + CARD_PADDING

        // Header: Day Number + Month Year / Weekday
        val dayString = entry.dayOfMonth
        canvas.drawText(dayString, leftX, currY + 85f, dateNumberPaint)

        val dateInfoX = leftX + dateNumberPaint.measureText(dayString) + 32f
        val sdfMonthYear = SimpleDateFormat("yyyy.MM", Locale.getDefault())
        val monthYearStr = sdfMonthYear.format(Date(entry.createdAt))
        canvas.drawText(monthYearStr, dateInfoX, currY + 38f, dateSubPaint)
        canvas.drawText("${entry.dayOfWeek} · ${entry.formattedTime}", dateInfoX, currY + 80f, dateSubPaint)

        currY += 120f

        // Thin subtle line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasCardBorder
            strokeWidth = 1.5f
        }
        canvas.drawLine(leftX, currY, leftX + contentWidth, currY, linePaint)
        currY += 32f

        // Badges Row (Mood, Weather, Category, Location)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasPillBg
            style = Paint.Style.FILL
        }
        val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasTextPrimary
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        var badgeX = leftX
        val badges = mutableListOf<String>()
        badges.add("${entry.mood} ${entry.moodLabel}")
        if (entry.weather.isNotBlank()) {
            val temp = if (entry.temperature.isNotBlank()) " ${entry.temperature}" else ""
            badges.add("${entry.weather} ${entry.weatherLabel}$temp")
        }
        badges.add("🔖 ${entry.category}")
        if (entry.location.isNotBlank()) {
            val locShort = if (entry.location.length > 8) entry.location.take(8) + "…" else entry.location
            badges.add("📍 $locShort")
        }

        for (badge in badges) {
            val textWidth = pillTextPaint.measureText(badge)
            val pillWidth = textWidth + 36f
            if (badgeX + pillWidth <= leftX + contentWidth) {
                val pillRect = RectF(badgeX, currY, badgeX + pillWidth, currY + 52f)
                canvas.drawRoundRect(pillRect, 26f, 26f, pillPaint)
                canvas.drawText(badge, badgeX + 18f, currY + 36f, pillTextPaint)
                badgeX += pillWidth + 16f
            }
        }

        currY += 80f

        // Title
        if (titleLayout != null) {
            canvas.save()
            canvas.translate(leftX, currY)
            titleLayout.draw(canvas)
            canvas.restore()
            currY += titleLayout.height + 30f
        }

        // Body Content
        canvas.save()
        canvas.translate(leftX, currY)
        bodyLayout.draw(canvas)
        canvas.restore()
        currY += bodyLayout.height + 40f

        // Photos Grid (if any)
        if (loadedPhotos.isNotEmpty()) {
            val photoHeight = 440f
            if (loadedPhotos.size == 1) {
                val photo = loadedPhotos[0]
                val dstRect = RectF(leftX, currY, leftX + contentWidth, currY + photoHeight)
                drawScaledBitmap(canvas, photo, dstRect, 20f)
            } else {
                val spacing = 16f
                val singleWidth = (contentWidth - spacing) / 2
                val rect1 = RectF(leftX, currY, leftX + singleWidth, currY + photoHeight)
                val rect2 = RectF(leftX + singleWidth + spacing, currY, leftX + contentWidth, currY + photoHeight)
                drawScaledBitmap(canvas, loadedPhotos[0], rect1, 20f)
                drawScaledBitmap(canvas, loadedPhotos[1], rect2, 20f)
            }
            currY += photoHeight + 40f
        }

        // Decorative Footer Divider
        canvas.drawLine(leftX, currY, leftX + contentWidth, currY, linePaint)
        currY += 32f

        // Quote
        canvas.save()
        canvas.translate(leftX, currY)
        quoteLayout.draw(canvas)
        canvas.restore()
        currY += quoteLayout.height + 40f

        // Seal Stamp & App Branding Watermark at bottom
        val sealSize = 90f
        val sealX = leftX + contentWidth - sealSize - 10f
        val sealY = currY

        // Draw Chinese Seal Stamp
        val sealBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasSealColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val sealRect = RectF(sealX, sealY, sealX + sealSize, sealY + sealSize)
        canvas.drawRoundRect(sealRect, 12f, 12f, sealBorderPaint)

        val sealInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasSealColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val sealInnerRect = RectF(sealX + 4f, sealY + 4f, sealX + sealSize - 4f, sealY + sealSize - 4f)
        canvas.drawRoundRect(sealInnerRect, 8f, 8f, sealInnerPaint)

        val sealTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasSealColor
            textSize = 24f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        // Draw 2-character stacked or small text
        val sealChar1 = theme.sealText.take(2)
        val sealChar2 = theme.sealText.drop(2).take(2)
        canvas.drawText(sealChar1, sealX + 18f, sealY + 40f, sealTextPaint)
        if (sealChar2.isNotEmpty()) {
            canvas.drawText(sealChar2, sealX + 18f, sealY + 70f, sealTextPaint)
        }

        // App Branding Text on Left
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasTextPrimary
            textSize = 28f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subBrandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.canvasTextSecondary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("心语日记 · 本地私密生活志", leftX, currY + 36f, brandPaint)
        canvas.drawText("每一段时光，都值得被温柔以待", leftX, currY + 70f, subBrandPaint)

        bitmap
    }

    private fun drawScaledBitmap(canvas: Canvas, source: Bitmap, dst: RectF, radius: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.save()
        val path = android.graphics.Path().apply {
            addRoundRect(dst, radius, radius, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(path)

        // Center crop math
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val dstW = dst.width()
        val dstH = dst.height()

        val scale = max(dstW / srcW, dstH / srcH)
        val scaledW = srcW * scale
        val scaledH = srcH * scale
        val left = dst.left + (dstW - scaledW) / 2f
        val top = dst.top + (dstH - scaledH) / 2f

        val renderRect = RectF(left, top, left + scaledW, top + scaledH)
        canvas.drawBitmap(source, null, renderRect, paint)
        canvas.restore()
    }

    private fun buildStaticLayout(text: CharSequence, paint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.4f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text, paint, width,
                Layout.Alignment.ALIGN_NORMAL,
                1.4f, 0f, true
            )
        }
    }

    /**
     * Saves the poster bitmap into the Android system media library (Pictures album).
     */
    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Result<Uri> =
        withContext(Dispatchers.IO) {
            try {
                val filename = "Diary_${System.currentTimeMillis()}.png"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DiaryPosters")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("无法创建相册存储路径"))

                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                } ?: return@withContext Result.failure(Exception("无法打开文件输出流"))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                Result.success(uri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Shares the poster bitmap directly via Android Intent chooser with FileProvider.
     */
    suspend fun shareBitmap(context: Context, bitmap: Bitmap, entry: DiaryEntry): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, "shared_posters").apply { mkdirs() }
                val posterFile = File(cacheDir, "diary_poster_${entry.id}_${System.currentTimeMillis()}.png")

                FileOutputStream(posterFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    posterFile
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, entry.title.ifBlank { "日记记录" })
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(sendIntent, "分享精美日记长图").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
