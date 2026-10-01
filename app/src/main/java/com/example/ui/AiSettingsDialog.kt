package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.AiConfig
import com.example.ai.AiConfigManager
import com.example.ai.AiProvider
import com.example.ai.AiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsDialog(
    configManager: AiConfigManager,
    aiService: AiService,
    onDismiss: () -> Unit,
    onConfigSaved: (AiConfig) -> Unit
) {
    val context = LocalContext.current
    val currentConfig = remember { configManager.loadConfig() }

    var selectedProvider by remember { mutableStateOf(currentConfig.provider) }
    var geminiApiKey by remember { mutableStateOf(currentConfig.geminiApiKey) }
    var geminiModel by remember { mutableStateOf(currentConfig.geminiModel) }

    var customBaseUrl by remember { mutableStateOf(currentConfig.customBaseUrl) }
    var customApiKey by remember { mutableStateOf(currentConfig.customApiKey) }
    var customModel by remember { mutableStateOf(currentConfig.customModel) }
    var temperature by remember { mutableFloatStateOf(currentConfig.temperature) }

    var isApiKeyVisible by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testIsSuccess by remember { mutableStateOf<Boolean?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI 智能模型配置",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("ai_settings_close")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "支持对接 Google Gemini 或自定义任意 OpenAI 兼容大模型（如 DeepSeek、通义千问、Ollama 本地模型等）。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Provider Choice
                Text(
                    text = "选择 AI 服务提供方",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedProvider == AiProvider.GEMINI,
                        onClick = { selectedProvider = AiProvider.GEMINI },
                        label = { Text("Google Gemini") },
                        modifier = Modifier.weight(1f).testTag("provider_gemini"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    FilterChip(
                        selected = selectedProvider == AiProvider.CUSTOM_OPENAI,
                        onClick = { selectedProvider = AiProvider.CUSTOM_OPENAI },
                        label = { Text("自定义/兼容模型") },
                        modifier = Modifier.weight(1f).testTag("provider_custom"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specific Fields based on Provider
                if (selectedProvider == AiProvider.GEMINI) {
                    // Gemini fields
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Gemini API Key",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = geminiApiKey,
                                onValueChange = { geminiApiKey = it },
                                placeholder = { Text("若已在 Secrets 配置可留空") },
                                singleLine = true,
                                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                        Icon(
                                            imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_api_key_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "选择模型型号",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-3.1-flash-lite-preview").forEach { m ->
                                    FilterChip(
                                        selected = geminiModel == m,
                                        onClick = { geminiModel = m },
                                        label = { Text(m) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom / OpenAI Compatible fields
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "快速预设填充（含主流免费接口）：",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        customBaseUrl = "https://open.bigmodel.cn/api/paas/v4"
                                        customModel = "glm-4-flash"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("智谱(免费)", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        customBaseUrl = "https://api.siliconflow.cn/v1"
                                        customModel = "Qwen/Qwen2.5-7B-Instruct"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("硅基流动(免费模型)", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        customBaseUrl = "https://api.groq.com/openai/v1"
                                        customModel = "llama-3.3-70b-versatile"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("Groq(高速免费)", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        customBaseUrl = "https://api.deepseek.com/v1"
                                        customModel = "deepseek-chat"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("DeepSeek", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        customBaseUrl = "https://api.openai.com/v1"
                                        customModel = "gpt-4o-mini"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("OpenAI", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        customBaseUrl = "http://10.0.2.2:11434/v1"
                                        customModel = "llama3"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("Ollama 本地", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Base URL
                            Text(
                                text = "API 接口地址 (Base URL)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customBaseUrl,
                                onValueChange = { customBaseUrl = it },
                                placeholder = { Text("例如：https://api.deepseek.com/v1") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_base_url_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom API Key
                            Text(
                                text = "API 密钥 (API Key)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customApiKey,
                                onValueChange = { customApiKey = it },
                                placeholder = { Text("sk-xxxx（本地模型无密钥可留空）") },
                                singleLine = true,
                                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                        Icon(
                                            imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_api_key_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Model Name
                            Text(
                                text = "模型代号 (Model Name)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customModel,
                                onValueChange = { customModel = it },
                                placeholder = { Text("例如：deepseek-chat, gpt-4o-mini") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_model_input")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Free API Guide Info Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 免费 AI API 推荐与内置机制",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. 智谱 GLM-4-Flash：国内直连免翻墙，官方宣布永久免费调用，点上方预设即可快速填入！\n2. 硅基流动 (SiliconFlow)：内置多款 7B/开源模型永久免费白嫖。\n3. Google Gemini：官方每天提供 1500 次免费调用配额。\n4. 内置离线引擎：即使未配 API Key 或断网，应用内也集成了本地启发式智能引擎，日记分析与待办拆解依然可用！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Temperature Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "生成创意度 (Temperature)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "%.1f".format(temperature),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0.1f..1.0f,
                    steps = 8
                )

                // Test Connection Status Box
                testResultText?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (testIsSuccess == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (testIsSuccess == true) Color(0xFF4CAF50) else Color(0xFFE57373)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (testIsSuccess == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (testIsSuccess == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (testIsSuccess == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Test Connection Button
                    OutlinedButton(
                        onClick = {
                            val candidateConfig = AiConfig(
                                provider = selectedProvider,
                                geminiApiKey = geminiApiKey.trim(),
                                geminiModel = geminiModel.trim(),
                                customBaseUrl = customBaseUrl.trim(),
                                customApiKey = customApiKey.trim(),
                                customModel = customModel.trim(),
                                temperature = temperature
                            )
                            configManager.saveConfig(candidateConfig)

                            isTestingConnection = true
                            testResultText = null
                            testIsSuccess = null

                            coroutineScope.launch {
                                val testRes = aiService.testConnection()
                                isTestingConnection = false
                                if (testRes.isSuccess) {
                                    testIsSuccess = true
                                    testResultText = "连接成功！AI回复：${testRes.getOrNull()}"
                                } else {
                                    testIsSuccess = false
                                    testResultText = "连接失败：${testRes.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        enabled = !isTestingConnection,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_ai_connection_button")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("测试中…")
                        } else {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("测试连接")
                        }
                    }

                    // Save Config Button
                    Button(
                        onClick = {
                            val newConfig = AiConfig(
                                provider = selectedProvider,
                                geminiApiKey = geminiApiKey.trim(),
                                geminiModel = geminiModel.trim(),
                                customBaseUrl = customBaseUrl.trim(),
                                customApiKey = customApiKey.trim(),
                                customModel = customModel.trim(),
                                temperature = temperature
                            )
                            configManager.saveConfig(newConfig)
                            onConfigSaved(newConfig)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_ai_config_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存配置", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
