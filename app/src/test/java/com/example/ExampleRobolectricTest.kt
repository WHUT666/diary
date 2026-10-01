package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiConfig
import com.example.ai.AiConfigManager
import com.example.ai.AiProvider
import com.example.data.DiaryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("日记", appName)
  }

  @Test
  fun `ai config manager default and save`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = AiConfigManager(context)
    val initialConfig = manager.loadConfig()
    assertNotNull(initialConfig)

    val customConfig = AiConfig(
      provider = AiProvider.CUSTOM_OPENAI,
      customBaseUrl = "https://api.deepseek.com/v1",
      customApiKey = "sk-test",
      customModel = "deepseek-chat",
      temperature = 0.8f
    )
    manager.saveConfig(customConfig)

    val loaded = manager.loadConfig()
    assertEquals(AiProvider.CUSTOM_OPENAI, loaded.provider)
    assertEquals("https://api.deepseek.com/v1", loaded.customBaseUrl)
    assertEquals("deepseek-chat", loaded.customModel)
    assertEquals(0.8f, loaded.temperature, 0.01f)
  }

  @Test
  fun `word count calculation`() {
    val sampleChinese = "今天天气真好，去公园散步了。"
    val count = DiaryEntry.calculateWordCount(sampleChinese)
    assertEquals(12, count)

    val sampleEnglish = "Hello world!"
    val engCount = DiaryEntry.calculateWordCount(sampleEnglish)
    assertEquals(2, engCount)
  }
}
