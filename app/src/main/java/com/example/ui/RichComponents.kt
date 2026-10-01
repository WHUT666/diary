package com.example.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Highlight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AvailableWeathers
import com.example.data.CitySearchResult
import com.example.data.LocationPresets
import com.example.data.RealTimeWeatherResult
import com.example.data.TemperaturePresets
import com.example.data.WeatherItem
import com.example.data.WeatherLocationService
import kotlinx.coroutines.launch

data class PresetCity(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String = "中国"
)

val PopularPresetCities = listOf(
    PresetCity("北京", 39.9042, 116.4074),
    PresetCity("上海", 31.2304, 121.4737),
    PresetCity("广州", 23.1291, 113.2644),
    PresetCity("深圳", 22.5431, 114.0579),
    PresetCity("杭州", 30.2741, 120.1551),
    PresetCity("成都", 30.5728, 104.0668),
    PresetCity("武汉", 30.5928, 114.3055),
    PresetCity("西安", 34.3416, 108.9398),
    PresetCity("南京", 32.0603, 118.7969),
    PresetCity("重庆", 29.5630, 106.5516),
    PresetCity("厦门", 24.4798, 118.0894),
    PresetCity("青岛", 36.0671, 120.3826),
    PresetCity("香港", 22.3193, 114.1694),
    PresetCity("台北", 25.0330, 121.5654),
    PresetCity("东京", 35.6762, 139.6503, "日本"),
    PresetCity("巴黎", 48.8566, 2.3522, "法国"),
    PresetCity("纽约", 40.7128, -74.0060, "美国"),
    PresetCity("伦敦", 51.5074, -0.1278, "英国")
)

/**
 * Rich Text Editing Toolbar
 */
@Composable
fun RichTextFormattingToolbar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onValueChange(insertBlock(textFieldValue, "\n![照片]($uri)\n"))
        }
    }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onValueChange(insertBlock(textFieldValue, "\n[video]($uri)\n"))
        }
    }
    
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onValueChange(insertBlock(textFieldValue, "\n[audio]($uri)\n"))
        }
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ToolbarIconButton(
                icon = Icons.Default.Image,
                description = "插入图片",
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            ToolbarIconButton(
                icon = Icons.Default.Movie,
                description = "插入视频",
                onClick = { videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }
            )
            ToolbarIconButton(
                icon = Icons.Default.Mic,
                description = "插入录音",
                onClick = { audioPickerLauncher.launch("audio/*") }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Spacer(modifier = Modifier.width(4.dp))
            
            ToolbarButton(
                label = "H1",
                onClick = { onValueChange(insertLinePrefix(textFieldValue, "# ")) }
            )
            ToolbarButton(
                label = "H2",
                onClick = { onValueChange(insertLinePrefix(textFieldValue, "## ")) }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatBold,
                description = "加粗",
                onClick = { onValueChange(wrapSelection(textFieldValue, "**", "**", "加粗文本")) }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatItalic,
                description = "斜体",
                onClick = { onValueChange(wrapSelection(textFieldValue, "*", "*", "斜体文本")) }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatStrikethrough,
                description = "删除线",
                onClick = { onValueChange(wrapSelection(textFieldValue, "~~", "~~", "划线文本")) }
            )
            ToolbarIconButton(
                icon = Icons.Outlined.Highlight,
                description = "高亮",
                onClick = { onValueChange(wrapSelection(textFieldValue, "==", "==", "高亮重点")) }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatQuote,
                description = "引用",
                onClick = { onValueChange(insertLinePrefix(textFieldValue, "> ")) }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatListBulleted,
                description = "无序列表",
                onClick = { onValueChange(insertLinePrefix(textFieldValue, "- ")) }
            )
            ToolbarIconButton(
                icon = Icons.Default.FormatListNumbered,
                description = "有序列表",
                onClick = { onValueChange(insertLinePrefix(textFieldValue, "1. ")) }
            )
            ToolbarButton(
                label = "代码",
                onClick = { onValueChange(wrapSelection(textFieldValue, "`", "`", "code")) }
            )
            ToolbarIconButton(
                icon = Icons.Default.HorizontalRule,
                description = "分割线",
                onClick = { onValueChange(insertBlock(textFieldValue, "\n---\n")) }
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp)
        )
    }
}

