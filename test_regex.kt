import java.util.regex.Pattern

fun main() {
    val line = "Hello ![img](url1) world [video](url2) test [audio](url3) end."
    val regex = Regex("""(!\[(.*?)\]\((.*?)\))|(\[video\]\((.*?)\))|(\[audio\]\((.*?)\))""")
    
    var lastIndex = 0
    regex.findAll(line).forEach { match ->
        val textBefore = line.substring(lastIndex, match.range.first).trim()
        if (textBefore.isNotEmpty()) {
            println("Paragraph: $textBefore")
        }
        if (match.groups[1] != null) {
            println("Image: alt=${match.groups[2]?.value}, url=${match.groups[3]?.value}")
        } else if (match.groups[4] != null) {
            println("Video: url=${match.groups[5]?.value}")
        } else if (match.groups[6] != null) {
            println("Audio: url=${match.groups[7]?.value}")
        }
        lastIndex = match.range.last + 1
    }
    val textAfter = line.substring(lastIndex).trim()
    if (textAfter.isNotEmpty()) {
        println("Paragraph: $textAfter")
    }
}
