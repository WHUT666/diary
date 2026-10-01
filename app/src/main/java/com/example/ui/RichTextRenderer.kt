package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import coil.compose.AsyncImage
import com.example.ui.audio.AudioNotePlayerCard

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Quote(val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class CodeBlock(val code: String) : MarkdownBlock()
    data class Image(val alt: String, val url: String) : MarkdownBlock()
    data class Video(val url: String) : MarkdownBlock()
    data class Audio(val url: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

object MarkdownParser {
    fun parse(rawText: String): List<MarkdownBlock> {
        val lines = rawText.lines()
        val blocks = mutableListOf<MarkdownBlock>()
        var inCodeBlock = false
        val codeBuffer = StringBuilder()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    blocks.add(MarkdownBlock.CodeBlock(codeBuffer.toString().trimEnd()))
                    codeBuffer.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                i++
                continue
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append("\n")
                i++
                continue
            }

            if (trimmed.isEmpty()) {
                i++
                continue
            }

            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                blocks.add(MarkdownBlock.Divider)
                i++
                continue
            }

            if (trimmed.startsWith("### ")) {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ").trim()))
                i++
                continue
            }
            if (trimmed.startsWith("## ")) {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ").trim()))
                i++
                continue
            }
            if (trimmed.startsWith("# ")) {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ").trim()))
                i++
                continue
            }

            if (trimmed.startsWith("> ") || trimmed == ">") {
                val quoteLines = mutableListOf<String>()
                quoteLines.add(trimmed.removePrefix(">").trim())
                i++
                while (i < lines.size && (lines[i].trim().startsWith(">") || (lines[i].isNotBlank() && !lines[i].trim().startsWith("#") && !lines[i].trim().startsWith("-")))) {
                    val nextLine = lines[i].trim()
                    if (nextLine.startsWith(">")) {
                        quoteLines.add(nextLine.removePrefix(">").trim())
                    } else {
                        quoteLines.add(nextLine)
                    }
                    i++
                }
                blocks.add(MarkdownBlock.Quote(quoteLines.filter { it.isNotBlank() }.joinToString("\n")))
                continue
            }

            if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
                val bulletText = trimmed.substring(2).trim()
                blocks.add(MarkdownBlock.BulletItem(bulletText))
                i++
                continue
            }

            val numberedMatch = Regex("""^(\d+)\.\s+(.*)$""").find(trimmed)
            if (numberedMatch != null) {
                val number = numberedMatch.groupValues[1]
                val text = numberedMatch.groupValues[2]
                blocks.add(MarkdownBlock.NumberedItem(number, text))
                i++
                continue
            }

            // Regular paragraph (combine contiguous non-empty lines)
            val paraLines = mutableListOf<String>()
            paraLines.add(line)
            i++
            while (i < lines.size) {
                val nextLine = lines[i]
                val nextTrimmed = nextLine.trim()
                if (nextTrimmed.isEmpty() ||
                    nextTrimmed.startsWith("#") ||
                    nextTrimmed.startsWith(">") ||
                    nextTrimmed.startsWith("- ") ||
                    nextTrimmed.startsWith("* ") ||
                    nextTrimmed.startsWith("```") ||
                    nextTrimmed == "---" ||
                    Regex("""^(\d+)\.\s+""").containsMatchIn(nextTrimmed)
                ) {
                    break
                }
                paraLines.add(nextLine)
                i++
            }
            
            val paraString = paraLines.joinToString("\n")
            val mediaRegex = Regex("""(!\[(.*?)\]\((.*?)\))|(\[video\]\((.*?)\))|(\[audio\]\((.*?)\))""")
            val matches = mediaRegex.findAll(paraString).toList()
            if (matches.isNotEmpty()) {
                var lastIndex = 0
                matches.forEach { match ->
                    val textBefore = paraString.substring(lastIndex, match.range.first).trim()
                    if (textBefore.isNotEmpty()) {
                        blocks.add(MarkdownBlock.Paragraph(textBefore))
                    }
                    if (match.groups[1] != null) {
                        blocks.add(MarkdownBlock.Image(match.groups[2]?.value ?: "", match.groups[3]?.value ?: ""))
                    } else if (match.groups[4] != null) {
                        blocks.add(MarkdownBlock.Video(match.groups[5]?.value ?: ""))
                    } else if (match.groups[6] != null) {
                        blocks.add(MarkdownBlock.Audio(match.groups[7]?.value ?: ""))
                    }
                    lastIndex = match.range.last + 1
                }
                val textAfter = paraString.substring(lastIndex).trim()
                if (textAfter.isNotEmpty()) {
                    blocks.add(MarkdownBlock.Paragraph(textAfter))
                }
            } else {
                blocks.add(MarkdownBlock.Paragraph(paraString))
            }
        }

        if (inCodeBlock && codeBuffer.isNotEmpty()) {
            blocks.add(MarkdownBlock.CodeBlock(codeBuffer.toString().trimEnd()))
        }

        return blocks
    }

    fun parseInline(text: String, highlightColor: Color): AnnotatedString {
        return buildAnnotatedString {
            var index = 0
            val length = text.length

            while (index < length) {
                // Bold: **text**
                if (text.startsWith("**", index)) {
                    val end = text.indexOf("**", index + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(index + 2, end))
                        }
                        index = end + 2
                        continue
                    }
                }

                // Strikethrough: ~~text~~
                if (text.startsWith("~~", index)) {
                    val end = text.indexOf("~~", index + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(text.substring(index + 2, end))
                        }
                        index = end + 2
                        continue
                    }
                }

                // Highlight: ==text==
                if (text.startsWith("==", index)) {
                    val end = text.indexOf("==", index + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(background = highlightColor.copy(alpha = 0.25f), fontWeight = FontWeight.SemiBold)) {
                            append(text.substring(index + 2, end))
                        }
                        index = end + 2
                        continue
                    }
                }

                // Inline code: `code`
                if (text[index] == '`') {
                    val end = text.indexOf('`', index + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0x18888888),
                                fontSize = 14.sp
                            )
                        ) {
                            append(" " + text.substring(index + 1, end) + " ")
                        }
                        index = end + 1
                        continue
                    }
                }

                // Italic: *text* (single star, not double)
                if (text[index] == '*' && (index == 0 || text[index - 1] != '*') && (index + 1 < length && text[index + 1] != '*')) {
                    val end = text.indexOf('*', index + 1)
                    if (end != -1 && (end + 1 >= length || text[end + 1] != '*')) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(index + 1, end))
                        }
                        index = end + 1
                        continue
                    }
                }

                append(text[index])
                index++
            }
        }
    }
}

