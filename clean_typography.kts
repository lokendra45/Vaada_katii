import java.io.File

fun cleanFile(file: File) {
    var content = file.readText()
    val pattern = Regex("MaterialTheme\\.typography\\.[a-zA-Z]+\\.copy\\s*\\(")
    
    var changed = false
    while (true) {
        val match = pattern.find(content) ?: break
        val startIndex = match.range.last
        var openCount = 1
        var endIndex = startIndex + 1
        
        while (endIndex < content.length && openCount > 0) {
            val c = content[endIndex]
            if (c == '(') openCount++
            else if (c == ')') openCount--
            endIndex++
        }
        
        if (openCount == 0) {
            val toReplace = content.substring(match.range.first, endIndex)
            val replacement = toReplace.substringBefore(".copy")
            content = content.replace(toReplace, replacement)
            changed = true
        } else {
            break
        }
    }
    
    if (changed) {
        file.writeText(content)
        println("Cleaned ")
    }
}

val dir = File("C:/Users/loke_Machine/Desktop/RentManagerApp/shared/src/commonMain/kotlin/com/gaatho/rent")
dir.walk().filter { it.isFile && it.extension == "kt" }.forEach { cleanFile(it) }
