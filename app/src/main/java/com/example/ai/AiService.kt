package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiExtractedMetadata(
    val recommendedTitle: String,
    val moodEmoji: String,
    val moodLabel: String,
    val weatherEmoji: String,
    val weatherLabel: String,
    val category: String
)

data class DailyInspiration(
    val quote: String,
    val author: String,
    val reflection: String
)

class AiService(private val configManager: AiConfigManager) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateText(prompt: String, systemInstruction: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            val config = configManager.loadConfig()
            try {
                when (config.provider) {
                    AiProvider.GEMINI -> callGemini(config, prompt, systemInstruction)
                    AiProvider.CUSTOM_OPENAI -> callCustomOpenAi(config, prompt, systemInstruction)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun callGemini(
        config: AiConfig,
        prompt: String,
        systemInstruction: String?
    ): Result<String> {
        val apiKey = config.effectiveGeminiKey
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return Result.failure(
                IllegalStateException("未配置 Gemini API Key。请在「AI 设置」中填入 API Key，或在 AI Studio 的 Secrets 面板中配置。")
            )
        }

        val model = config.geminiModel.ifBlank { "gemini-3.5-flash" }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val rootObj = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val textPart = JSONObject().put("text", prompt)
        partsArray.put(textPart)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootObj.put("contents", contentsArray)

        if (!systemInstruction.isNullOrBlank()) {
            val sysContent = JSONObject()
            val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
            sysContent.put("parts", sysParts)
            rootObj.put("systemInstruction", sysContent)
        }

        val genConfig = JSONObject()
        genConfig.put("temperature", config.temperature)
        rootObj.put("generationConfig", genConfig)

        val requestBody = rootObj.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(responseBodyStr)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}: $responseBodyStr"
            }
            return Result.failure(RuntimeException("Gemini 请求失败: $errorMsg"))
        }

        val responseJson = JSONObject(responseBodyStr)
        val candidates = responseJson.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            return Result.failure(RuntimeException("Gemini 未返回任何结果"))
        }

        val firstCandidate = candidates.getJSONObject(0)
        val candidateContent = firstCandidate.optJSONObject("content")
        val parts = candidateContent?.optJSONArray("parts")
        val generatedText = parts?.optJSONObject(0)?.optString("text")

        return if (!generatedText.isNullOrBlank()) {
            Result.success(generatedText.trim())
        } else {
            Result.failure(RuntimeException("模型回复文本为空"))
        }
    }

    private fun callCustomOpenAi(
        config: AiConfig,
        prompt: String,
        systemInstruction: String?
    ): Result<String> {
        val baseUrl = config.customBaseUrl.trim().removeSuffix("/")
        val endpoint = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
        val model = config.customModel.ifBlank { "deepseek-chat" }

        val rootObj = JSONObject()
        rootObj.put("model", model)
        rootObj.put("temperature", config.temperature)

        val messagesArray = JSONArray()
        if (!systemInstruction.isNullOrBlank()) {
            messagesArray.put(
                JSONObject().put("role", "system").put("content", systemInstruction)
            )
        }
        messagesArray.put(
            JSONObject().put("role", "user").put("content", prompt)
        )
        rootObj.put("messages", messagesArray)

        val requestBody = rootObj.toString().toRequestBody(jsonMediaType)
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(requestBody)

        if (config.customApiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ${config.customApiKey.trim()}")
        }

        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        val responseBodyStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(responseBodyStr)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}: $responseBodyStr"
            }
            return Result.failure(RuntimeException("自定义模型请求失败: $errorMsg"))
        }

        val responseJson = JSONObject(responseBodyStr)
        val choices = responseJson.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            return Result.failure(RuntimeException("接口未返回任何生成选项"))
        }

        val firstChoice = choices.getJSONObject(0)
        val messageObj = firstChoice.optJSONObject("message")
        val content = messageObj?.optString("content")

        return if (!content.isNullOrBlank()) {
            Result.success(content.trim())
        } else {
            Result.failure(RuntimeException("模型回复文本为空"))
        }
    }

    // --- Diary Writing Assistant Operations ---

    suspend fun testConnection(): Result<String> {
        val testPrompt = "请用一句非常简短温柔的话向正在写日记的我说声你好（不超过20字）。"
        return generateText(testPrompt, "你是一个温馨的日记写作助手。")
    }

    suspend fun expandDiary(draftOrKeywords: String): Result<String> {
        val sysPrompt = "你是一位善于感受生活细节、文字细腻温润的日记助手。根据用户提供的灵感碎片或简单草稿，将其扩写为一篇充满真情实感、有细节描写的优美日记。保持第一人称，字数在150-300字左右。避免空洞的说教，注重生活感与内心情感。"
        val prompt = "请帮我将以下生活记录或想法扩写为一篇温馨的日记：\n\n$draftOrKeywords"
        return generateText(prompt, sysPrompt)
    }

    suspend fun polishText(originalText: String): Result<String> {
        val sysPrompt = "你是一位优秀的文字编辑和日记修辞助手。在严格尊重用户原意和真实情感的前提下，润色和优化用户的日记语言，使其行文更流畅、用词更典雅生动，段落层次更清晰。直接输出润色后的日记正文，不要输出多余的解释。"
        val prompt = "请润色以下日记文本：\n\n$originalText"
        return generateText(prompt, sysPrompt)
    }

    suspend fun continueDiary(existingText: String): Result<String> {
        val sysPrompt = "你是一位有同理心的日记续写助手。根据用户已写的内容，自然地承接上文的语境和情感基调，顺着思路续写一段深入的思考或细节展开，字数在100-200字左右。直接输出续写内容。"
        val prompt = "我正在写这篇日记，请帮我顺着思路继续写下去：\n\n$existingText"
        return generateText(prompt, sysPrompt)
    }

    suspend fun extractMetadata(content: String): Result<AiExtractedMetadata> {
        val sysPrompt = "你是一个日记分析助手。分析用户的日记内容，以严格的JSON格式返回提取的元数据。格式必须为：{\"title\":\"...\",\"moodEmoji\":\"...\",\"moodLabel\":\"...\",\"weatherEmoji\":\"...\",\"weatherLabel\":\"...\",\"category\":\"...\"}。可选心情标签：开心、平静、兴奋、思考、低落、焦虑、疲惫、感恩；可选天气：☀️ 晴、⛅ 多云、🌧️ 雨、❄️ 雪、💨 风；可选分类：日常、随想、工作、学习、旅行、感悟、美食。不要输出Markdown代码块标记，只输出纯JSON。"
        val prompt = "分析这篇日记并返回元数据JSON：\n\n$content"

        return try {
            val result = generateText(prompt, sysPrompt)
            val text = result.getOrThrow()
            val cleanJson = text.replace("```json", "").replace("```", "").trim()
            val obj = JSONObject(cleanJson)
            Result.success(
                AiExtractedMetadata(
                    recommendedTitle = obj.optString("title", "今日小记"),
                    moodEmoji = obj.optString("moodEmoji", "😊"),
                    moodLabel = obj.optString("moodLabel", "开心"),
                    weatherEmoji = obj.optString("weatherEmoji", "☀️"),
                    weatherLabel = obj.optString("weatherLabel", "晴"),
                    category = obj.optString("category", "日常")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateReflectiveQuestions(content: String): Result<List<String>> {
        val sysPrompt = "你是一位温柔的心理觉察与日记教练。阅读用户的日记后，提出2到3个温和而具有启发性的反思问题，帮助作者进一步与自己的内心对话、梳理情绪或发现成长。每行一个问题，使用编号列表。"
        val prompt = "根据我写的这篇日记，给我提出2-3个启发内省的问题：\n\n$content"
        val result = generateText(prompt, sysPrompt)
        return result.map { text ->
            text.lines().map { it.trim() }.filter { it.isNotBlank() }
        }
    }

    // --- Multi-faceted Information & Discovery ---

    suspend fun fetchDailyInspiration(): Result<DailyInspiration> {
        val sysPrompt = "你是一位生活哲理与文学大师。请为今日写日记的人推荐一句触动人心的智慧格言，并配上简短的生活感悟。请严格按JSON输出：{\"quote\":\"格言名句\",\"author\":\"作者或出处\",\"reflection\":\"一到两句解读感悟\"}。不要Markdown格式，直接输出纯JSON。"
        val prompt = "请生成今日的日记灵感格言。"
        return try {
            val text = generateText(prompt, sysPrompt).getOrThrow()
            val cleanJson = text.replace("```json", "").replace("```", "").trim()
            val obj = JSONObject(cleanJson)
            Result.success(
                DailyInspiration(
                    quote = obj.optString("quote", "每一天都是一首尚未谱完的诗。"),
                    author = obj.optString("author", "生活之思"),
                    reflection = obj.optString("reflection", "停下匆匆脚步，记录下此时此刻的心情。")
                )
            )
        } catch (e: Exception) {
            // Graceful fallback
            Result.success(
                DailyInspiration(
                    quote = "步履不停，生活在每一个当下发光。",
                    author = "木心",
                    reflection = "将今日的微光收进日记本里，岁月自会沉淀回响。"
                )
            )
        }
    }

    suspend fun fetchOnThisDayInsight(dateStr: String): Result<String> {
        val sysPrompt = "你是一位博学的历史与文化向导。告诉用户在历史上与今天这个日期相关的1-2件具有人文温度、科学发现或哲学意义的趣闻或历史瞬间，并联系到当下的日常生活，给作者一个独特的写日记切入视角。字数在150字左右。"
        val prompt = "今天是 $dateStr，历史上今天有什么耐人寻味或温暖的故事？请给我一些历史视角与写作灵感。"
        return generateText(prompt, sysPrompt)
    }

    suspend fun fetchMindfulnessPrompt(): Result<String> {
        val sysPrompt = "你是一位正念生活导师。给用户出一个今天可以观察自己内心或周围环境的正念觉察小练习（例如：观察一杯水的温度、注意身体的紧绷、感知风的声音、回想今天陌生人的微笑等），引导用户以此为主题写下日记。字数在80-120字。"
        val prompt = "请给我一个今天的正念观察练习题目与引导。"
        return generateText(prompt, sysPrompt)
    }

    suspend fun convertFragmentToDiary(fragments: String): Result<String> {
        val sysPrompt = "你是一位日记整理达人。用户给你输入了一堆琐碎的随想碎片、备忘要点或零散句子，请将它们串联成一篇语脉清晰、真挚自然的个人日记。分段得体，保留所有核心信息点。"
        val prompt = "请帮我将以下零散的备忘碎片整理成一篇连贯的日记：\n\n$fragments"
        return generateText(prompt, sysPrompt)
    }

    suspend fun generateMoodHealingAdvice(moodLabel: String, noteSnippet: String): Result<String> {
        val sysPrompt = "你是一位温暖体贴的心理陪伴者。用户记录了自己当前的心情与一些状态，请用非常温暖治愈、不假大空的语言给予关怀和理解，并给出1-2个立即可以做的心灵放松小动作。字数在120字左右。"
        val prompt = "我今天的心情是【$moodLabel】，记录的内容片段是：$noteSnippet。请给我一些温暖的抚慰和放松建议。"
        return generateText(prompt, sysPrompt)
    }

    // --- AI + Todo Deep Integration ---

    suspend fun extractTodosFromDiary(diaryContent: String): Result<List<String>> {
        val sysPrompt = "你是一位高效生活助理。请通读这篇日记，提取出其中提到或隐含的所有待办任务、计划安排、承诺或下一步行动建议。每行输出一个待办事项，以动词开头，简练明确（如'回复李老师的邮件'、'买一本新笔记本'）。如果没有明确待办，基于日记内容提供2项有助于作者的行动建议。每行一项，不要输出额外说明和数字编号。"
        val prompt = "请分析以下日记内容并提取出所有待办清单：\n\n$diaryContent"
        val result = generateText(prompt, sysPrompt)
        return result.map { text ->
            text.lines()
                .map { line -> line.trim().replace(Regex("^[•\\-*\\d\\.\\s]+"), "") }
                .filter { it.isNotBlank() }
        }
    }

    suspend fun breakdownTodoTask(taskTitle: String): Result<List<String>> {
        val sysPrompt = "你是一位精益任务管理大师。请将用户输入的待办事项拆解为 3 到 5 个清晰、易于立即行动的具体子步骤。每行一条，简短明确，不要带Markdown格式或数字前缀。"
        val prompt = "请帮我拆解这项待办任务，制定具体行动步骤：【$taskTitle】"
        val result = generateText(prompt, sysPrompt)
        return result.map { text ->
            text.lines()
                .map { line -> line.trim().replace(Regex("^[•\\-*\\d\\.\\s]+"), "") }
                .filter { it.isNotBlank() }
        }
    }

    suspend fun generateDiaryFromTodos(todosSummary: String): Result<String> {
        val sysPrompt = "你是一位善于复盘与生活洞察的日记导师。根据用户今天推进和完成的待办任务清单，为用户撰写一篇富有成就感、生活温度与自我关怀的复盘日记。语言真挚自然，回顾今天付出的努力、完成任务的满足感以及对明天的憧憬。段落清晰，200字左右。"
        val prompt = "这是我今天推进与完成的待办清单：\n\n$todosSummary\n\n请帮我生成一篇今日复盘与生活总结日记："
        return generateText(prompt, sysPrompt)
    }

    suspend fun suggestTodoPriorities(todosList: List<String>): Result<String> {
        val sysPrompt = "你是一位高效时间管理教练。请审视用户的这批待办事项，给出1-2句点醒专注力的优先级排序建议，以及保持心流的鼓励。"
        val prompt = "我目前的待办事项有：\n" + todosList.joinToString("\n") { "• $it" } + "\n\n请给出高效处理优先级建议："
        return generateText(prompt, sysPrompt)
    }

    // --- Rich Text & Multi-Modal Location / Weather Optimization ---

    suspend fun formatAsRichTextDiary(rawText: String): Result<String> {
        val sysPrompt = "你是一位出版级杂志主编与排版大师。请将用户写下的日记草稿进行精美的Markdown排版整理：\n1. 提炼或保留一个优雅的 `# 一级主标题`\n2. 提炼一句最具哲思感悟的句子用 `> 引用语法` 单独作为金句块\n3. 正文分段清晰，对核心字句适当运用 `**加粗**` 或 `*斜体*`\n4. 在文末整理 2~3 条今日微光或收获清单用 `- 无序列表`\n请直接输出排版后的Markdown正文，不要带有外层的代码块标记。"
        val prompt = "请将以下这篇日记进行优美的Markdown富文本排版与润色：\n\n$rawText"
        return generateText(prompt, sysPrompt)
    }

    suspend fun generatePhotoLocationDiary(
        location: String,
        weather: String,
        temperature: String,
        mood: String,
        hint: String
    ): Result<String> {
        val sysPrompt = "你是一位浪漫细腻的纪实文学作家。用户在特定地点与天气下记录了生活瞬间。请结合地点、天气气温、心情和简短的画面线索，写下一篇富有画面感、空间呼吸感和真挚温度的图文日记，支持Markdown格式（带有标题、引用名句和精致分段），字数在180-260字左右。"
        val prompt = "地点：${location.ifBlank { "日常生活" }}；天气：$weather $temperature；心情：$mood；画面线索：${hint.ifBlank { "街景与身边的生活光影" }}。请为我写下一篇图文意境日记："
        return generateText(prompt, sysPrompt)
    }

    // --- Emotional Report & Deep Reflection ---

    suspend fun generateEmotionalReport(
        periodTitle: String,
        statsSummary: String,
        snippets: String
    ): Result<String> {
        val sysPrompt = """
            你是一位温暖深刻的心理学心灵导师与生活哲学家。
            请根据用户在【$periodTitle】的心情统计、写作频率及日记片段，为用户撰写一份兼具人文深度与治愈力量的「心绪复盘信」。
            内容包含：
            1. 【心境概览】：用温柔诗意的语言总结这段时间的情感主旋律；
            2. 【生活闪光点】：捕捉日记中展现的坚韧、微确幸与成长线索；
            3. 【自我关怀锦囊】：给出 2 条具体可行且舒缓身心的生活建议。
            语气真挚体贴，段落分明，字数约 220-300 字。
        """.trimIndent()
        val prompt = "这是我【$periodTitle】的记录数据与心情脉络：\n$statsSummary\n\n日记节选：\n$snippets\n\n请为我撰写这篇专属的心绪复盘信："
        return generateText(prompt, sysPrompt)
    }
}