private fun wrapSelection(
    current: TextFieldValue,
    prefix: String,
    suffix: String,
    defaultPlaceholder: String
): TextFieldValue {
    val text = current.text
    val selection = current.selection

    return if (selection.collapsed) {
        val insertText = "$prefix$defaultPlaceholder$suffix"
        val newText = text.substring(0, selection.start) + insertText + text.substring(selection.end)
        val newCursor = selection.start + prefix.length
        TextFieldValue(
            text = newText,
            selection = TextRange(newCursor, newCursor + defaultPlaceholder.length)
        )
    } else {
        val selectedText = text.substring(selection.min, selection.max)
        val wrappedText = "$prefix$selectedText$suffix"
        val newText = text.substring(0, selection.min) + wrappedText + text.substring(selection.max)
        val newCursor = selection.min + wrappedText.length
        TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }
}

private fun insertLinePrefix(current: TextFieldValue, linePrefix: String): TextFieldValue {
    val text = current.text
    val selection = current.selection
    val start = selection.min

    val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let {
        if (it == -1) 0 else it + 1
    }

    val newText = text.substring(0, lineStart) + linePrefix + text.substring(lineStart)
    val newCursor = selection.start + linePrefix.length
    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
    )
}

private fun insertBlock(current: TextFieldValue, blockText: String): TextFieldValue {
    val text = current.text
    val selection = current.selection
    val newText = text.substring(0, selection.min) + blockText + text.substring(selection.max)
    val newCursor = selection.min + blockText.length
    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
    )
}

/**
 * Photo Picker & Thumbnail List
 */
