package com.example.data

data class JournalTemplate(
    val id: String,
    val title: String,
    val category: String,
    val iconEmoji: String,
    val subtitle: String,
    val defaultMoodEmoji: String,
    val defaultMoodLabel: String,
    val templateContent: String
)

object JournalTemplateProvider {
    val templates = listOf(
        JournalTemplate(
            id = "three_good_things",
            title = "三件微小确幸",
            category = "感恩心理",
            iconEmoji = "🌻",
            subtitle = "积极心理学练习，捕获日常中的温暖微光",
            defaultMoodEmoji = "😊",
            defaultMoodLabel = "开心",
            templateContent = """
### 🌻 今日三件微小确幸

1. **第一件微光：**
   - 发生了什么：
   - 为什么让我感到温暖或踏实：

2. **第二件微光：**
   - 细节描述：
   - 当时的心境：

3. **第三件微光：**
   - 意外的惊喜：
   - 我想感谢的人或事物：

---
> 💡 *“幸福不在于拥有多少，而在于能感知多少当下的美好。”*
""".trimIndent()
        ),
        JournalTemplate(
            id = "morning_intention",
            title = "晨间心流唤醒",
            category = "晨间意向",
            iconEmoji = "🌅",
            subtitle = "开启专注与清明的一天，设立今日核心意图",
            defaultMoodEmoji = "⚡",
            defaultMoodLabel = "元气",
            templateContent = """
### 🌅 晨间心流与意向唤醒

- **今日身体与精力状态：** [ 元气充沛 / 略带疲倦 / 平静如水 ]
- **今日唯一最核心的目标 (The One Thing)：**
  
- **期待经历的三个微小瞬间：**
  1. 
  2. 
  3. 

- **对自己的今日赋能寄语：**
  > 无论今天发生什么，我都会保持从容与专注。
""".trimIndent()
        ),
        JournalTemplate(
            id = "evening_reflection",
            title = "晚安睡前复盘",
            category = "睡前放下",
            iconEmoji = "🌙",
            subtitle = "卸下一天的浮沉，与自己温和对饮",
            defaultMoodEmoji = "😌",
            defaultMoodLabel = "平静",
            templateContent = """
### 🌙 晚安睡前复盘与放下

- **今天值得自我夸奖的一件事：**
  
- **今天遇到的阻碍与心得学问：**
  
- **需要彻底放下并交还给夜晚的烦忧：**
  
- **给明天的自己留一句话：**
  晚安，今天的我已经全力以赴。
""".trimIndent()
        ),
        JournalTemplate(
            id = "emotional_unpack",
            title = "心绪梳理与解压",
            category = "情绪疗愈",
            iconEmoji = "🌿",
            subtitle = "看见情绪、接纳感受，梳理内在打结的思绪",
            defaultMoodEmoji = "🤔",
            defaultMoodLabel = "思考",
            templateContent = """
### 🌿 心绪梳理与自我对话

- **我此刻最直观的身体与情绪感受：**
  
- **触动这个情绪的具体事实是什么？（客观描述）：**
  
- **我脑海中自动跳出的念头（主观评价）：**
  
- **换一个慈悲和宏观的角度，事情还可以怎么看？**
  
- **现在，我能为自己做的一件安抚小事：**
  
""".trimIndent()
        ),
        JournalTemplate(
            id = "weekly_review",
            title = "周度深度复盘",
            category = "周期精进",
            iconEmoji = "📊",
            subtitle = "站在高处俯瞰一周的成长轨迹与生命足印",
            defaultMoodEmoji = "✨",
            defaultMoodLabel = "元气",
            templateContent = """
### 📊 本周深度复盘与展望

#### 一、本周高光里程碑
- 
- 

#### 二、时间与精力投入反思
- 哪些事情带来了最高的情绪能量：
- 哪些事情造成了无效损耗：

#### 三、下周三大突破重心
1. 
2. 
3. 
""".trimIndent()
        ),
        JournalTemplate(
            id = "travel_notes",
            title = "旅途与风景漫步",
            category = "生活漫游",
            iconEmoji = "✈️",
            subtitle = "收录行走于天地间的气味、声响与见闻",
            defaultMoodEmoji = "🥳",
            defaultMoodLabel = "兴奋",
            templateContent = """
### ✈️ 旅途漫游笔记

- **坐标与行迹：** 
- **捕捉到的声音与气味：**
  
- **最令人心动的画面或转角：**
  
- **偶遇的人或一段有趣对话：**
  
- **旅途带给我的生活隐喻：**
  
""".trimIndent()
        )
    )
}
