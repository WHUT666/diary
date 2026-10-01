package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.FragmentActivity
import com.example.ai.AiConfigManager
import com.example.ai.AiService
import com.example.data.DiaryDatabase
import com.example.data.DiaryDraftManager
import com.example.data.DiaryRepository
import com.example.data.TimeCapsuleRepository
import com.example.data.TodoRepository
import com.example.security.BiometricAuthHelper
import com.example.security.SecurityLockManager
import com.example.ui.AppLockScreen
import com.example.ui.DiaryScreen
import com.example.ui.DiaryViewModel
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

  private val database by lazy { DiaryDatabase.getDatabase(applicationContext) }
  private val repository by lazy { DiaryRepository(database.diaryDao()) }
  private val todoRepository by lazy { TodoRepository(database.todoDao()) }
  private val timeCapsuleRepository by lazy { TimeCapsuleRepository(database.timeCapsuleDao()) }
  private val draftManager by lazy { DiaryDraftManager(applicationContext) }
  private val viewModel: DiaryViewModel by viewModels {
    DiaryViewModel.provideFactory(repository, todoRepository, timeCapsuleRepository, draftManager)
  }
  private val configManager by lazy { AiConfigManager(applicationContext) }
  private val aiService by lazy { AiService(configManager) }
  private val securityLockManager by lazy { SecurityLockManager(applicationContext) }
  private val themeManager by lazy { AppThemeManager(applicationContext) }

  // 记录离开前台的时间戳，用于后台超时自动重新上锁
  private var lastPauseTime = 0L
  private var isAppUnlockedState = mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 初始状态：若未开启密码保护，则默认已解锁；若开启了，则要求解锁
    isAppUnlockedState.value = !securityLockManager.isLockEnabled

    setContent {
      val currentTheme by themeManager.currentTheme.collectAsState()

      MyApplicationTheme(themePreset = currentTheme) {
        val isUnlocked by remember { isAppUnlockedState }

        if (securityLockManager.isLockEnabled && !isUnlocked) {
          AppLockScreen(
            securityLockManager = securityLockManager,
            onUnlocked = {
              isAppUnlockedState.value = true
            },
            onRequestBiometric = {
              requestBiometricUnlock()
            },
            modifier = Modifier.fillMaxSize()
          )
        } else {
          DiaryScreen(
            viewModel = viewModel,
            aiService = aiService,
            configManager = configManager,
            securityLockManager = securityLockManager,
            themeManager = themeManager,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    lastPauseTime = System.currentTimeMillis()
  }

  override fun onResume() {
    super.onResume()
    // 检查是否需要重新上锁
    if (securityLockManager.isLockEnabled && lastPauseTime > 0) {
      val elapsedSeconds = (System.currentTimeMillis() - lastPauseTime) / 1000
      val timeoutSeconds = securityLockManager.lockTimeoutSeconds
      if (elapsedSeconds >= timeoutSeconds) {
        isAppUnlockedState.value = false
      }
    }
  }

  private fun requestBiometricUnlock() {
    if (!securityLockManager.isBiometricEnabled || !securityLockManager.canAuthenticateWithBiometrics()) {
      return
    }

    BiometricAuthHelper.showBiometricPrompt(
      activity = this,
      title = "日记安全验证",
      subtitle = "请验证指纹或面部以进入日记",
      negativeButtonText = "使用密码解锁",
      onSuccess = {
        isAppUnlockedState.value = true
      },
      onError = { _, _ ->
        // 用户取消或生物识别失败，保持密码键盘界面
      },
      onFailed = {
        // 单次不匹配
      }
    )
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "日记 $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("日记") }
}