@Composable
fun RichTextDocument(
    content: String,
    modifier: Modifier = Modifier,
    onImageClick: ((String) -> Unit)? = null
) {
    val blocks = remember(content) { MarkdownParser.parse(content) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val (fontSize, fontWeight, topPadding) = when (block.level) {
                        1 -> Triple(22.sp, FontWeight.Bold, if (index == 0) 0.dp else 16.dp)
                        2 -> Triple(18.sp, FontWeight.Bold, if (index == 0) 0.dp else 12.dp)
                        else -> Triple(16.sp, FontWeight.SemiBold, if (index == 0) 0.dp else 8.dp)
                    }
                    val inline = remember(block.text) { MarkdownParser.parseInline(block.text, primaryColor) }
                    Spacer(modifier = Modifier.height(topPadding))
                    Text(
                        text = inline,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = (fontSize.value * 1.35f).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                is MarkdownBlock.Paragraph -> {
                    val inline = remember(block.text) { MarkdownParser.parseInline(block.text, primaryColor) }
                    Text(
                        text = inline,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                is MarkdownBlock.Quote -> {
                    val inline = remember(block.text) { MarkdownParser.parseInline(block.text, primaryColor) }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = inline,
                                fontSize = 14.5.sp,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                is MarkdownBlock.BulletItem -> {
                    val inline = remember(block.text) { MarkdownParser.parseInline(block.text, primaryColor) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = inline,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    val inline = remember(block.text) { MarkdownParser.parseInline(block.text, primaryColor) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = block.number,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = inline,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = block.code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
                is MarkdownBlock.Image -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (onImageClick != null) Modifier.clickable { onImageClick(block.url) }
                                else Modifier
                            )
                    ) {
                        AsyncImage(
                            model = block.url,
                            contentDescription = block.alt,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is MarkdownBlock.Video -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AndroidView(
                            factory = { context ->
                                VideoView(context).apply {
                                    setVideoURI(Uri.parse(block.url))
                                    val mediaController = MediaController(context)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        )
                    }
                }
                is MarkdownBlock.Audio -> {
                    Box(modifier = Modifier.padding(vertical = 8.dp)) {
                        AudioNotePlayerCard(
                            audioPath = block.url,
                            audioDurationSec = 0
                        )
                    }
                }
            }
        }
    }
}