@Composable
fun PhotoAttachmentSection(
    images: List<String>,
    onImagesChange: (List<String>) -> Unit,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newUris = uris.map { it.toString() }
            val combined = (images + newUris).distinct()
            onImagesChange(combined)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "照片记录 (${images.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.testTag("add_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "添加照片",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加照片", fontSize = 12.5.sp)
            }
        }

        if (images.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEachIndexed { index, uriString ->
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { onPhotoClick(uriString) }
                    ) {
                        AsyncImage(
                            model = uriString,
                            contentDescription = "日记照片 $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Delete button
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .clickable {
                                    val updated = images.toMutableList().also { it.removeAt(index) }
                                    onImagesChange(updated)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "删除照片",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Add button tile
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "添加",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * City Search & Preset Dialog
 * Free API: Open-Meteo Geocoding
 */
@Composable
fun CitySearchDialog(
    onDismiss: () -> Unit,
    onCitySelected: (RealTimeWeatherResult) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val weatherService = remember { WeatherLocationService(context) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<CitySearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isLoadingWeather by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(520.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "选择或搜索城市气象",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("输入城市名，如：杭州、成都、东京…", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("city_search_input"),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "清空", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (searchQuery.isBlank()) return@Button
                            coroutineScope.launch {
                                isSearching = true
                                statusMessage = "正在检索城市…"
                                val res = weatherService.searchCities(searchQuery)
                                isSearching = false
                                res.onSuccess {
                                    searchResults = it
                                    statusMessage = if (it.isEmpty()) "未找到匹配城市，请尝试常见城市名" else "已找到 ${it.size} 个匹配结果"
                                }.onFailure {
                                    statusMessage = "搜索失败，请检查网络"
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = searchQuery.isNotBlank() && !isSearching
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("搜索", fontSize = 13.sp)
                        }
                    }
                }

                // Status Message / Loading
                statusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isLoadingWeather) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("正在获取实时气象数据 (Open-Meteo)…", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // If search results exist, show them
                if (searchResults.isNotEmpty()) {
                    Text(
                        text = "搜索结果 (点击应用实时天气)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(searchResults) { city ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            isLoadingWeather = true
                                            val res = weatherService.fetchWeatherForCity(
                                                cityName = city.name,
                                                latitude = city.latitude,
                                                longitude = city.longitude
                                            )
                                            isLoadingWeather = false
                                            res.onSuccess { weatherRes ->
                                                onCitySelected(weatherRes)
                                                onDismiss()
                                            }.onFailure {
                                                statusMessage = "获取天气失败，请重试"
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = city.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${city.admin1.orEmpty()} ${city.country}".trim(),
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Popular Cities Section
                    Text(
                        text = "热门预设城市 (点击一键获取实时天气)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PopularPresetCities.chunked(3).forEach { rowCities ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowCities.forEach { city ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                coroutineScope.launch {
                                                    isLoadingWeather = true
                                                    val res = weatherService.fetchWeatherForCity(
                                                        cityName = city.name,
                                                        latitude = city.lat,
                                                        longitude = city.lon
                                                    )
                                                    isLoadingWeather = false
                                                    res.onSuccess { weatherRes ->
                                                        onCitySelected(weatherRes)
                                                        onDismiss()
                                                    }.onFailure {
                                                        statusMessage = "获取天气失败，请重试"
                                                    }
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = city.name,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Integrated Real-Time Weather & Location Module
 * - Free Real-time Location API (GPS + IP Geolocation Fallback)
 * - Free Real-time Weather API (Open-Meteo)
 * - Fully customizable: allows custom typing for location, temperature & weather selection.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealTimeWeatherLocationSection(
    currentLocation: String,
    onLocationChange: (String) -> Unit,
    selectedWeather: String,
    selectedWeatherLabel: String,
    temperature: String,
    onWeatherSelect: (WeatherItem) -> Unit,
    onTemperatureChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val weatherLocationService = remember { WeatherLocationService(context) }

    var isFetchingRealTime by remember { mutableStateOf(false) }
    var showCitySearchDialog by remember { mutableStateOf(false) }
    var showCustomLocDialog by remember { mutableStateOf(false) }
    var customLocInput by remember { mutableStateOf("") }
    var showCustomTempDialog by remember { mutableStateOf(false) }
    var customTempInput by remember { mutableStateOf("") }
    var realTimeSyncInfo by remember { mutableStateOf<String?>(null) }
    var realTimeSyncError by remember { mutableStateOf<String?>(null) }

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Whether granted or not, proceed to fetch (GPS if granted, IP if denied)
        coroutineScope.launch {
            isFetchingRealTime = true
            realTimeSyncError = null
            val result = weatherLocationService.fetchCurrentLocationAndWeather()
            isFetchingRealTime = false
            result.onSuccess { data ->
                onLocationChange(data.location)
                onWeatherSelect(data.weather)
                onTemperatureChange(data.temperature)
                realTimeSyncInfo = "已同步：${data.location} · ${data.weather.emoji} ${data.weather.label} ${data.temperature}" +
                        (if (data.humidity != null) " (湿度${data.humidity}%" else "") +
                        (if (data.windSpeed != null) " 风速${data.windSpeed}km/h)" else ")")
            }.onFailure { err ->
                realTimeSyncError = "获取气象失败：${err.localizedMessage ?: "网络或定位异常"}"
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Module Title & Subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "地点与天气",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "实时API · 支持自定义",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Row: Real-time One-click Fetch & City Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                enabled = !isFetchingRealTime,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("fetch_realtime_weather_button")
            ) {
                if (isFetchingRealTime) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("获取中…", fontSize = 12.5.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("实时获取当地气象", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = { showCitySearchDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_city_weather_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("搜索城市", fontSize = 12.5.sp)
            }
        }

        // Real-time sync feedback banner
        realTimeSyncInfo?.let { info ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🟢 $info",
                        fontSize = 11.5.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { realTimeSyncInfo = null },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        // Real-time error feedback banner
        realTimeSyncError?.let { err ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚠️ $err",
                        fontSize = 11.5.sp,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { realTimeSyncError = null },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = Color(0xFFC62828), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Location Section (Chips + Customization)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (currentLocation.isNotBlank()) "当前地点: $currentLocation" else "记录地点",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (currentLocation.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (currentLocation.isNotBlank()) {
                TextButton(onClick = { onLocationChange("") }) {
                    Text("清除", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = {
                    customLocInput = currentLocation
                    showCustomLocDialog = true
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AddLocation,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("自定义地点", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            )

            LocationPresets.forEach { preset ->
                val selected = currentLocation == preset
                AssistChip(
                    onClick = {
                        onLocationChange(if (selected) "" else preset)
                    },
                    label = { Text(preset, fontSize = 11.5.sp) },
                    colors = if (selected) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Weather Condition Row (All weather emojis + Custom)
        Text(
            text = "天气状况: $selectedWeather $selectedWeatherLabel",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AvailableWeathers.forEach { weatherItem ->
                val selected = selectedWeather == weatherItem.emoji
                FilterChip(
                    selected = selected,
                    onClick = {
                        onWeatherSelect(weatherItem)
                        if (temperature.isBlank()) {
                            onTemperatureChange(weatherItem.defaultTemp)
                        }
                    },
                    label = {
                        Text("${weatherItem.emoji} ${weatherItem.label}", fontSize = 12.sp)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Temperature Row (Presets + Custom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (temperature.isNotBlank()) "气温: $temperature" else "气温:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AssistChip(
                onClick = {
                    customTempInput = temperature
                    showCustomTempDialog = true
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                },
                label = { Text("自定义气温", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                )
            )

            TemperaturePresets.forEach { temp ->
                val selected = temperature == temp
                AssistChip(
                    onClick = { onTemperatureChange(temp) },
                    label = { Text(temp, fontSize = 11.5.sp) },
                    colors = if (selected) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    }
                )
            }
        }
    }

    // Custom Location Dialog
    if (showCustomLocDialog) {
        AlertDialog(
            onDismissRequest = { showCustomLocDialog = false },
            title = { Text("输入自定义地点", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customLocInput,
                    onValueChange = { customLocInput = it },
                    label = { Text("例如：上海 · 静安公园、西湖断桥、家里书房") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLocationChange(customLocInput.trim())
                        showCustomLocDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomLocDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Custom Temperature Dialog
    if (showCustomTempDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTempDialog = false },
            title = { Text("输入自定义气温", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customTempInput,
                    onValueChange = { customTempInput = it },
                    label = { Text("例如：24°C、18~25°C、-3°C") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTemperatureChange(customTempInput.trim())
                        showCustomTempDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTempDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // City Search Dialog
    if (showCitySearchDialog) {
        CitySearchDialog(
            onDismiss = { showCitySearchDialog = false },
            onCitySelected = { result ->
                onLocationChange(result.location)
                onWeatherSelect(result.weather)
                onTemperatureChange(result.temperature)
                realTimeSyncInfo = "已切换城市：${result.location} · ${result.weather.emoji} ${result.weather.label} ${result.temperature}"
                showCitySearchDialog = false
            }
        )
    }
}

/**
 * Location Selector & Chip Row (Compatibility wrapper)
 */
@Composable
fun LocationSelectorSection(
    currentLocation: String,
    onLocationChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (currentLocation.isNotBlank()) "地点: $currentLocation" else "记录地点",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (currentLocation.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (currentLocation.isNotBlank()) {
                TextButton(onClick = { onLocationChange("") }) {
                    Text("清除", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = {
                    customText = currentLocation
                    showCustomDialog = true
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AddLocation,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("自定义地点", fontSize = 12.sp) }
            )

            LocationPresets.forEach { preset ->
                val selected = currentLocation == preset
                AssistChip(
                    onClick = {
                        onLocationChange(if (selected) "" else preset)
                    },
                    label = { Text(preset, fontSize = 12.sp) },
                    colors = if (selected) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    }
                )
            }
        }
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("输入自定义地点", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    label = { Text("例如：上海 · 静安公园、家里书房") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLocationChange(customText.trim())
                        showCustomDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * Weather and Temperature Selector (Compatibility wrapper)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherTemperatureSection(
    selectedWeather: String,
    selectedWeatherLabel: String,
    temperature: String,
    onWeatherSelect: (WeatherItem) -> Unit,
    onTemperatureChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTempDialog by remember { mutableStateOf(false) }
    var customTempInput by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "天气与气温",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Weather scrollable row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AvailableWeathers.forEach { weatherItem ->
                val selected = selectedWeather == weatherItem.emoji
                FilterChip(
                    selected = selected,
                    onClick = {
                        onWeatherSelect(weatherItem)
                        if (temperature.isBlank()) {
                            onTemperatureChange(weatherItem.defaultTemp)
                        }
                    },
                    label = {
                        Text("${weatherItem.emoji} ${weatherItem.label}", fontSize = 12.sp)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Temperature row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (temperature.isNotBlank()) temperature else "气温:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TemperaturePresets.forEach { temp ->
                val selected = temperature == temp
                AssistChip(
                    onClick = { onTemperatureChange(temp) },
                    label = { Text(temp, fontSize = 11.5.sp) },
                    colors = if (selected) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    }
                )
            }

            AssistChip(
                onClick = {
                    customTempInput = temperature
                    showTempDialog = true
                },
                label = { Text("自定义", fontSize = 11.5.sp) }
            )
        }
    }

    if (showTempDialog) {
        AlertDialog(
            onDismissRequest = { showTempDialog = false },
            title = { Text("设定气温", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customTempInput,
                    onValueChange = { customTempInput = it },
                    label = { Text("例如：24°C 或 18~25°C") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTemperatureChange(customTempInput.trim())
                        showTempDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTempDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * Home Screen Top Real-Time Weather & Location Card
 */
@Composable
fun RealTimeWeatherHomeBanner(
    onOpenEditor: (location: String?, weatherEmoji: String?, weatherLabel: String?, temp: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val weatherService = remember { WeatherLocationService(context) }

    var isFetching by remember { mutableStateOf(false) }
    var weatherResult by remember { mutableStateOf<RealTimeWeatherResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        coroutineScope.launch {
            isFetching = true
            errorMessage = null
            val res = weatherService.fetchCurrentLocationAndWeather()
            isFetching = false
            res.onSuccess {
                weatherResult = it
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "获取失败"
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        shadowElevation = 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (weatherResult == null && !isFetching) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
            ) {
                if (isFetching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "正在获取当地实时气象…",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (weatherResult != null) {
                    val w = weatherResult!!
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = w.weather.emoji, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = w.location,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "${w.weather.label} ${w.temperature}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = listOfNotNull(
                                w.humidity?.let { "相对湿度 $it%" },
                                w.windSpeed?.let { "风速 ${it}km/h" }
                            ).ifEmpty { listOf("今日天气良好，适合记录日常") }.joinToString(" · "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "开启今日实时气象与足迹",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "点击一键获取当地天气、气温与位置",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (weatherResult != null && !isFetching) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isFetching = true
                                val res = weatherService.fetchCurrentLocationAndWeather()
                                isFetching = false
                                res.onSuccess { weatherResult = it }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新天气",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                FilledTonalButton(
                    onClick = {
                        val w = weatherResult
                        onOpenEditor(w?.location, w?.weather?.emoji, w?.weather?.label, w?.temperature)
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("写此刻", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Fullscreen Image Viewer Dialog
 */
@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "全屏大图",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }
    }
}
