package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.SecurityLockManager

/**
 * 隐私与安全锁设置弹窗
 * 用户可以在此开启/关闭隐私锁、修改密码、开启指纹识别及设置密保。
 */
@Composable
fun SecuritySettingsDialog(
    securityLockManager: SecurityLockManager,
    onDismiss: () -> Unit,
    onLockStateChanged: () -> Unit
) {
    var isLockEnabled by remember { mutableStateOf(securityLockManager.isLockEnabled) }
    var isBiometricEnabled by remember { mutableStateOf(securityLockManager.isBiometricEnabled) }
    val canHardwareBiometric = remember { securityLockManager.canAuthenticateWithBiometrics() }

    // 设置新密码/修改密码状态
    var showSetPasswordSection by remember { mutableStateOf(!isLockEnabled) }
    var oldPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var questionInput by remember { mutableStateOf(securityLockManager.securityQuestion ?: "我的小学名字是？") }
    var answerInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("security_settings_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("日记隐私安全锁", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "开启隐私锁后，每次启动应用或从后台返回都需要验证身份，守护您的私人内心独白。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 总开关 Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("开启应用启动验证", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = if (isLockEnabled) "已受密码与生物识别保护" else "当前未开启保护",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    // 关闭保护
                                    securityLockManager.disableLock()
                                    isLockEnabled = false
                                    successMessage = "已成功解除隐私锁"
                                    errorMessage = null
                                    onLockStateChanged()
                                } else {
                                    // 展开设置密码区
                                    showSetPasswordSection = true
                                }
                            },
                            modifier = Modifier.testTag("switch_enable_lock")
                        )
                    }
                }

                // 生物识别支持选项
                if (isLockEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("指纹 / 面容快速解锁", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    text = if (canHardwareBiometric) "支持硬件生物识别" else "当前设备未录入或不支持指纹",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isBiometricEnabled && canHardwareBiometric,
                                enabled = canHardwareBiometric,
                                onCheckedChange = { checked ->
                                    isBiometricEnabled = checked
                                    securityLockManager.setBiometricEnabled(checked)
                                    onLockStateChanged()
                                },
                                modifier = Modifier.testTag("switch_biometric")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            showSetPasswordSection = !showSetPasswordSection
                            errorMessage = null
                            successMessage = null
                        },
                        modifier = Modifier.fillMaxWidth().testTag("change_password_button")
                    ) {
                        Icon(imageVector = Icons.Default.LockReset, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showSetPasswordSection) "收起修改密码" else "修改4位解锁密码")
                    }
                }

                // 密码输入设置表单
                AnimatedVisibility(visible = showSetPasswordSection) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = DividerDefaults.color.copy(alpha = 0.4f)
                        )

                        Text(
                            text = if (isLockEnabled) "修改安全密码" else "首次设置4位数字安全密码",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLockEnabled) {
                            OutlinedTextField(
                                value = oldPasswordInput,
                                onValueChange = { oldPasswordInput = it },
                                label = { Text("原密码") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) newPasswordInput = it
                            },
                            label = { Text("新密码（4位数字）") },
                            placeholder = { Text("如：1234") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("new_password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPasswordInput = it
                            },
                            label = { Text("确认新密码") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "安全密保问题（用于忘记密码时重置）",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { questionInput = it },
                            label = { Text("密保问题") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = answerInput,
                            onValueChange = { answerInput = it },
                            label = { Text("密保答案") },
                            placeholder = { Text("答案不区分大小写") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("security_answer_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                successMessage = null

                                if (isLockEnabled) {
                                    if (!securityLockManager.verifyPassword(oldPasswordInput)) {
                                        errorMessage = "原密码输入不正确"
                                        return@Button
                                    }
                                }

                                if (newPasswordInput.length != 4) {
                                    errorMessage = "新密码必须为4位纯数字"
                                    return@Button
                                }

                                if (newPasswordInput != confirmPasswordInput) {
                                    errorMessage = "两次输入的新密码不一致"
                                    return@Button
                                }

                                if (answerInput.isBlank()) {
                                    errorMessage = "请填写密保答案以便忘记时找回"
                                    return@Button
                                }

                                val success = securityLockManager.setPassword(
                                    newPasswordInput,
                                    questionInput,
                                    answerInput
                                )
                                if (success) {
                                    isLockEnabled = true
                                    showSetPasswordSection = false
                                    oldPasswordInput = ""
                                    newPasswordInput = ""
                                    confirmPasswordInput = ""
                                    answerInput = ""
                                    successMessage = "密码设置成功！"
                                    onLockStateChanged()
                                } else {
                                    errorMessage = "保存密码失败，请重试"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_password_button")
                        ) {
                            Text("保存密码与安全设置")
                        }
                    }
                }

                // 反馈消息
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = successMessage ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}
